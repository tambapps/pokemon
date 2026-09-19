package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Move

private val ATE_TYPE_BY_ABILITY = mapOf(
  Ability.AERILATE to PokeType.FLYING,
  Ability.PIXILATE to PokeType.FAIRY,
  Ability.REFRIGERATE to PokeType.ICE,
  Ability.DRAGONIZE to PokeType.DRAGON,
)

/** Moves that pick their type from the field/attacker rather than the games' generic "-ate" ability handling. */
private val EXEMPT_FROM_ATE_ABILITIES = setOf("Weather Ball", "Terrain Pulse")

/** Struggle is the games' one Typeless move: never boosted by STAB, never resisted or immune to anything. Since [PokeType] has no Typeless value, both effects are applied directly by name at their two call sites (see [TypeEffectivenessCalculator] and [stabMultiplier]) instead of through a fake type. */
const val STRUGGLE_MOVE_NAME = "Struggle"

/**
 * A handful of moves pick their type at the moment they're used rather than having a fixed
 * one; on top of that, a "-ate" ability or Liquid Voice can retype an eligible move entirely.
 * Ported from the relevant branches of checkMoveTypeChange and checkAbilityTypeChange.
 */
fun effectiveTypeOf(move: Move, attacker: BattlePokemon, field: Battlefield): PokeType {
  val fieldResolvedType = fieldResolvedTypeOf(move, attacker, field)
  if (move.name.value in EXEMPT_FROM_ATE_ABILITIES) return fieldResolvedType

  if (attacker.resolvedAbility == Ability.LIQUID_VOICE && move.isSound) return PokeType.WATER
  if (fieldResolvedType == PokeType.NORMAL) {
    ATE_TYPE_BY_ABILITY[attacker.resolvedAbility]?.let { return it }
  }
  return fieldResolvedType
}

/** True when a "-ate" ability retyped this move, which also grants it a 1.2x power boost. */
fun hasAteAbilityBoost(move: Move, attacker: BattlePokemon): Boolean =
  move.name.value !in EXEMPT_FROM_ATE_ABILITIES &&
    move.type == PokeType.NORMAL &&
    ATE_TYPE_BY_ABILITY.containsKey(attacker.resolvedAbility)

private fun fieldResolvedTypeOf(move: Move, attacker: BattlePokemon, field: Battlefield): PokeType = when (move.name.value) {
  "Weather Ball" -> when (field.weather) {
    Weather.SUN -> PokeType.FIRE
    Weather.RAIN -> PokeType.WATER
    Weather.SAND -> PokeType.ROCK
    Weather.HAIL, Weather.SNOW -> PokeType.ICE
    Weather.NONE -> PokeType.NORMAL
  }
  "Terrain Pulse" -> if (field.terrain == Terrain.NONE || !attacker.isGrounded(field)) {
    PokeType.NORMAL
  } else when (field.terrain) {
    Terrain.ELECTRIC -> PokeType.ELECTRIC
    Terrain.GRASSY -> PokeType.GRASS
    Terrain.MISTY -> PokeType.FAIRY
    Terrain.PSYCHIC -> PokeType.PSYCHIC
    Terrain.NONE -> PokeType.NORMAL
  }
  "Raging Bull" -> when (attacker.species.name.value) {
    "Tauros-Paldea-Combat" -> PokeType.FIGHTING
    "Tauros-Paldea-Blaze" -> PokeType.FIRE
    "Tauros-Paldea-Aqua" -> PokeType.WATER
    else -> PokeType.NORMAL
  }
  else -> move.type
}
