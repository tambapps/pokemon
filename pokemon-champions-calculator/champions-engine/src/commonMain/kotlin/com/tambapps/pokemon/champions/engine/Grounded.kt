package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Item

/** Whether terrain, Grassy Glide's priority, Misty/Psychic Terrain and Ground-move immunities apply to this Pokemon. */
fun BattlePokemon.isGrounded(field: Battlefield): Boolean {
  if (field.isGravity || effectiveItem == Item.IRON_BALL) return true
  val isAirborne = effectiveItem == Item.AIR_BALLOON ||
    resolvedAbility == Ability.LEVITATE || resolvedAbility == Ability.EELEVATE ||
    hasType(PokeType.FLYING)
  return !isAirborne
}
