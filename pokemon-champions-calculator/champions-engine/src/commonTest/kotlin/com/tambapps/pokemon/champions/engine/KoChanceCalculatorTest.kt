package com.tambapps.pokemon.champions.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KoChanceCalculatorTest {

  // The same 16-roll Earthquake-vs-Toxapex distribution from DamageCalculatorCrossValidationTest,
  // cross-checked against a plain-Python convolution independent of this codebase.
  private val rolls = listOf(74, 74, 78, 78, 78, 80, 80, 80, 80, 84, 84, 84, 86, 86, 86, 90)

  @Test
  fun oneHitKoChanceIsTheFractionOfRollsThatReachTheTarget() {
    assertEquals(1.0, KoChanceCalculator.koChance(rolls, hits = 1, targetHp = 74))
    assertEquals(0.4375, KoChanceCalculator.koChance(rolls, hits = 1, targetHp = 84))
    assertEquals(0.0625, KoChanceCalculator.koChance(rolls, hits = 1, targetHp = 90))
    assertEquals(0.0, KoChanceCalculator.koChance(rolls, hits = 1, targetHp = 91))
  }

  @Test
  fun twoHitsGuaranteesTheKoWhenEvenTheWorstRollTwiceClearsTheTarget() {
    // worst case: 74 + 74 = 148 >= 145
    assertEquals(1.0, KoChanceCalculator.koChance(rolls, hits = 2, targetHp = 145))
  }

  @Test
  fun threeHitsCanHaveAPartialChance() {
    assertEquals(0.9775390625, KoChanceCalculator.koChance(rolls, hits = 3, targetHp = 230), absoluteTolerance = 1e-9)
  }

  @Test
  fun minimumHitsToKoFindsTheFirstHitCountWithAnyChance() {
    val result = KoChanceCalculator.minimumHitsToKo(rolls, targetHp = 145)
    assertEquals(2, result?.hits)
    assertEquals(1.0, result?.chance)
    assertEquals(true, result?.isGuaranteed)
  }

  @Test
  fun minimumHitsToKoReturnsNullWhenOutOfReach() {
    assertNull(KoChanceCalculator.minimumHitsToKo(rolls, targetHp = 1_000_000, maxHits = 4))
  }
}

private fun assertEquals(expected: Double, actual: Double, absoluteTolerance: Double) {
  kotlin.test.assertTrue(kotlin.math.abs(expected - actual) <= absoluteTolerance, "expected $expected but was $actual")
}
