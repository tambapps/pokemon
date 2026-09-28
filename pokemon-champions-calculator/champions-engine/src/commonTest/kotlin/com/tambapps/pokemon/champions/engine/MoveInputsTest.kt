package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.champions.data.ChampionsDex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The move properties telling a caller which [MoveUse] inputs matter for a move. */
class MoveInputsTest {

  @Test
  fun counterLikeMovesReturnTheDefendersMove() {
    for (name in listOf("Counter", "Mirror Coat", "Metal Burst", "Comeuppance")) {
      assertTrue(testMove(name).returnsDefenderMove, name)
    }
    assertFalse(testMove("Earthquake").returnsDefenderMove)
    assertEquals(
      setOf("Counter", "Mirror Coat", "Metal Burst", "Comeuppance"),
      ChampionsDex.allMoves.filter { it.returnsDefenderMove }.map { it.name.value }.toSet(),
    )
  }
}
