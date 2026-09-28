package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.Gender
import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Item
import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.data.MoveCategory
import com.tambapps.pokemon.champions.engine.description.CalcFactsBuilder
import com.tambapps.pokemon.champions.engine.description.RivalryEffect

internal const val LASH_OUT = "Lash Out"

/** Ported from calcBPMods, scoped to Champions. Order matters: chainMods rounds after every step. */
internal object BasePowerMods {

  private val SUPREME_OVERLORD_BOOST = listOf(0x119A, 0x1333, 0x14CD, 0x1666, 0x1800)
  private val POISONED_STATUSES = setOf(Status.POISONED, Status.BADLY_POISONED)

  fun resolve(
    basePower: Int,
    move: Move,
    effectiveType: PokeType,
    moveUse: MoveUse,
    attacker: BattlePokemon,
    defender: BattlePokemon,
    field: Battlefield,
    facts: CalcFactsBuilder = CalcFactsBuilder(),
    defenderAbility: Ability = defender.resolvedAbility,
  ): List<Int> {
    val mods = mutableListOf<Int>()
    val effectiveCategory = effectiveCategoryOf(move, attacker, defender)

    rivalryMod(attacker, defender)?.let {
      mods.add(it)
      facts.attackerAbility(attacker.resolvedAbility)
      facts.rivalryEffect = if (it == 0x1400) RivalryEffect.SAME_GENDER else RivalryEffect.OPPOSITE_GENDER
    }
    offensiveBoostMod(move, attacker)?.let {
      mods.add(it)
      facts.attackerAbility(attacker.resolvedAbility)
    }
    if (field.attackerSide.hasBattery && effectiveCategory == MoveCategory.SPECIAL) {
      mods.add(0x14CD)
      facts.isBattery = true
    }
    if (field.attackerSide.hasPowerSpot) {
      mods.add(0x14CD)
      facts.isPowerSpot = true
    }
    if (field.attackerSide.hasAllySteelySpirit && effectiveType == PokeType.STEEL) {
      mods.add(0x1800)
      facts.isSteelySpirit = true
    }
    offensiveAbilityMod(move, effectiveType, effectiveCategory, attacker, defender, field)?.let {
      mods.add(it)
      facts.attackerAbility(attacker.resolvedAbility)
      if (attacker.resolvedAbility == Ability.SAND_FORCE) facts.weather(field.weather)
    }
    // Champions only offers Fairy Aura, not Dark Aura / Aura Break (hidden for this gen in the source calculator)
    if (field.isFairyAura && effectiveType == PokeType.FAIRY) {
      mods.add(0x1548)
      if (attacker.resolvedAbility == Ability.FAIRY_AURA) {
        facts.attackerAbility(attacker.resolvedAbility)
      } else if (defenderAbility == Ability.FAIRY_AURA) {
        facts.defenderAbility(defenderAbility)
      }
    }

    // Technician checks the power after every modifier above it, but before anything below.
    val powerSoFar = pokeRound(basePower * chainMods(mods), 0x1000)
    strongOffenseMod(move, effectiveType, attacker, powerSoFar)?.let {
      mods.add(it)
      facts.attackerAbility(attacker.resolvedAbility)
    }

    if (defenderAbility == Ability.DRY_SKIN && effectiveType == PokeType.FIRE) {
      mods.add(0x1400)
      facts.defenderAbility(defenderAbility)
    }
    itemPowerMod(effectiveType, attacker, effectiveCategory)?.let {
      mods.add(it)
      facts.attackerItem(attacker.effectiveItem)
    }
    if (weakensInWeather(move, attacker, field)) {
      mods.add(0x800)
      facts.moveBP = move.basePower / 2.0
      facts.weather(field.weather)
    }
    if (isOneAndHalfPowerMove(move, attacker, defender, field)) {
      mods.add(0x1800)
      facts.moveBP = move.basePower * 1.5
    }
    if (field.attackerSide.hasHelpingHand) {
      mods.add(0x1800)
      facts.isHelpingHand = true
    }
    if (chargeMod(effectiveType, attacker, field)) {
      mods.add(0x2000)
      facts.charged = true
    }
    if (isDoubledByCondition(move, moveUse, attacker, defender)) {
      mods.add(0x2000)
      facts.moveBP = move.basePower * 2.0
    }
    terrainOffenseMod(effectiveType, attacker, field)?.let {
      mods.add(it)
      facts.terrain(field.terrain)
    }
    terrainDefenseMod(move, effectiveType, defender, field)?.let {
      mods.add(it)
      facts.terrain(field.terrain)
    }
    supremeOverlordMod(attacker, moveUse)?.let {
      mods.add(it)
      facts.attackerAbility(attacker.resolvedAbility)
      facts.faintedAllies = moveUse.faintedAllyCount
    }

    return mods
  }

  /** Knock Off against a removable item, Grav Apple under Gravity, Misty Explosion and Expanding Force in their terrain. */
  private fun isOneAndHalfPowerMove(move: Move, attacker: BattlePokemon, defender: BattlePokemon, field: Battlefield): Boolean =
    when (move.name.value) {
      "Knock Off" -> canRemoveItem(defender)
      "Grav Apple" -> field.isGravity
      "Misty Explosion" -> field.terrain == Terrain.MISTY && attacker.isAttackerGrounded(field)
      "Expanding Force" -> field.terrain == Terrain.PSYCHIC && attacker.isAttackerGrounded(field)
      else -> false
    }

  // cantRemoveItem: no item, or a mega holding its own mega stone
  private fun canRemoveItem(defender: BattlePokemon): Boolean {
    if (!defender.holdsItem) return false
    if (defender.resolvedAbility == Ability.KLUTZ) return true
    val item = defender.item ?: return false
    return defender.species.megaStone?.let { item.matches(it) } != true
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

  private fun offensiveAbilityMod(
    move: Move,
    effectiveType: PokeType,
    effectiveCategory: MoveCategory,
    attacker: BattlePokemon,
    defender: BattlePokemon,
    field: Battlefield,
  ): Int? = when {
    attacker.resolvedAbility == Ability.SHEER_FORCE && move.hasSecondaryEffect -> 0x14CD
    attacker.resolvedAbility == Ability.SAND_FORCE && field.weather == Weather.SAND &&
      effectiveType in setOf(PokeType.ROCK, PokeType.GROUND, PokeType.STEEL) -> 0x14CD
    attacker.resolvedAbility == Ability.ANALYTIC && !attackerMovesFirst(attacker, defender, field) -> 0x14CD
    attacker.resolvedAbility == Ability.TOUGH_CLAWS && makesContact(move, attacker, effectiveCategory) -> 0x14CD
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
      item == Item.NORMAL_GEM && effectiveType == PokeType.NORMAL -> 0x14CD
      else -> null
    }
  }

  private fun weakensInWeather(move: Move, attacker: BattlePokemon, field: Battlefield): Boolean =
    move.name.value in setOf("Solar Beam", "Solar Blade") &&
      field.weather != Weather.NONE && field.weather != Weather.SUN &&
      attacker.resolvedAbility != Ability.MEGA_SOL

  private fun chargeMod(effectiveType: PokeType, attacker: BattlePokemon, field: Battlefield): Boolean =
    effectiveType == PokeType.ELECTRIC &&
      (field.attackerSide.isCharged || (attacker.resolvedAbility == Ability.ELECTROMORPHOSIS && attacker.abilityIsActive))

  private fun isDoubledByCondition(move: Move, moveUse: MoveUse, attacker: BattlePokemon, defender: BattlePokemon): Boolean = when (move.name.value) {
    "Facade" -> attacker.status.isNonHealthy
    "Venoshock", "Barb Barrage" -> defender.status in POISONED_STATUSES
    LASH_OUT -> moveUse.isPowerDoubled
    else -> false
  }

  private fun terrainOffenseMod(effectiveType: PokeType, attacker: BattlePokemon, field: Battlefield): Int? {
    if (!attacker.isAttackerGrounded(field)) return null
    val boosted = (field.terrain == Terrain.ELECTRIC && effectiveType == PokeType.ELECTRIC) ||
      (field.terrain == Terrain.GRASSY && effectiveType == PokeType.GRASS) ||
      (field.terrain == Terrain.PSYCHIC && effectiveType == PokeType.PSYCHIC)
    return if (boosted) 0x14CD else null
  }

  private fun terrainDefenseMod(move: Move, effectiveType: PokeType, defender: BattlePokemon, field: Battlefield): Int? {
    if (!defender.isDefenderGrounded(field)) return null
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
