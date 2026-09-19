package com.tambapps.pokemon.champions.engine

import kotlin.test.Test
import kotlin.test.assertEquals

class ChainModsTest {

  @Test
  fun emptyListIsANoOpMultiplier() {
    assertEquals(0x1000, chainMods(emptyList()))
  }

  @Test
  fun oneModIsReturnedAsIs() {
    assertEquals(0x1800, chainMods(listOf(0x1800)))
  }

  @Test
  fun neutralModsInTheListAreSkippedButOthersStillApply() {
    assertEquals(0x1800, chainMods(listOf(0x1000, 0x1800, 0x1000)))
  }

  @Test
  fun multipleModsChainWithRoundingAtEachStep() {
    // 1.5x then 1.5x: 0x1000 -> round(0x1000*0x1800/0x1000)=0x1800 -> round(0x1800*0x1800/0x1000)=0x2400
    assertEquals(0x2400, chainMods(listOf(0x1800, 0x1800)))
  }
}
