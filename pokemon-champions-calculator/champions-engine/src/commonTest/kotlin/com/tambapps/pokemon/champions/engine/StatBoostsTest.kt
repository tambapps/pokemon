package com.tambapps.pokemon.champions.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StatBoostsTest {

  @Test
  fun zeroStageLeavesTheStatUnchanged() {
    assertEquals(100, applyBoostStage(100, 0))
  }

  @Test
  fun positiveStagesUseTheGamesRatioTable() {
    // +1 = 3/2, +2 = 2/1, ... +6 = 4/1
    assertEquals(150, applyBoostStage(100, 1))
    assertEquals(200, applyBoostStage(100, 2))
    assertEquals(250, applyBoostStage(100, 3))
    assertEquals(300, applyBoostStage(100, 4))
    assertEquals(350, applyBoostStage(100, 5))
    assertEquals(400, applyBoostStage(100, 6))
  }

  @Test
  fun negativeStagesUseTheGamesRatioTableAndTruncate() {
    // -1 = 2/3, -2 = 1/2, ... -6 = 1/4
    assertEquals(66, applyBoostStage(100, -1))
    assertEquals(50, applyBoostStage(100, -2))
    assertEquals(40, applyBoostStage(100, -3))
    assertEquals(33, applyBoostStage(100, -4))
    assertEquals(28, applyBoostStage(100, -5))
    assertEquals(25, applyBoostStage(100, -6))
  }

  @Test
  fun boostsAreClampedToTheGamesMinusSixToSixRange() {
    assertFailsWith<IllegalArgumentException> { StatBoosts(attack = 7) }
    assertFailsWith<IllegalArgumentException> { StatBoosts(speed = -7) }
  }

  @Test
  fun indexingByBoostableStatReturnsTheMatchingField() {
    val boosts = StatBoosts(attack = 1, defense = 2, specialAttack = 3, specialDefense = 4, speed = 5)
    assertEquals(1, boosts[BoostableStat.ATTACK])
    assertEquals(2, boosts[BoostableStat.DEFENSE])
    assertEquals(3, boosts[BoostableStat.SPECIAL_ATTACK])
    assertEquals(4, boosts[BoostableStat.SPECIAL_DEFENSE])
    assertEquals(5, boosts[BoostableStat.SPEED])
  }
}
