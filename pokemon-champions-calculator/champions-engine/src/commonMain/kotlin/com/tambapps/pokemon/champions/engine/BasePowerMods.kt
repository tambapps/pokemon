package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.Gender
import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Item
import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.data.MoveCategory

/** Ported from calcBPMods, scoped to Champions. Order matters: chainMods rounds after every step. */
internal object BasePowerMods {

  private val SUPREME_OVERLORD_BOOST = listOf(0x119A, 0x1333, 0x14CD, 0x1666, 0x1800)
  private val POISONED_STATUSES = setOf(Status.POISONED, Status.BADLY_POISONED)

  fun resolve(basePower: Int, move: Move, effectiveType: PokeType, moveUse: MoveUse, attacker: BattlePokemon, defender: BattlePokemon, field: Battlefield): List<Int> {
    val mods = mutableListOf<Int>()
    val effectiveCategory = effectiveCategoryOf(move, attacker, defender)

    rivalryMod(attacker, defender)?.let(mods::add)
    offensiveBoostMod(move, attacker)?.let(mods::add)
    if (field.attackerSide.hasBattery && effectiveCategory == MoveCategory.SPECIAL) mods.add(0x14CD)
    if (field.attackerSide.hasPowerSpot) mods.add(0x14CD)
    if (field.attackerSide.hasAllySteelySpirit && effectiveType == PokeType.STEEL) mods.add(0x1800)
    offensiveAbilityMod(move, effectiveType, attacker, defender, field)?.let(mods::add)

    // Technician checks the power after every modifier above it, but before anything below.
    val powerSoFar = pokeRound(basePower * chainMods(mods), 0x1000)
    strongOffenseMod(move, effectiveType, attacker, powerSoFar)?.let(mods::add)

    if (defender.resolvedAbility == Ability.DRY_SKIN && effectiveType == PokeType.FIRE) mods.add(0x1400)
    itemPowerMod(effectiveType, attacker, effectiveCategory)?.let(mods::add)
    if (weakensInWeather(move, attacker, field)) mods.add(0x800)
    if (field.attackerSide.hasHelpingHand) mods.add(0x1800)
    if (chargeMod(effectiveType, attacker, field)) mods.add(0x2000)
    if (isDoubledByCondition(move, attacker, defender)) mods.add(0x2000)
    terrainOffenseMod(effectiveType, attacker, field)?.let(mods::add)
    terrainDefenseMod(move, effectiveType, defender, field)?.let(mods::add)
    supremeOverlordMod(attacker, moveUse)?.let(mods::add)
    if (move.name.value == "Knock Off" && defender.effectiveItem != null) mods.add(0x1800)

    return mods
  }

  private fun rivalryMod(attacker: BattlePokemon, defender: BattlePokemon): Int? {
    if (attacker.resolvedAbility != Ability.RIVALRY) return null
    if (attacker.gender == Gender.ASEXUAL || defender.gender == Gender.ASEXUAL) return null
    return if (attacker.gender == defender.gender) 0x1400 else 0x0C00
  }

  private fun offensiveBoostMod(move: Move, attacker: BattlePokemon): Int? = when {
    hasAteAbilityBoost(move, attacker) -> 0x1333
    attacker.resolvedAbility == Ability.RECKLESS && move.hasRecoil -> 0x1333
    attacker.resolvedAbility == Ability.IRON_FIST && move.isPunch -> 0x1333
    else -> null
  }

  private fun offensiveAbilityMod(move: Move, effectiveType: PokeType, attacker: BattlePokemon, defender: BattlePokemon, field: Battlefield): Int? = when {
    attacker.resolvedAbility == Ability.SHEER_FORCE && move.hasSecondaryEffect -> 0x14CD
    attacker.resolvedAbility == Ability.SAND_FORCE && field.weather == Weather.SAND &&
      effectiveType in setOf(PokeType.ROCK, PokeType.GROUND, PokeType.STEEL) -> 0x14CD
    attacker.resolvedAbility == Ability.ANALYTIC && !attackerMovesFirst(attacker, defender, field) -> 0x14CD
    attacker.resolvedAbility == Ability.TOUGH_CLAWS && move.makesContact -> 0x14CD
    attacker.resolvedAbility == Ability.PUNK_ROCK && move.isSound -> 0x14CD
    else -> null
  }

  private fun strongOffenseMod(move: Move, effectiveType: PokeType, attacker: BattlePokemon, powerSoFar: Int): Int? {
    val qualifies = (attacker.resolvedAbility == Ability.TECHNICIAN && powerSoFar <= 60) ||
      (attacker.resolvedAbility == Ability.MEGA_LAUNCHER && move.isPulse) ||
      (attacker.resolvedAbility == Ability.STRONG_JAW && move.isBite) ||
      (attacker.resolvedAbility == Ability.STEELY_SPIRIT && effectiveType == PokeType.STEEL)
    return if (qualifies) 0x1800 else null
  }

  private fun itemPowerMod(effectiveType: PokeType, attacker: BattlePokemon, effectiveCategory: MoveCategory): Int? {
    val item = attacker.effectiveItem
    return when {
      item == Item.MUSCLE_BAND && effectiveCategory == MoveCategory.PHYSICAL -> 0x1199
      item == Item.WISE_GLASSES && effectiveCategory == MoveCategory.SPECIAL -> 0x1199
      boostsType(item, effectiveType) -> 0x1333
      else -> null
    }
  }

  private fun weakensInWeather(move: Move, attacker: BattlePokemon, field: Battlefield): Boolean =
    move.name.value in setOf("Solar Beam", "Solar Blade") &&
      field.weather != Weather.NONE && field.weather != Weather.SUN &&
      attacker.resolvedAbility != Ability.MEGA_SOL

  private fun chargeMod(effectiveType: PokeType, attacker: BattlePokemon, field: Battlefield): Boolean =
    effectiveType == PokeType.ELECTRIC &&
      (field.isCharge || (attacker.resolvedAbility == Ability.ELECTROMORPHOSIS && attacker.abilityIsActive))

  private fun isDoubledByCondition(move: Move, attacker: BattlePokemon, defender: BattlePokemon): Boolean = when (move.name.value) {
    "Facade" -> attacker.status.isNonHealthy
    "Venoshock", "Barb Barrage" -> defender.status in POISONED_STATUSES
    else -> false
  }

  private fun terrainOffenseMod(effectiveType: PokeType, attacker: BattlePokemon, field: Battlefield): Int? {
    if (!attacker.isGrounded(field)) return null
    val boosted = (field.terrain == Terrain.ELECTRIC && effectiveType == PokeType.ELECTRIC) ||
      (field.terrain == Terrain.GRASSY && effectiveType == PokeType.GRASS) ||
      (field.terrain == Terrain.PSYCHIC && effectiveType == PokeType.PSYCHIC)
    return if (boosted) 0x14CD else null
  }

  private fun terrainDefenseMod(move: Move, effectiveType: PokeType, defender: BattlePokemon, field: Battlefield): Int? {
    if (!defender.isGrounded(field)) return null
    val weakened = (field.terrain == Terrain.MISTY && effectiveType == PokeType.DRAGON) ||
      (field.terrain == Terrain.GRASSY && move.name.value in setOf("Earthquake", "Bulldoze"))
    return if (weakened) 0x800 else null
  }

  private fun supremeOverlordMod(attacker: BattlePokemon, moveUse: MoveUse): Int? {
    if (attacker.resolvedAbility != Ability.SUPREME_OVERLORD || moveUse.faintedAllyCount == 0) return null
    return SUPREME_OVERLORD_BOOST[moveUse.faintedAllyCount - 1]
  }

  private fun attackerMovesFirst(attacker: BattlePokemon, defender: BattlePokemon, field: Battlefield): Boolean =
    SpeedCalculator.effectiveSpeed(attacker, field, isAttackerSide = true) >
      SpeedCalculator.effectiveSpeed(defender, field, isAttackerSide = false)
}
