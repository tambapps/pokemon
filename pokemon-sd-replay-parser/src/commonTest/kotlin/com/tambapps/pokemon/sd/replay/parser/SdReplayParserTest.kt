package com.tambapps.pokemon.sd.replay.parser

import com.tambapps.pokemon.PokeType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SdReplayParserTest {

  private val parser = SdReplayParser()

  @Test
  fun decodeTest() {
    val rawReplay = parser.parse(SdReplays.REPLAY_1)

    // Verify basic replay properties
    assertEquals("gen9vgc2025reggbo3", rawReplay.formatId)
    assertEquals(1288, rawReplay.rating)
    assertEquals(1735820009, rawReplay.uploadTime)

    val replay = parser.parse(rawReplay)

    // Test player 1 data
    val player1 = replay.player1
    assertEquals(listOf("Rillaboom", "Raging Bolt"), player1.lead, "Player 1 has incorrect leads")
    assertEquals(listOf("Rillaboom", "Raging Bolt", "Calyrex-Shadow", "Ogerpon-Hearthflame"), player1.selection)

    val expectedTeam1 = listOf(
      "Calyrex-Shadow",
      "Incineroar",
      "Rillaboom",
      "Urshifu-*",
      "Raging Bolt",
      "Ogerpon-Hearthflame"
    )
    assertEquals(expectedTeam1, player1.teamPreview.pokemons.map { it.name })
    assertEquals(Terastallization(pokemon = "Raging Bolt", type = PokeType.ELECTRIC), player1.terastallization)
    assertEquals(1358, player1.beforeElo)
    assertEquals(1332, player1.afterElo)

    // Test player 2 data
    val player2 = replay.player2
    assertEquals(listOf("Miraidon", "Entei"), player2.lead, "Player 2 has incorrect leads")
    assertEquals(listOf("Miraidon", "Entei", "Whimsicott", "Chien-Pao"), player2.selection)

    val expectedTeam2 = listOf(
      "Miraidon",
      "Entei",
      "Chien-Pao",
      "Iron Hands",
      "Whimsicott",
      "Ogerpon-Cornerstone"
    )
    assertEquals(expectedTeam2, player2.teamPreview.pokemons.map { it.name })
    assertEquals(Terastallization(pokemon = "Entei", type = PokeType.NORMAL), player2.terastallization)
    assertEquals(1256, player2.beforeElo)
    assertEquals(1288, player2.afterElo)

    // Test winner
    assertEquals("jarmanvgc", replay.winner)
    val winnerPlayer = replay.players.find { it.name == replay.winner }
    assertEquals(player2, winnerPlayer)

    // Test move usages
    assertNotNull(player1.movesUsage)
    println(player1.movesUsage)
  }
}