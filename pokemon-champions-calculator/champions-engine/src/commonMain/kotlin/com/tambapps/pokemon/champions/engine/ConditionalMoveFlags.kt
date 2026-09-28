package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.data.MoveCategory

/** Whether the move makes contact this time, ported from checkContactOverride: Long Reach removes it, a physical Shell Side Arm adds it. */
internal fun makesContact(move: Move, attacker: BattlePokemon, effectiveCategory: MoveCategory): Boolean = when {
  move.makesContact && attacker.resolvedAbility == Ability.LONG_REACH -> false
  move.name.value == "Shell Side Arm" && effectiveCategory == MoveCategory.PHYSICAL -> true
  else -> move.makesContact
}

/** Whether the move hits every opponent, ported from checkConditionalSpread: Expanding Force does in Psychic Terrain. */
internal fun isSpreadHit(move: Move, attacker: BattlePokemon, field: Battlefield): Boolean =
  move.isSpread || (move.name.value == "Expanding Force" && field.terrain == Terrain.PSYCHIC && attacker.isGrounded(field))

/**
 * Whether the move has priority this time, ported from checkConditionalPriority: Grassy Glide in Grassy Terrain, and
 * Gale Wings' Flying-type moves at full HP.
 */
internal fun hasEffectivePriority(move: Move, attacker: BattlePokemon, field: Battlefield): Boolean =
  move.hasPriority ||
    (move.name.value == "Grassy Glide" && field.terrain == Terrain.GRASSY && attacker.isGrounded(field)) ||
    (attacker.resolvedAbility == Ability.GALE_WINGS && fieldResolvedTypeOf(move, attacker, field) == PokeType.FLYING && attacker.hp == attacker.maxHp)
