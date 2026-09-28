package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.HitCount
import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.data.MoveCategory
import com.tambapps.pokemon.champions.engine.description.DescriptionBuilder
import com.tambapps.pokemon.champions.engine.description.StatDisplay
import com.tambapps.pokemon.champions.engine.description.StatInvestment
import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.Stat
import kotlin.math.floor

/**
 * The Champions damage formula, ported from GET_DAMAGE_SV/calcGeneralMods in
 * NCP-VGC-Damage-Calculator. See the module README for what's deliberately out of scope
 * (Z-moves, Dynamax, Terastallization -- none exist in Champions -- plus a short list of
 * narrow edge cases this port doesn't attempt).
 */
object DamageCalculator {

  /** Every Pokemon in Champions battles at this level; there is no way to change it. */
  private const val CHAMPIONS_LEVEL = 50

  /**
   * One hit of [moveUse] against [defender]. For multi-hit moves, call this once per hit -- see [calculateParentalBondHits].
   * [statDisplay] is how [DamageResult.description] writes the stat investments.
   */
  fun calculateSingleHit(
    attacker: BattlePokemon,
    defender: BattlePokemon,
    moveUse: MoveUse,
    field: Battlefield,
    statDisplay: StatDisplay = StatDisplay.STAT_POINTS,
  ): DamageResult = describedHit(attacker, defender, moveUse, field, statDisplay).first

  /**
   * Every hit of one use of [moveUse]'s move: [hits] times for a multi-hit move (each Triple Axel/Triple Kick
   * hit with its own power), twice for a Parental Bond single-hit move (as the source calculator, not for a
   * spread move in Doubles), once otherwise. Every hit uses the attacker/defender's starting state.
   * [statDisplay] is how the descriptions write the stat investments.
   */
  fun calculateMove(
    attacker: BattlePokemon,
    defender: BattlePokemon,
    moveUse: MoveUse,
    field: Battlefield,
    hits: Int = defaultHitCount(moveUse.move, attacker),
    statDisplay: StatDisplay = StatDisplay.STAT_POINTS,
  ): MoveDamageResult {
    val move = moveUse.move
    requireValid(hits in move.hitCountRange) { "${move.name.value} hits ${move.hitCountRange} times, got $hits" }
    val isParentalBond = attacker.resolvedAbility == Ability.PARENTAL_BOND && move.hitCount == HitCount.Once &&
      (field.format == BattleFormat.SINGLES || !move.isSpread)
    if (isParentalBond) {
      val (firstHit, description) = describedHit(attacker, defender, moveUse.copy(isSecondParentalBondHit = false), field, statDisplay)
      val secondHit = calculateSingleHit(attacker, defender, moveUse.copy(isSecondParentalBondHit = true), field, statDisplay)
      description.attackerAbility(attacker.resolvedAbility)
      description.hits = 2
      return moveResult(listOf(firstHit, secondHit), description, statDisplay)
    }
    val firstMoveUse = if (move.hasEscalatingPower) moveUse.copy(hitNumber = 1) else moveUse
    val (firstHit, description) = describedHit(attacker, defender, firstMoveUse, field, statDisplay)
    if (move.hitCount != HitCount.Once) description.hits = hits
    val allHits = if (move.hasEscalatingPower) {
      listOf(firstHit) + (2..hits).map { calculateSingleHit(attacker, defender, moveUse.copy(hitNumber = it), field, statDisplay) }
    } else {
      // every hit is the same, no need to calculate it several times
      List(hits) { firstHit }
    }
    return moveResult(allHits, description, statDisplay)
  }

  private fun moveResult(hits: List<DamageResult>, description: DescriptionBuilder, statDisplay: StatDisplay): MoveDamageResult {
    val calcDescription = description.build()
    return MoveDamageResult(hits, calcDescription.format(statDisplay), calcDescription)
  }

  /** Parental Bond always hits twice: a full-power hit, then a second hit at a quarter of that base damage. */
  fun calculateParentalBondHits(
    attacker: BattlePokemon,
    defender: BattlePokemon,
    moveUse: MoveUse,
    field: Battlefield,
    statDisplay: StatDisplay = StatDisplay.STAT_POINTS,
  ): ParentalBondHits {
    requireValid(attacker.resolvedAbility == Ability.PARENTAL_BOND) { "calculateParentalBondHits requires the attacker to have Parental Bond" }
    val firstHit = calculateSingleHit(attacker, defender, moveUse.copy(isSecondParentalBondHit = false), field, statDisplay)
    val secondHit = calculateSingleHit(attacker, defender, moveUse.copy(isSecondParentalBondHit = true), field, statDisplay)
    return ParentalBondHits(firstHit, secondHit)
  }

  /** The hit with its description, and what the description was built from, for [calculateMove] to add the whole-move facts. */
  private fun describedHit(
    attacker: BattlePokemon,
    defender: BattlePokemon,
    moveUse: MoveUse,
    field: Battlefield,
    statDisplay: StatDisplay,
  ): Pair<DamageResult, DescriptionBuilder> {
    val description = DescriptionBuilder(attacker.species.name, moveUse.move.name, defender.species.name)
    val hit = calculateHit(attacker, defender, moveUse, field, description)
    val calcDescription = description.build()
    return hit.copy(description = calcDescription.format(statDisplay), calcDescription = calcDescription) to description
  }

  private fun calculateHit(attacker: BattlePokemon, defender: BattlePokemon, moveUse: MoveUse, field: Battlefield, description: DescriptionBuilder): DamageResult {
    val move = moveUse.move
    val isQuarteredByProtect = isQuarteredByProtect(move, attacker, field)
    if (isQuarteredByProtect) description.attackerAbility(attacker.resolvedAbility)
    if (move.category == MoveCategory.STATUS) return DamageResult.noDamage()

    val effectiveType = effectiveTypeOf(move, attacker, field)
    if (isRetypedByLiquidVoice(move, attacker)) description.attackerAbility(attacker.resolvedAbility)
    if (ImmunityChecker.isImmune(move, effectiveType, attacker, defender, field, description)) {
      return DamageResult.noDamage(typeEffectiveness = 0.0)
    }
    description.hp = StatInvestment.of(defender, Stat.HP)

    val typeEffectiveness = TypeEffectivenessCalculator.effectivenessOf(move, effectiveType, attacker, defender, field)
    val effectiveCategory = effectiveCategoryOf(move, attacker, defender)
    val hitsPhysical = hitsPhysicalDefense(move, effectiveCategory)
    val isCritical = moveUse.isCritical || move.alwaysCrits

    val basePower = resolveBasePower(move, effectiveType, moveUse, attacker, defender, field, description)
    val attack = resolveAttack(move, effectiveType, attacker, defender, isCritical, field, description)
    val defense = resolveDefense(move, attacker, defender, hitsPhysical, isCritical, field, description)
    val baseDamage = baseDamageFormula(basePower, attack, defense)

    val preRollDamage = applyPreRollModifiers(baseDamage, move, effectiveType, moveUse, attacker, defender, field, isCritical, description)
    val stabMod = stabMultiplier(move, effectiveType, attacker)
    describeStab(move, effectiveType, attacker, description)
    val burnHalves = isBurnHalved(move, attacker, effectiveCategory)
    description.isBurned = burnHalves
    val finalMod = chainMods(FinalMods.resolve(move, effectiveType, attacker, defender, field, isCritical, typeEffectiveness, description))
    description.isQuarteredByProtect = isQuarteredByProtect

    val rolls = (85..100).map { percent ->
      rollDamage(preRollDamage, percent, stabMod, typeEffectiveness, burnHalves, finalMod, isQuarteredByProtect)
    }.sorted()

    return DamageResult(
      rolls = rolls,
      typeEffectiveness = typeEffectiveness,
      isCritical = isCritical,
      attackStat = AttackStatResolver.attackStatSourceOf(move, attacker, defender),
      defenseStat = DefenseStatResolver.defenseStatOf(hitsPhysical),
    )
  }

  private fun resolveBasePower(
    move: Move,
    effectiveType: PokeType,
    moveUse: MoveUse,
    attacker: BattlePokemon,
    defender: BattlePokemon,
    field: Battlefield,
    description: DescriptionBuilder,
  ): Int {
    val power = BasePowerResolver.resolve(move, moveUse, attacker, defender, field, description)
    val mods = BasePowerMods.resolve(power, move, effectiveType, moveUse, attacker, defender, field, description)
    return maxOf(1, pokeRound(power * chainMods(mods), 0x1000))
  }

  private fun resolveAttack(
    move: Move,
    effectiveType: PokeType,
    attacker: BattlePokemon,
    defender: BattlePokemon,
    isCritical: Boolean,
    field: Battlefield,
    description: DescriptionBuilder,
  ): Int {
    val attack = AttackStatResolver.resolve(move, attacker, defender, isCritical, description)
    val mods = AttackStatMods.resolve(move, effectiveType, attacker, defender, field, description)
    return maxOf(1, pokeRound(attack * chainMods(mods), 0x1000))
  }

  private fun resolveDefense(
    move: Move,
    attacker: BattlePokemon,
    defender: BattlePokemon,
    hitsPhysical: Boolean,
    isCritical: Boolean,
    field: Battlefield,
    description: DescriptionBuilder,
  ): Int {
    val defense = DefenseStatResolver.resolve(move, attacker, defender, hitsPhysical, isCritical, field, description)
    val mods = DefenseStatMods.resolve(defender, field, hitsPhysical, description)
    return maxOf(1, pokeRound(defense * chainMods(mods), 0x1000))
  }

  private fun baseDamageFormula(basePower: Int, attack: Int, defense: Int): Int {
    val levelFactor = 2 * CHAMPIONS_LEVEL / 5 + 2
    return levelFactor * basePower * attack / defense / 50 + 2
  }

  private fun applyPreRollModifiers(
    baseDamage: Int,
    move: Move,
    effectiveType: PokeType,
    moveUse: MoveUse,
    attacker: BattlePokemon,
    defender: BattlePokemon,
    field: Battlefield,
    isCritical: Boolean,
    description: DescriptionBuilder,
  ): Int {
    var damage = baseDamage
    if (field.format != BattleFormat.SINGLES && move.isSpread) damage = pokeRound(damage * 0xC00, 0x1000)
    if (moveUse.isSecondParentalBondHit) damage = pokeRound(damage * 0x0400, 0x1000)
    damage = applyWeatherMod(damage, effectiveType, attacker, field, description)
    if (defender.isVulnerableFromGlaiveRush) {
      damage = pokeRound(damage * 0x2000, 0x1000)
      description.isGlaiveMod = true
    }
    if (isCritical) {
      damage = floor(damage * 1.5).toInt()
      description.isCritical = true
    }
    return damage
  }

  private fun applyWeatherMod(damage: Int, effectiveType: PokeType, attacker: BattlePokemon, field: Battlefield, description: DescriptionBuilder): Int {
    val boosted = (isSunActive(attacker, field) && effectiveType == PokeType.FIRE) ||
      (field.weather == Weather.RAIN && effectiveType == PokeType.WATER)
    if (boosted) {
      // the source credits Mega Sol over the weather whenever the attacker has it
      if (attacker.resolvedAbility == Ability.MEGA_SOL) description.attackerAbility(attacker.resolvedAbility) else description.weather(field.weather)
      return pokeRound(damage * 0x1800, 0x1000)
    }

    val weakened = (field.weather == Weather.SUN && effectiveType == PokeType.WATER) ||
      (field.weather == Weather.RAIN && effectiveType == PokeType.FIRE && attacker.resolvedAbility != Ability.MEGA_SOL)
    if (weakened) {
      description.weather(field.weather)
      return pokeRound(damage * 0x800, 0x1000)
    }

    return damage
  }

  private fun isBurnHalved(move: Move, attacker: BattlePokemon, effectiveCategory: MoveCategory): Boolean =
    attacker.status == Status.BURNED &&
      effectiveCategory == MoveCategory.PHYSICAL &&
      attacker.resolvedAbility != Ability.GUTS &&
      !move.ignoresBurn

  /** Piercing Drill and Unseen Fist punch through Protect on contact; Champions has no Z-moves or Dynamax to add to this. */
  private fun isQuarteredByProtect(move: Move, attacker: BattlePokemon, field: Battlefield): Boolean =
    field.defenderSide.isProtected &&
      (attacker.resolvedAbility == Ability.PIERCING_DRILL || attacker.resolvedAbility == Ability.UNSEEN_FIST) &&
      move.makesContact

  private fun rollDamage(
    preRollDamage: Int,
    rollPercent: Int,
    stabMod: Int,
    typeEffectiveness: Double,
    burnHalves: Boolean,
    finalMod: Int,
    isQuarteredByProtect: Boolean,
  ): Int {
    var damage = preRollDamage * rollPercent / 100
    damage = pokeRound(damage * stabMod, 0x1000)
    damage = floor(damage * typeEffectiveness).toInt()
    if (burnHalves) damage /= 2
    damage = pokeRound(damage * finalMod, 0x1000)
    if (isQuarteredByProtect) damage = pokeRound(damage * 0x400, 0x1000)
    damage = maxOf(1, damage)
    if (damage > 65535) damage %= 65536
    return damage
  }
}

/** The two hits of a Parental Bond attack: a full-power hit, then a weaker follow-up. */
data class ParentalBondHits(val firstHit: DamageResult, val secondHit: DamageResult) {
  val totalMinDamage: Int get() = firstHit.minDamage + secondHit.minDamage
  val totalMaxDamage: Int get() = firstHit.maxDamage + secondHit.maxDamage
}
