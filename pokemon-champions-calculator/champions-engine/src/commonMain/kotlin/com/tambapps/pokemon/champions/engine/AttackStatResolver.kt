package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.data.MoveCategory
import com.tambapps.pokemon.champions.engine.description.CalcFactsBuilder
import com.tambapps.pokemon.champions.engine.description.StatInvestment

/** Ported from calcAttack, scoped to Champions. Resolves the (Special) Attack value a hit rolls damage off of, before [AttackStatMods]. */
internal object AttackStatResolver {

  private val MID_MOVE_BOOST_MOVES = setOf("Meteor Beam", "Electro Shot")

  /** Which stat a hit's attack is read from, and whose: Foul Play reads the defender's Attack, Body Press the attacker's Defense. */
  fun attackStatSourceOf(move: Move, attacker: BattlePokemon, defender: BattlePokemon) = AttackStatSource(
    stat = when {
      move.name.value == "Body Press" -> BoostableStat.DEFENSE
      effectiveCategoryOf(move, attacker, defender) == MoveCategory.PHYSICAL -> BoostableStat.ATTACK
      else -> BoostableStat.SPECIAL_ATTACK
    },
    isDefenderStat = move.name.value == "Foul Play",
  )

  fun resolve(
    move: Move,
    attacker: BattlePokemon,
    defender: BattlePokemon,
    isCritical: Boolean,
    facts: CalcFactsBuilder = CalcFactsBuilder(),
    defenderAbility: Ability = defender.resolvedAbility,
  ): Int {
    val statSource = attackStatSourceOf(move, attacker, defender)
    val attackSource = if (statSource.isDefenderStat) defender else attacker
    val attackStat = statSource.stat
    facts.attackStat = StatInvestment.of(attackSource, attackStat.toStat())
    facts.usesOppAtkStat = statSource.isDefenderStat

    // Meteor Beam and Electro Shot raise the user's Sp. Atk before hitting (lower it with Contrary), with the source's bounds
    val sourceBoost = attackSource.boosts[attackStat]
    val contrary = if (attacker.resolvedAbility == Ability.CONTRARY) -1 else 1
    val isMidMoveBoost = move.name.value in MID_MOVE_BOOST_MOVES && ((contrary == -1 && sourceBoost > -6) || sourceBoost < 6)
    val boost = if (isMidMoveBoost) sourceBoost + contrary else sourceBoost
    val rawStat = attackSource.stats[attackStat.toStat()]

    var attack = when {
      defenderAbility == Ability.UNAWARE && boost != 0 -> {
        facts.defenderAbility(defenderAbility)
        facts.attackBoost = boost
        rawStat
      }
      isMidMoveBoost -> {
        facts.attackBoost = boost
        if (boost == 0 || (isCritical && boost < 0)) rawStat else applyBoostStage(rawStat, boost)
      }
      boost == 0 || (isCritical && boost < 0) -> rawStat
      else -> {
        facts.attackBoost = boost
        attackSource.boostedStat(attackStat)
      }
    }

    if (attacker.resolvedAbility == Ability.HUSTLE && effectiveCategoryOf(move, attacker, defender) == MoveCategory.PHYSICAL) {
      attack = pokeRound(attack * 3.0 / 2)
      facts.attackerAbility(attacker.resolvedAbility)
    }
    return attack
  }
}
