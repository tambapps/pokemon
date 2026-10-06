package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.AbilityName
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.HitCount
import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.data.MoveCategory
import com.tambapps.pokemon.champions.engine.description.CalcFactsBuilder
import com.tambapps.pokemon.champions.engine.description.StatDisplay
import com.tambapps.pokemon.champions.engine.description.StatInvestment
import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.Stat
import kotlin.math.floor

/**
 * The Champions damage formula, ported from CALCULATE_ALL_MOVES_SV/GET_DAMAGE_SV/calcGeneralMods in
 * NCP-VGC-Damage-Calculator. See the module README for what's deliberately out of scope
 * (Z-moves, Dynamax, Terastallization -- none exist in Champions -- plus a short list of
 * narrow edge cases this port doesn't attempt).
 *
 * Every calc first applies what happens before any move ([BattleSetup]: Intimidate, terrain seeds, Trace...), then
 * computes the hit(s) like the source does.
 */
object DamageCalculator {

  /** Every Pokemon in Champions battles at this level; there is no way to change it. */
  internal const val CHAMPIONS_LEVEL = 50

  private val CRIT_BLOCKING_ABILITIES = setOf(Ability.BATTLE_ARMOR, Ability.SHELL_ARMOR)

  /**
   * One hit of [moveUse] against [defender], as the first hit of the move. For every hit of a multi-hit or Parental
   * Bond move, with what changes between them, see [calculateMove].
   * [statDisplay] is how [DamageResult.description] writes the stat investments.
   */
  fun calculateSingleHit(
    attacker: BattlePokemon,
    defender: BattlePokemon,
    moveUse: MoveUse,
    field: Battlefield,
    statDisplay: StatDisplay = StatDisplay.STAT_POINTS,
  ): DamageResult {
    val battle = BattleSetup.prepare(attacker, defender, field)
    return calculateHit(battle, moveUse, newFacts(battle, moveUse), statDisplay).result
  }

  /**
   * Every hit of one use of [moveUse]'s move: [hits] times for a multi-hit move (each Triple Axel/Triple Kick
   * hit with its own power), twice for a Parental Bond single-hit move (as the source calculator, not for a
   * spread move in Doubles), once otherwise. Like the source, the hits after the first account for what the
   * first ones changed: a consumed resist berry, a broken Multiscale, Weak Armor, Stamina, Gooey, Spicy Spray...
   * [statDisplay] is how the descriptions write the stat investments. The result also carries the move's KO chance.
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
    return calculatePreparedMove(BattleSetup.prepare(attacker, defender, field), moveUse, hits, statDisplay)
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
    val battle = BattleSetup.prepare(attacker, defender, field)
    val first = moveUse.copy(isSecondParentalBondHit = false)
    val firstHit = calculateHit(battle, first, newFacts(battle, first), statDisplay).result
    val childBattle = battle.copy(attacker = battle.attacker.asParentalBondChild())
    val second = moveUse.copy(isSecondParentalBondHit = true)
    val secondHit = calculateHit(childBattle, second, newFacts(childBattle, second), statDisplay).result
    return ParentalBondHits(firstHit, secondHit)
  }

  /** The whole move once [BattleSetup] was applied: the first hit, then the others as [AdditionalHits] computes them. */
  internal fun calculatePreparedMove(battle: PreparedBattle, moveUse: MoveUse, hits: Int, statDisplay: StatDisplay): MoveDamageResult {
    val move = moveUse.move
    val firstMoveUse = if (move.hasEscalatingPower) moveUse.copy(hitNumber = 1) else moveUse
    val facts = newFacts(battle, firstMoveUse)
    val first = calculateHit(battle, firstMoveUse, facts, statDisplay)
    val allHits = if (!first.isFullCalc) {
      // like the source, a move that didn't go through the damage formula (immunity, fixed damage) has no further hit logic
      List(hits) { first.result }
    } else {
      if (move.hitCount != HitCount.Once) facts.hits = hits
      AdditionalHits.hitsOf(battle, moveUse, hits, first, facts, statDisplay)
    }
    val calcFacts = facts.build()
    // like the source, the KO chance reads the Pokemon and field as the setup pass left them (e.g. a Klutz holder's item)
    val koChance = KoChanceCalculator.koChance(allHits.map { it.rolls }, move, battle.attacker, battle.defender, battle.field)
    return MoveDamageResult(allHits, calcFacts.format(statDisplay), calcFacts, koChance)
  }

  internal fun newFacts(battle: PreparedBattle, moveUse: MoveUse) =
    CalcFactsBuilder(battle.attacker.species.name, moveUse.move.name, battle.defender.species.name)

  /** Parental Bond's second hit is calculated as its "child", with no ability, like the source. */
  internal fun BattlePokemon.asParentalBondChild() = copy(ability = AbilityName(""))

  /**
   * One hit of an already prepared battle. [HitCalc.isFullCalc] is false when the move didn't go through the damage
   * formula, where the source returns early: a status move, an immunity, a fixed-damage move.
   */
  internal fun calculateHit(battle: PreparedBattle, moveUse: MoveUse, facts: CalcFactsBuilder, statDisplay: StatDisplay): HitCalc {
    val (attacker, defender, field) = battle
    val move = moveUse.move
    val effectiveCategory = effectiveCategoryOf(move, attacker, defender)
    val isQuarteredByProtect = isQuarteredByProtect(move, attacker, effectiveCategory, field)
    if (isQuarteredByProtect) facts.attackerAbility(attacker.resolvedAbility)
    if (move.category == MoveCategory.STATUS) {
      val rolls = if (move.name.value == PAIN_SPLIT) listOf(defender.hp - (defender.hp + attacker.hp) / 2) else listOf(0)
      return HitCalc(noDamage(rolls, typeEffectiveness = 1.0, facts, statDisplay), isFullCalc = false, hitsPhysical = false, effectiveCategory)
    }

    // Mold Breaker ignores the defender's ability (every one legal in Champions can be ignored)
    val defenderAbility = if (attacker.resolvedAbility == Ability.MOLD_BREAKER) {
      facts.attackerAbility(attacker.resolvedAbility)
      Ability.NO_ABILITY
    } else {
      defender.resolvedAbility
    }
    val isCritical = (moveUse.isCritical || move.alwaysCrits) && defenderAbility !in CRIT_BLOCKING_ABILITIES

    val effectiveType = effectiveTypeOf(move, attacker, field)
    if (isRetypedByLiquidVoice(move, attacker)) facts.attackerAbility(attacker.resolvedAbility)
    if (ImmunityChecker.isImmune(move, effectiveType, attacker, defender, field, facts, defenderAbility)) {
      return HitCalc(noDamage(listOf(0), typeEffectiveness = 0.0, facts, statDisplay), isFullCalc = false, hitsPhysical = false, effectiveCategory)
    }
    facts.hp = StatInvestment.of(defender, Stat.HP)

    val typeEffectiveness = TypeEffectivenessCalculator.effectivenessOf(move, effectiveType, attacker, defender, field)
    FixedDamage.rollsOf(battle, moveUse, effectiveCategory, facts, statDisplay)?.let { rolls ->
      return HitCalc(noDamage(rolls, typeEffectiveness, facts, statDisplay), isFullCalc = false, hitsPhysical = false, effectiveCategory)
    }

    val hitsPhysical = hitsPhysicalDefense(move, effectiveCategory)
    val basePower = resolveBasePower(move, effectiveType, moveUse, attacker, defender, field, facts, defenderAbility)
    val attack = resolveAttack(move, effectiveType, attacker, defender, isCritical, field, facts, defenderAbility)
    val defense = resolveDefense(move, attacker, defender, hitsPhysical, isCritical, field, facts, defenderAbility)
    val baseDamage = baseDamageFormula(basePower, attack, defense)

    val preRollDamage = applyPreRollModifiers(baseDamage, move, effectiveType, moveUse, attacker, defender, field, isCritical, facts)
    val stabMod = stabMultiplier(move, effectiveType, attacker)
    describeStab(move, effectiveType, attacker, facts)
    val burnHalves = isBurnHalved(move, attacker, effectiveCategory)
    facts.isBurned = burnHalves
    val finalMod = chainMods(FinalMods.resolve(move, effectiveType, attacker, defender, field, isCritical, typeEffectiveness, facts, defenderAbility))
    facts.isQuarteredByProtect = isQuarteredByProtect

    val rolls = (85..100).map { percent ->
      rollDamage(preRollDamage, percent, stabMod, typeEffectiveness, burnHalves, finalMod, isQuarteredByProtect)
    }.sorted()

    val calcFacts = facts.build()
    val result = DamageResult(
      rolls = rolls,
      typeEffectiveness = typeEffectiveness,
      isCritical = isCritical,
      attackStat = AttackStatResolver.attackStatSourceOf(move, attacker, defender),
      defenseStat = DefenseStatResolver.defenseStatOf(hitsPhysical),
      description = calcFacts.format(statDisplay),
      facts = calcFacts,
    )
    return HitCalc(result, isFullCalc = true, hitsPhysical, effectiveCategory)
  }

  private fun noDamage(rolls: List<Int>, typeEffectiveness: Double, facts: CalcFactsBuilder, statDisplay: StatDisplay): DamageResult {
    val calcFacts = facts.build()
    return DamageResult(rolls, typeEffectiveness, isCritical = false, description = calcFacts.format(statDisplay), facts = calcFacts)
  }

  private fun resolveBasePower(
    move: Move,
    effectiveType: PokeType,
    moveUse: MoveUse,
    attacker: BattlePokemon,
    defender: BattlePokemon,
    field: Battlefield,
    facts: CalcFactsBuilder,
    defenderAbility: Ability,
  ): Int {
    val power = BasePowerResolver.resolve(move, moveUse, attacker, defender, field, facts, defenderAbility)
    val mods = BasePowerMods.resolve(power, move, effectiveType, moveUse, attacker, defender, field, facts, defenderAbility)
    return maxOf(1, pokeRound(power * chainMods(mods), 0x1000))
  }

  private fun resolveAttack(
    move: Move,
    effectiveType: PokeType,
    attacker: BattlePokemon,
    defender: BattlePokemon,
    isCritical: Boolean,
    field: Battlefield,
    facts: CalcFactsBuilder,
    defenderAbility: Ability,
  ): Int {
    val attack = AttackStatResolver.resolve(move, attacker, defender, isCritical, facts, defenderAbility)
    val mods = AttackStatMods.resolve(move, effectiveType, attacker, defender, field, facts, defenderAbility)
    return maxOf(1, pokeRound(attack * chainMods(mods), 0x1000))
  }

  private fun resolveDefense(
    move: Move,
    attacker: BattlePokemon,
    defender: BattlePokemon,
    hitsPhysical: Boolean,
    isCritical: Boolean,
    field: Battlefield,
    facts: CalcFactsBuilder,
    defenderAbility: Ability,
  ): Int {
    val defense = DefenseStatResolver.resolve(move, attacker, defender, hitsPhysical, isCritical, field, facts)
    val mods = DefenseStatMods.resolve(defender, field, hitsPhysical, facts, defenderAbility)
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
    facts: CalcFactsBuilder,
  ): Int {
    var damage = baseDamage
    if (field.format != BattleFormat.SINGLES && isSpreadHit(move, attacker, field)) {
      damage = pokeRound(damage * 0xC00, 0x1000)
      facts.isSpread = true
    }
    if (moveUse.isSecondParentalBondHit) damage = pokeRound(damage * 0x0400, 0x1000)
    damage = applyWeatherMod(damage, effectiveType, attacker, field, facts)
    if (defender.isVulnerableFromGlaiveRush) {
      damage = pokeRound(damage * 0x2000, 0x1000)
      facts.isGlaiveMod = true
    }
    if (isCritical) {
      damage = floor(damage * 1.5).toInt()
      facts.isCritical = true
    }
    return damage
  }

  private fun applyWeatherMod(damage: Int, effectiveType: PokeType, attacker: BattlePokemon, field: Battlefield, facts: CalcFactsBuilder): Int {
    val boosted = (isSunActive(attacker, field) && effectiveType == PokeType.FIRE) ||
      (field.weather == Weather.RAIN && effectiveType == PokeType.WATER)
    if (boosted) {
      // the source credits Mega Sol over the weather whenever the attacker has it
      if (attacker.resolvedAbility == Ability.MEGA_SOL) facts.attackerAbility(attacker.resolvedAbility) else facts.weather(field.weather)
      return pokeRound(damage * 0x1800, 0x1000)
    }

    val weakened = (field.weather == Weather.SUN && effectiveType == PokeType.WATER) ||
      (field.weather == Weather.RAIN && effectiveType == PokeType.FIRE && attacker.resolvedAbility != Ability.MEGA_SOL)
    if (weakened) {
      facts.weather(field.weather)
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
  private fun isQuarteredByProtect(move: Move, attacker: BattlePokemon, effectiveCategory: MoveCategory, field: Battlefield): Boolean =
    field.defenderSide.isProtected &&
      (attacker.resolvedAbility == Ability.PIERCING_DRILL || attacker.resolvedAbility == Ability.UNSEEN_FIST) &&
      makesContact(move, attacker, effectiveCategory)

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

internal const val PAIN_SPLIT = "Pain Split"

/** One hit, with what the next hits of the move need to know about it. */
internal data class HitCalc(
  val result: DamageResult,
  val isFullCalc: Boolean,
  val hitsPhysical: Boolean,
  val effectiveCategory: MoveCategory,
)

/** The two hits of a Parental Bond attack: a full-power hit, then a weaker follow-up. */
data class ParentalBondHits(val firstHit: DamageResult, val secondHit: DamageResult) {
  val totalMinDamage: Int get() = firstHit.minDamage + secondHit.minDamage
  val totalMaxDamage: Int get() = firstHit.maxDamage + secondHit.maxDamage
}
