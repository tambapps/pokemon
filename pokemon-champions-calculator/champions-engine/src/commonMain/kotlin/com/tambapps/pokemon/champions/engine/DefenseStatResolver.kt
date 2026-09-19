package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Move

/** Ported from calcDefense, scoped to Champions. Resolves the (Special) Defense value a hit rolls damage off of, before [DefenseStatMods]. */
internal object DefenseStatResolver {

  fun resolve(move: Move, attacker: BattlePokemon, defender: BattlePokemon, hitsPhysical: Boolean, isCritical: Boolean, field: Battlefield): Int {
    val defenseStat = if (hitsPhysical) BoostableStat.DEFENSE else BoostableStat.SPECIAL_DEFENSE
    val boost = defender.boosts[defenseStat]

    var defense = when {
      attacker.resolvedAbility == Ability.UNAWARE && boost != 0 -> defender.stats[defenseStat.toStat()]
      move.ignoresDefenseBoosts && boost != 0 -> defender.stats[defenseStat.toStat()]
      boost == 0 || (isCritical && boost > 0) -> defender.stats[defenseStat.toStat()]
      else -> defender.boostedStat(defenseStat)
    }

    val roughTerrainBoost = (field.weather == Weather.SAND && defender.hasType(PokeType.ROCK) && !hitsPhysical) ||
      (field.weather == Weather.SNOW && defender.hasType(PokeType.ICE) && hitsPhysical)
    if (roughTerrainBoost && attacker.resolvedAbility != Ability.MEGA_SOL) {
      defense = pokeRound(defense * 3.0 / 2)
    }
    return defense
  }
}
