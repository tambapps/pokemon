package com.tambapps.pokemon.sd.replay.log.visitor

import kotlin.test.assertEquals
import kotlin.test.assertTrue


fun assertMoveLog(log: String,
                  expectedSourcePokemonSlot: String,
                  expectedSourcePokemonName: String,
                  expectedMoveName: String,
                  expectedTargetPokemonSlot: String,
                  expectedTargetPokemonName: String,
                  expectedIsSpread: Boolean

) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitMoveLog(
      sourcePokemonSlot: String,
      sourcePokemonName: String,
      moveName: String,
      targetPokemonSlot: String,
      targetPokemonName: String,
      isSpread: Boolean
    ) {
      visited = true
      assertEquals(expectedSourcePokemonSlot, sourcePokemonSlot)
      assertEquals(expectedSourcePokemonName, sourcePokemonName)
      assertEquals(expectedMoveName, moveName)
      assertEquals(expectedTargetPokemonSlot, targetPokemonSlot)
      assertEquals(expectedTargetPokemonName, targetPokemonName)
      assertEquals(expectedIsSpread, isSpread)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}
