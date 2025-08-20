package com.tambapps.pokemon.sd.replay.log.visitor

import kotlin.test.assertEquals
import kotlin.test.assertTrue


fun assertMoveLog(log: String,
                  expectedSourcePokemonSlot: String,
                  expectedSourcePokemonName: String,
                  expectedMoveName: String,
                  expectedTargetPokemonSlot: String?,
                  expectedTargetPokemonName: String?,
                  expectedIsSpread: Boolean
) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitMoveLog(
      sourcePokemonSlot: String,
      sourcePokemonName: String,
      moveName: String,
      targetPokemonSlot: String?,
      targetPokemonName: String?,
      isSpread: Boolean,
      isStill: Boolean,
      additionalInfo: String
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

fun assertJoinLog(log: String, expectedPlayerName: String) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitJoinLog(playerName: String) {
      visited = true
      assertEquals(expectedPlayerName, playerName)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertPlayerLog(log: String, expectedPlayerSlot: String, expectedPlayerName: String, expectedAvatar: String?, expectedRating: String?) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitPlayerLog(playerSlot: String, playerName: String, avatar: String?, rating: String?) {
      visited = true
      assertEquals(expectedPlayerSlot, playerSlot)
      assertEquals(expectedPlayerName, playerName)
      assertEquals(expectedAvatar, avatar)
      assertEquals(expectedRating, rating)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertPokeLog(log: String, expectedPlayerSlot: String, expectedPokemonName: String, expectedLevel: Int?, expectedGender: String?) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitPokeLog(playerSlot: String, pokemonName: String, level: Int?, gender: String?) {
      visited = true
      assertEquals(expectedPlayerSlot, playerSlot)
      assertEquals(expectedPokemonName, pokemonName)
      assertEquals(expectedLevel, level)
      assertEquals(expectedGender, gender)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertSwitchLog(log: String, expectedPokemonSlot: String, expectedPokemonName: String, expectedHpValue: Int?) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitSwitchLog(pokemonSlot: String, pokemonName: String, hpPercentage: Int?) {
      visited = true
      assertEquals(expectedPokemonSlot, pokemonSlot)
      assertEquals(expectedPokemonName, pokemonName)
      assertEquals(expectedHpValue, hpPercentage)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertTerastallizeLog(log: String, expectedPokemonSlot: String, expectedTeraType: String) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitTerastallizeLog(pokemonSlot: String, teraType: String) {
      visited = true
      assertEquals(expectedPokemonSlot, pokemonSlot)
      assertEquals(expectedTeraType, teraType)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertDamageLog(log: String, expectedPokemonSlot: String, expectedHpStatus: String, expectedSource: String?) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitDamageLog(pokemonSlot: String, hpStatus: String, source: String?) {
      visited = true
      assertEquals(expectedPokemonSlot, pokemonSlot)
      assertEquals(expectedHpStatus, hpStatus)
      assertEquals(expectedSource, source)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertFaintLog(log: String, expectedPokemonSlot: String) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitFaintLog(pokemonSlot: String) {
      visited = true
      assertEquals(expectedPokemonSlot, pokemonSlot)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertHealLog(log: String, expectedPokemonSlot: String, expectedHpStatus: String, expectedSource: String?) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitHealLog(pokemonSlot: String, hpStatus: String, source: String?) {
      visited = true
      assertEquals(expectedPokemonSlot, pokemonSlot)
      assertEquals(expectedHpStatus, hpStatus)
      assertEquals(expectedSource, source)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertAbilityLog(log: String, expectedPokemonSlot: String, expectedAbility: String) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitAbilityLog(pokemonSlot: String, ability: String) {
      visited = true
      assertEquals(expectedPokemonSlot, pokemonSlot)
      assertEquals(expectedAbility, ability)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertFieldStartLog(log: String, expectedField: String, expectedSource: String?, expectedFrom: String?) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitFieldStartLog(field: String, source: String?, from: String?) {
      visited = true
      assertEquals(expectedField, field)
      assertEquals(expectedSource, source)
      assertEquals(expectedFrom, from)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertEndItemLog(log: String, expectedPokemonSlot: String, expectedItem: String) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitEndItemLog(pokemonSlot: String, item: String) {
      visited = true
      assertEquals(expectedPokemonSlot, pokemonSlot)
      assertEquals(expectedItem, item)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertActivateLog(log: String, expectedPokemonSlot: String, expectedAbility: String, expectedDetails: String) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitActivateLog(pokemonSlot: String, ability: String, details: String) {
      visited = true
      assertEquals(expectedPokemonSlot, pokemonSlot)
      assertEquals(expectedAbility, ability)
      assertEquals(expectedDetails, details)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertBoostLog(log: String, expectedPokemonSlot: String, expectedStat: String, expectedAmount: Int) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitBoostLog(pokemonSlot: String, stat: String, amount: Int) {
      visited = true
      assertEquals(expectedPokemonSlot, pokemonSlot)
      assertEquals(expectedStat, stat)
      assertEquals(expectedAmount, amount)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertUnboostLog(log: String, expectedPokemonSlot: String, expectedStat: String, expectedAmount: Int) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitUnboostLog(pokemonSlot: String, stat: String, amount: Int) {
      visited = true
      assertEquals(expectedPokemonSlot, pokemonSlot)
      assertEquals(expectedStat, stat)
      assertEquals(expectedAmount, amount)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertSingleTurnLog(log: String, expectedPokemonSlot: String, expectedMove: String) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitSingleTurnLog(pokemonSlot: String, move: String) {
      visited = true
      assertEquals(expectedPokemonSlot, pokemonSlot)
      assertEquals(expectedMove, move)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertFailLog(log: String, expectedPokemonSlot: String) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitFailLog(pokemonSlot: String) {
      visited = true
      assertEquals(expectedPokemonSlot, pokemonSlot)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertCritLog(log: String, expectedPokemonSlot: String) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitCritLog(pokemonSlot: String) {
      visited = true
      assertEquals(expectedPokemonSlot, pokemonSlot)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertSuperEffectiveLog(log: String, expectedPokemonSlot: String) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitSuperEffectiveLog(pokemonSlot: String) {
      visited = true
      assertEquals(expectedPokemonSlot, pokemonSlot)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertResistedLog(log: String, expectedPokemonSlot: String) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitResistedLog(pokemonSlot: String) {
      visited = true
      assertEquals(expectedPokemonSlot, pokemonSlot)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertSideStartLog(log: String, expectedSide: String, expectedCondition: String) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitSideStartLog(side: String, condition: String) {
      visited = true
      assertEquals(expectedSide, side)
      assertEquals(expectedCondition, condition)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertSideEndLog(log: String, expectedSide: String, expectedCondition: String) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitSideEndLog(side: String, condition: String) {
      visited = true
      assertEquals(expectedSide, side)
      assertEquals(expectedCondition, condition)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertRatingUpdateRawLog(log: String, expectedContent: String, expectedPlayerName: String, expectedBeforeElo: Int, expectedAfterElo: Int) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitRatingUpdateRawLog(content: String, playerName: String, beforeElo: Int, afterElo: Int) {
      visited = true
      assertEquals(expectedContent, content)
      assertEquals(expectedPlayerName, playerName)
      assertEquals(expectedBeforeElo, beforeElo)
      assertEquals(expectedAfterElo, afterElo)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}

fun assertWinLog(log: String, expectedWinner: String) {
  var visited = false
  val visitor = object: SdReplayLogVisitor {
    override fun visitWinLog(winner: String) {
      visited = true
      assertEquals(expectedWinner, winner)
    }
  }
  visitor.visit(log)
  assertTrue(visited, "Should have visited")
}
