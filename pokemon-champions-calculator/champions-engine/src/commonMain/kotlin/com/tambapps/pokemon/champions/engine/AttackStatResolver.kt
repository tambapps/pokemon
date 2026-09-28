package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.data.MoveCategory

/** Ported from calcAttack, scoped to Champions. Resolves the (Special) Attack value a hit rolls damage off of, before [AttackStatMods]. */
internal object AttackStatResolver {

  /** Which stat a hit's attack is read from, and whose: Foul Play reads the defender's Attack, Body Press the attacker's Defense. */
  fun attackStatSourceOf(move: Move, attacker: BattlePokemon, defender: BattlePokemon) = AttackStatSource(
    stat = when {
      move.name.value == "Body Press" -> BoostableStat.DEFENSE
      effectiveCategoryOf(move, attacker, defender) == MoveCategory.PHYSICAL -> BoostableStat.ATTACK
      else -> BoostableStat.SPECIAL_ATTACK
    },
    isDefenderStat = move.name.value == "Foul Play",
  )

  fun resolve(move: Move, attacker: BattlePokemon, defender: BattlePokemon, isCritical: Boolean, description: DescriptionBuilder = DescriptionBuilder()): Int {
    val statSource = attackStatSourceOf(move, attacker, defender)
    val attackSource = if (statSource.isDefenderStat) defender else attacker
    val attackStat = statSource.stat
    description.attackStat = StatInvestment.of(attackSource, attackStat.toStat())
    description.usesOppAtkStat = statSource.isDefenderStat

    var attack = when {
      defender.resolvedAbility == Ability.UNAWARE && attackSource.boosts[attackStat] != 0 -> {
        description.defenderAbility(defender.resolvedAbility)
        description.attackBoost = attackSource.boosts[attackStat]
        attackSource.stats[attackStat.toStat()]
      }
      attackSource.boosts[attackStat] == 0 || (isCritical && attackSource.boosts[attackStat] < 0) ->
        attackSource.stats[attackStat.toStat()]
      else -> {
        description.attackBoost = attackSource.boosts[attackStat]
        attackSource.boostedStat(attackStat)
      }
    }

    if (attacker.resolvedAbility == Ability.HUSTLE && effectiveCategoryOf(move, attacker, defender) == MoveCategory.PHYSICAL) {
      attack = pokeRound(attack * 3.0 / 2)
      description.attackerAbility(attacker.resolvedAbility)
    }
    return attack
  }
}
