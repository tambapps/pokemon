package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.data.MoveCategory
import com.tambapps.pokemon.PokeType
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

  /** One hit of [moveUse] against [defender]. For multi-hit moves, call this once per hit -- see [calculateParentalBondHits]. */
  fun calculateSingleHit(attacker: BattlePokemon, defender: BattlePokemon, moveUse: MoveUse, field: Battlefield): DamageResult {
    val move = moveUse.move
    if (move.category == MoveCategory.STATUS) return DamageResult.noDamage()

    val effectiveType = effectiveTypeOf(move, attacker, field)
    if (ImmunityChecker.isImmune(move, effectiveType, attacker, defender, field)) {
      return DamageResult.noDamage(typeEffectiveness = 0.0)
    }

    val typeEffectiveness = TypeEffectivenessCalculator.effectivenessOf(move, effectiveType, defender, field)
    val effectiveCategory = effectiveCategoryOf(move, attacker, defender)
    val hitsPhysical = hitsPhysicalDefense(move, effectiveCategory)
    val isCritical = moveUse.isCritical || move.alwaysCrits

    val basePower = resolveBasePower(move, effectiveType, moveUse, attacker, defender, field)
    val attack = resolveAttack(move, effectiveType, attacker, defender, isCritical, field)
    val defense = resolveDefense(move, effectiveType, attacker, defender, hitsPhysical, isCritical, field)
    val baseDamage = baseDamageFormula(basePower, attack, defense)

    val preRollDamage = applyPreRollModifiers(baseDamage, move, effectiveType, moveUse, attacker, defender, field, isCritical)
    val stabMod = stabMultiplier(move, effectiveType, attacker)
    val burnHalves = isBurnHalved(move, attacker, effectiveCategory)
    val finalMod = chainMods(FinalMods.resolve(move, effectiveType, attacker, defender, field, isCritical, typeEffectiveness))
    val isQuarteredByProtect = isQuarteredByProtect(move, attacker, field)

    val rolls = (85..100).map { percent ->
      rollDamage(preRollDamage, percent, stabMod, typeEffectiveness, burnHalves, finalMod, isQuarteredByProtect)
    }.sorted()

    return DamageResult(rolls, typeEffectiveness, isCritical)
  }

  /** Parental Bond always hits twice: a full-power hit, then a second hit at a quarter of that base damage. */
  fun calculateParentalBondHits(attacker: BattlePokemon, defender: BattlePokemon, moveUse: MoveUse, field: Battlefield): ParentalBondHits {
    require(attacker.resolvedAbility == Ability.PARENTAL_BOND) { "calculateParentalBondHits requires the attacker to have Parental Bond" }
    val firstHit = calculateSingleHit(attacker, defender, moveUse.copy(isSecondParentalBondHit = false), field)
    val secondHit = calculateSingleHit(attacker, defender, moveUse.copy(isSecondParentalBondHit = true), field)
    return ParentalBondHits(firstHit, secondHit)
  }

  private fun resolveBasePower(move: Move, effectiveType: PokeType, moveUse: MoveUse, attacker: BattlePokemon, defender: BattlePokemon, field: Battlefield): Int {
    val power = BasePowerResolver.resolve(move, moveUse, attacker, defender, field)
    val mods = BasePowerMods.resolve(power, move, effectiveType, moveUse, attacker, defender, field)
    return maxOf(1, pokeRound(power * chainMods(mods), 0x1000))
  }

  private fun resolveAttack(move: Move, effectiveType: PokeType, attacker: BattlePokemon, defender: BattlePokemon, isCritical: Boolean, field: Battlefield): Int {
    val attack = AttackStatResolver.resolve(move, attacker, defender, isCritical)
    val mods = AttackStatMods.resolve(move, effectiveType, attacker, defender, field)
    return maxOf(1, pokeRound(attack * chainMods(mods), 0x1000))
  }

  private fun resolveDefense(move: Move, effectiveType: PokeType, attacker: BattlePokemon, defender: BattlePokemon, hitsPhysical: Boolean, isCritical: Boolean, field: Battlefield): Int {
    val defense = DefenseStatResolver.resolve(move, attacker, defender, hitsPhysical, isCritical, field)
    val mods = DefenseStatMods.resolve(defender, field, hitsPhysical)
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
  ): Int {
    var damage = baseDamage
    if (field.format != BattleFormat.SINGLES && move.isSpread) damage = pokeRound(damage * 0xC00, 0x1000)
    if (moveUse.isSecondParentalBondHit) damage = pokeRound(damage * 0x0400, 0x1000)
    damage = applyWeatherMod(damage, effectiveType, attacker, field)
    if (defender.isVulnerableFromGlaiveRush) damage = pokeRound(damage * 0x2000, 0x1000)
    if (isCritical) damage = floor(damage * 1.5).toInt()
    return damage
  }

  private fun applyWeatherMod(damage: Int, effectiveType: PokeType, attacker: BattlePokemon, field: Battlefield): Int {
    val boosted = (isSunActive(attacker, field) && effectiveType == PokeType.FIRE) ||
      (field.weather == Weather.RAIN && effectiveType == PokeType.WATER)
    if (boosted) return pokeRound(damage * 0x1800, 0x1000)

    val weakened = (field.weather == Weather.SUN && effectiveType == PokeType.WATER) ||
      (field.weather == Weather.RAIN && effectiveType == PokeType.FIRE && attacker.resolvedAbility != Ability.MEGA_SOL)
    if (weakened) return pokeRound(damage * 0x800, 0x1000)

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
