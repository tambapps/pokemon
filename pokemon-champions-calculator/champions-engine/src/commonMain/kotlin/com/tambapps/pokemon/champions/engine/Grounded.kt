package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Item

/**
 * Whether terrain, Grassy Glide's priority, Misty/Psychic Terrain, Ground-move immunities and Spikes apply to this
 * Pokemon. [side] is this Pokemon's own side of the field, whose Ingrain grounds it. The source calculator reads the
 * defender's side for both Pokemon of a calc, so there the defender's Ingrain also grounds the attacker, and the
 * attacker's own doesn't: this port grounds each Pokemon by its own side's Ingrain instead, as the games do.
 */
fun BattlePokemon.isGrounded(field: Battlefield, side: SideConditions): Boolean {
  if (field.isGravity || effectiveItem == Item.IRON_BALL || side.isIngrained) return true
  val isAirborne = effectiveItem == Item.AIR_BALLOON ||
    resolvedAbility == Ability.LEVITATE || resolvedAbility == Ability.EELEVATE ||
    hasType(PokeType.FLYING)
  return !isAirborne
}

/** [isGrounded] for the attacker of a calc, grounded by its side's Ingrain. */
internal fun BattlePokemon.isAttackerGrounded(field: Battlefield): Boolean = isGrounded(field, field.attackerSide)

/** [isGrounded] for the defender of a calc, grounded by its side's Ingrain. */
internal fun BattlePokemon.isDefenderGrounded(field: Battlefield): Boolean = isGrounded(field, field.defenderSide)
