package com.tambapps.pokemon.sd.replay.log.visitor

import kotlin.test.Test

class SdReplayLogVisitorTest {

  private companion object {
    const val P1A = "p1a"
    const val P1B = "p1b"
    const val P2A = "p2a"
    const val P2B = "p2b"
  }

  @Test
  fun visitMove() {
    assertMoveLog(
      "|move|p2b: Miraidon|Draco Meteor|p1b: Ogerpon",
      P2B, "Miraidon", "Draco Meteor", P1B, "Ogerpon", false
    )
  }

  @Test
  fun visitSpreadMove() {
    assertMoveLog(
      "|move|p1a: Calyrex|Astral Barrage|p2a: Whimsicott|[spread] p2a,p2b",
      P1A, "Calyrex", "Astral Barrage", P2A, "Whimsicott", true
    )
  }

}