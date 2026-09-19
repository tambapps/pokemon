package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.data.MoveCategory

/** Ported from calcAttack, scoped to Champions. Resolves the (Special) Attack value a hit rolls damage off of, before [AttackStatMods]. */
internal object AttackStatResolver {

  fun resolve(move: Move, attacker: BattlePokemon, defender: BattlePokemon, isCritical: Boolean): Int {
    val attackSource = if (move.name.value == "Foul Play") defender else attacker
    val attackStat = when {
      move.name.value == "Body Press" -> BoostableStat.DEFENSE
      effectiveCategoryOf(move, attacker, defender) == MoveCategory.PHYSICAL -> BoostableStat.ATTACK
      else -> BoostableStat.SPECIAL_ATTACK
    }

    var attack = when {
      defender.resolvedAbility == Ability.UNAWARE && attackSource.boosts[attackStat] != 0 ->
        attackSource.stats[attackStat.toStat()]
      attackSource.boosts[attackStat] == 0 || (isCritical && attackSource.boosts[attackStat] < 0) ->
        attackSource.stats[attackStat.toStat()]
      else -> attackSource.boostedStat(attackStat)
    }

    if (attacker.resolvedAbility == Ability.HUSTLE && effectiveCategoryOf(move, attacker, defender) == MoveCategory.PHYSICAL) {
      attack = pokeRound(attack * 3.0 / 2)
    }
    return attack
  }
}
