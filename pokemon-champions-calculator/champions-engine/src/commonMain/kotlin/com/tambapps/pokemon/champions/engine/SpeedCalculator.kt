package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Item

/** Ported from getFinalSpeed in NCP-VGC-Damage-Calculator, scoped to abilities/items legal in Champions. */
object SpeedCalculator {

  fun effectiveSpeed(pokemon: BattlePokemon, field: Battlefield, isAttackerSide: Boolean): Int {
    val boosted = pokemon.boostedStat(BoostableStat.SPEED)
    val side = if (isAttackerSide) field.attackerSide else field.defenderSide

    var multiplier = 1.0
    multiplier *= itemSpeedMultiplier(pokemon.effectiveItem)
    multiplier *= abilitySpeedMultiplier(pokemon, field)
    if (side.hasTailwind) multiplier *= 2.0

    var speed = pokeRound(boosted * multiplier)

    if (pokemon.status == Status.PARALYZED && pokemon.resolvedAbility != Ability.QUICK_FEET) {
      speed /= 2
    }
    if (speed > 65535) speed %= 65536
    return speed.coerceAtMost(10000)
  }

  private fun itemSpeedMultiplier(item: Item?): Double = when (item) {
    Item.CHOICE_SCARF -> 1.5
    Item.IRON_BALL -> 0.5
    else -> 1.0
  }

  private fun abilitySpeedMultiplier(pokemon: BattlePokemon, field: Battlefield): Double = when {
    pokemon.resolvedAbility == Ability.QUICK_FEET && pokemon.status.isNonHealthy -> 1.5
    pokemon.resolvedAbility == Ability.CHLOROPHYLL && field.weather == Weather.SUN -> 2.0
    pokemon.resolvedAbility == Ability.SWIFT_SWIM && field.weather == Weather.RAIN -> 2.0
    pokemon.resolvedAbility == Ability.SAND_RUSH && field.weather == Weather.SAND -> 2.0
    pokemon.resolvedAbility == Ability.SLUSH_RUSH && (field.weather == Weather.HAIL || field.weather == Weather.SNOW) -> 2.0
    pokemon.resolvedAbility == Ability.SURGE_SURFER && field.terrain == Terrain.ELECTRIC -> 2.0
    pokemon.resolvedAbility == Ability.UNBURDEN && pokemon.item == null -> 2.0
    else -> 1.0
  }
}
