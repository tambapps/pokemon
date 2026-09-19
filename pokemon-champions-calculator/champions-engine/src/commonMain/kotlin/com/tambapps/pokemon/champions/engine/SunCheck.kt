package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.champions.data.Ability

/** Mega Sol acts as a personal, permanent Sun for its holder wherever the games check for sunny weather. */
fun isSunActive(attacker: BattlePokemon, field: Battlefield): Boolean =
  field.weather == Weather.SUN || attacker.resolvedAbility == Ability.MEGA_SOL
