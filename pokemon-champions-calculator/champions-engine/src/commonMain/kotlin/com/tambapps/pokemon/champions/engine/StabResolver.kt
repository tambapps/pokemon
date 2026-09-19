package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Move

/**
 * Same-Type Attack Bonus, as a fixed-point 0x1000-scaled multiplier. Ported from the
 * non-Terastal branch of calcGeneralMods's STAB section -- Champions has no Terastallization.
 * Struggle (the games' one Typeless move) never gets STAB, checked by name since [PokeType] has no Typeless value to compare against.
 */
fun stabMultiplier(move: Move, effectiveType: PokeType, attacker: BattlePokemon): Int = when {
  move.name.value == STRUGGLE_MOVE_NAME -> 0x1000
  attacker.hasType(effectiveType) -> if (attacker.resolvedAbility == Ability.ADAPTABILITY) 0x2000 else 0x1800
  (attacker.resolvedAbility == Ability.PROTEAN || attacker.resolvedAbility == Ability.LIBERO) && attacker.abilityIsActive -> 0x1800
  else -> 0x1000
}
