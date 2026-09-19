package com.tambapps.pokemon.champions.engine

import kotlin.test.Test
import kotlin.test.assertEquals

class PokeRoundTest {

  @Test
  fun roundsDownOnExactlyHalf() {
    // GameFreak rounds .5 down, unlike the usual round-half-up convention.
    assertEquals(2, pokeRound(2.5))
    assertEquals(10, pokeRound(10.5))
  }

  @Test
  fun roundsUpAboveHalf() {
    assertEquals(3, pokeRound(2.51))
  }

  @Test
  fun roundsDownBelowHalf() {
    assertEquals(2, pokeRound(2.49))
  }

  @Test
  fun wholeNumbersAreUnchanged() {
    assertEquals(5, pokeRound(5.0))
  }

  @Test
  fun fixedPointOverloadDividesByTheGivenDenominator() {
    // 0x1800 / 0x1000 = 1.5x
    assertEquals(15, pokeRound(10 * 0x1800, 0x1000))
  }
}
