package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.engine.description.CalcFactsBuilder
import com.tambapps.pokemon.champions.engine.description.StatInvestment

/** Ported from calcDefense, scoped to Champions. Resolves the (Special) Defense value a hit rolls damage off of, before [DefenseStatMods]. */
internal object DefenseStatResolver {

  /** Which of the defender's stats a hit's defense is read from. */
  fun defenseStatOf(hitsPhysical: Boolean): BoostableStat =
    if (hitsPhysical) BoostableStat.DEFENSE else BoostableStat.SPECIAL_DEFENSE

  fun resolve(
    move: Move,
    attacker: BattlePokemon,
    defender: BattlePokemon,
    hitsPhysical: Boolean,
    isCritical: Boolean,
    field: Battlefield,
    facts: CalcFactsBuilder = CalcFactsBuilder(),
  ): Int {
    val defenseStat = defenseStatOf(hitsPhysical)
    val boost = defender.boosts[defenseStat]
    facts.defenseStat = StatInvestment.of(defender, defenseStat.toStat())

    var defense = when {
      attacker.resolvedAbility == Ability.UNAWARE && boost != 0 -> {
        facts.attackerAbility(attacker.resolvedAbility)
        facts.defenseBoost = boost
        defender.stats[defenseStat.toStat()]
      }
      move.ignoresDefenseBoosts && boost != 0 -> {
        facts.defenseBoost = boost
        defender.stats[defenseStat.toStat()]
      }
      boost == 0 || (isCritical && boost > 0) -> defender.stats[defenseStat.toStat()]
      else -> {
        facts.defenseBoost = boost
        defender.boostedStat(defenseStat)
      }
    }

    val roughTerrainBoost = (field.weather == Weather.SAND && defender.hasType(PokeType.ROCK) && !hitsPhysical) ||
      (field.weather == Weather.SNOW && defender.hasType(PokeType.ICE) && hitsPhysical)
    if (roughTerrainBoost && attacker.resolvedAbility != Ability.MEGA_SOL) {
      defense = pokeRound(defense * 3.0 / 2)
      facts.weather(field.weather)
    }
    return defense
  }
}
