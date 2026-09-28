package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.data.MoveCategory
import com.tambapps.pokemon.champions.engine.description.CalcFactsBuilder
import com.tambapps.pokemon.champions.engine.description.CounterMultiplier
import com.tambapps.pokemon.champions.engine.description.CounteredMove
import com.tambapps.pokemon.champions.engine.description.StatDisplay

/**
 * Ported from setDamage: the moves whose damage doesn't come from the damage formula, scoped to Champions.
 * Like the source, Parental Bond doubles some of them in a single hit instead of hitting twice.
 */
/**
 * Counter, Mirror Coat, Metal Burst, Comeuppance: moves returning the damage of one of the defender's moves, which the
 * caller reports via [MoveUse.counteredMove]
 */
val Move.returnsDefenderMove: Boolean get() = name.value in FixedDamage.COUNTER_MOVES

internal object FixedDamage {

  internal val COUNTER_MOVES = mapOf(
    "Counter" to CounterMultiplier.DOUBLE,
    "Mirror Coat" to CounterMultiplier.DOUBLE,
    "Metal Burst" to CounterMultiplier.ONE_AND_A_HALF,
    "Comeuppance" to CounterMultiplier.ONE_AND_A_HALF,
  )

  /** The damage of [moveUse] if it's a fixed-damage move, else null. */
  fun rollsOf(
    battle: PreparedBattle,
    moveUse: MoveUse,
    effectiveCategory: MoveCategory,
    facts: CalcFactsBuilder,
    statDisplay: StatDisplay,
  ): List<Int>? {
    val (attacker, defender, _) = battle
    val move = moveUse.move
    val isParentalBond = attacker.resolvedAbility == Ability.PARENTAL_BOND
    COUNTER_MOVES[move.name.value]?.let { multiplier -> return counterRolls(battle, moveUse, multiplier, effectiveCategory, facts, statDisplay) }
    return when {
      move.name.value == "Super Fang" -> {
        val damage = defender.hp / 2
        listOf(if (isParentalBond) damage * 3 / 2 else damage)
      }
      move.name.value == "Endeavor" -> listOf(if (attacker.hp < defender.hp) defender.hp - attacker.hp else 0)
      move.name.value == "Final Gambit" -> listOf(attacker.hp)
      move.name.value == "Seismic Toss" || move.name.value == "Night Shade" ->
        listOf(if (isParentalBond) DamageCalculator.CHAMPIONS_LEVEL * 2 else DamageCalculator.CHAMPIONS_LEVEL)
      move.isOHKO -> listOf(if (move.name.value == "Sheer Cold" && defender.hasType(PokeType.ICE)) 0 else defender.hp)
      else -> null
    }
  }

  /**
   * The returned move is calculated from the defender to the attacker, on the same field like the source. Counter and
   * Mirror Coat only return a move of their own category; Metal Burst and Comeuppance return any.
   */
  private fun counterRolls(
    battle: PreparedBattle,
    moveUse: MoveUse,
    multiplier: CounterMultiplier,
    effectiveCategory: MoveCategory,
    facts: CalcFactsBuilder,
    statDisplay: StatDisplay,
  ): List<Int> {
    val (attacker, defender, field) = battle
    val countered = moveUse.counteredMove
    if (countered == null || countered.move.category == MoveCategory.STATUS) return listOf(0)
    val counteredBattle = PreparedBattle(attacker = defender, defender = attacker, field = field)
    val counteredResult = DamageCalculator.calculatePreparedMove(
      counteredBattle, countered, defaultHitCount(countered.move, defender), statDisplay,
    )
    val counteredCategory = effectiveCategoryOf(countered.move, defender, attacker)
    if (multiplier == CounterMultiplier.DOUBLE && effectiveCategory != counteredCategory) return listOf(0)
    // like the source, a multi-hit move returns the damage of its last hit
    val counteredRolls = counteredResult.hits.last().rolls
    val bondFactor = if (attacker.resolvedAbility == Ability.PARENTAL_BOND) 2 else 1
    facts.countered = CounteredMove(counteredResult.facts, multiplier)
    return counteredRolls.map { damage ->
      val returned = if (multiplier == CounterMultiplier.DOUBLE) damage * 2 else damage * 3 / 2
      returned * bondFactor
    }
  }
}
