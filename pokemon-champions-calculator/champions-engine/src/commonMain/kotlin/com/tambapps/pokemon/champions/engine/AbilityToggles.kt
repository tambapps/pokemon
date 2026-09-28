package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.champions.data.Ability

// The source calculator's ABILITY_TOGGLE_ON / ABILITY_TOGGLE_OFF (ap_calc.js), scoped to Champions
private val ABILITIES_ACTIVE_BY_DEFAULT = setOf(
  Ability.INTIMIDATE, Ability.PROTEAN, Ability.LIBERO, Ability.SUPERSWEET_SYRUP,
)
private val ABILITIES_INACTIVE_BY_DEFAULT = setOf(
  Ability.FLASH_FIRE, Ability.PLUS, Ability.MINUS, Ability.TRACE, Ability.STAKEOUT,
  Ability.SAND_SPIT, Ability.ELECTROMORPHOSIS, Ability.SEED_SOWER,
)

// The source calculator's setField (ap_calc.js) auto abilities, scoped to Champions
private val WEATHER_ABILITIES = mapOf(
  Ability.DROUGHT to Weather.SUN,
  Ability.DRIZZLE to Weather.RAIN,
  Ability.SAND_STREAM to Weather.SAND,
  Ability.SNOW_WARNING to Weather.SNOW,
)
private val TERRAIN_ABILITIES = mapOf(
  Ability.GRASSY_SURGE to Terrain.GRASSY,
  Ability.ELECTRIC_SURGE to Terrain.ELECTRIC,
  Ability.PSYCHIC_SURGE to Terrain.PSYCHIC,
)

/**
 * Whether this ability has the source calculator's "ability on" toggle, i.e. whether
 * [BattlePokemon.abilityIsActive] matters for it (e.g. Flash Fire once triggered, Intimidate on switch-in).
 */
val Ability.hasActiveToggle: Boolean
  get() = this in ABILITIES_ACTIVE_BY_DEFAULT || this in ABILITIES_INACTIVE_BY_DEFAULT

/** The default of this ability's toggle in the source calculator (e.g. true for Intimidate), false if it has none. */
val Ability.isActiveByDefault: Boolean
  get() = this in ABILITIES_ACTIVE_BY_DEFAULT

/**
 * The weather the source calculator sets on the field when a Pokemon with this ability is in the calc
 * (e.g. Drought -> Sun), or null. Sand Spit only sets it when [isActive].
 */
fun Ability.weatherSetOnField(isActive: Boolean): Weather? = when {
  this == Ability.SAND_SPIT -> if (isActive) Weather.SAND else null
  else -> WEATHER_ABILITIES[this]
}

/**
 * The terrain the source calculator sets on the field when a Pokemon with this ability is in the calc
 * (e.g. Grassy Surge -> Grassy), or null. Seed Sower only sets it when [isActive].
 */
fun Ability.terrainSetOnField(isActive: Boolean): Terrain? = when {
  this == Ability.SEED_SOWER -> if (isActive) Terrain.GRASSY else null
  else -> TERRAIN_ABILITIES[this]
}
