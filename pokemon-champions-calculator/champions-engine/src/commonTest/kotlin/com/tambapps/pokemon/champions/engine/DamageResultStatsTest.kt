package com.tambapps.pokemon.champions.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Which attack/defense stats a [DamageResult] reports having rolled damage off of. */
class DamageResultStatsTest {

  private fun result(attacker: String, defender: String, move: String) =
    DamageCalculator.calculateSingleHit(testPokemon(attacker), testPokemon(defender), MoveUse(testMove(move)), Battlefield())

  @Test
  fun physicalMoveUsesAttackerAttackAgainstDefense() {
    val result = result("Garchomp", "Toxapex", "Earthquake")
    assertEquals(AttackStatSource(BoostableStat.ATTACK, isDefenderStat = false), result.attackStat)
    assertEquals(BoostableStat.DEFENSE, result.defenseStat)
  }

  @Test
  fun specialMoveUsesAttackerSpecialAttackAgainstSpecialDefense() {
    val result = result("Sylveon", "Garchomp", "Moonblast")
    assertEquals(AttackStatSource(BoostableStat.SPECIAL_ATTACK, isDefenderStat = false), result.attackStat)
    assertEquals(BoostableStat.SPECIAL_DEFENSE, result.defenseStat)
  }

  @Test
  fun psyshockIsSpecialButHitsDefense() {
    val result = result("Sylveon", "Garchomp", "Psyshock")
    assertEquals(AttackStatSource(BoostableStat.SPECIAL_ATTACK, isDefenderStat = false), result.attackStat)
    assertEquals(BoostableStat.DEFENSE, result.defenseStat)
  }

  @Test
  fun bodyPressUsesAttackerDefense() {
    val result = result("Corviknight", "Kingambit", "Body Press")
    assertEquals(AttackStatSource(BoostableStat.DEFENSE, isDefenderStat = false), result.attackStat)
    assertEquals(BoostableStat.DEFENSE, result.defenseStat)
  }

  @Test
  fun foulPlayUsesDefenderAttack() {
    val result = result("Incineroar", "Garchomp", "Foul Play")
    assertEquals(AttackStatSource(BoostableStat.ATTACK, isDefenderStat = true), result.attackStat)
    assertEquals(BoostableStat.DEFENSE, result.defenseStat)
  }

  @Test
  fun statusMoveReportsNoStats() {
    val result = result("Incineroar", "Garchomp", "Protect")
    assertNull(result.attackStat)
    assertNull(result.defenseStat)
  }
}
