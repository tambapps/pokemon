package com.tambapps.pokemon.sd.replay.parser

import com.tambapps.pokemon.PokeType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Terastallization(
  val pokemon: String,
  val type: PokeType
)

@Serializable
data class SdReplay(
  val players: List<Player>,
  val uploadTime: Long,
  val format: String,
  val rating: Int?,
  val parserVersion: String?,
  val winner: String?,
  val nextBattle: String?,
) {
  val player1 get() = players[0]
  val player2 get() = players[1]
}

@Serializable
data class OpenTeamSheet(
  val pokemons: List<OtsPokemon>,
)

@Serializable
data class OtsPokemon(
  val name: String,
  val item: String,
  val ability: String,
  val moves: List<String>,
  val level: Int,
  val teraType: PokeType?
)

@Serializable
data class TeamPreview(
  val pokemons: List<TeamPreviewPokemon>,
)

@Serializable
data class TeamPreviewPokemon(
  val name: String,
  val level: Int?
)

@Serializable
data class Player(
  val name: String,
  val teamPreview: TeamPreview,
  val selection: List<String>,
  val beforeElo: Int?,
  val afterElo: Int?,
  val terastallization: Terastallization?,
  val ots: OpenTeamSheet?,
  val movesUsage: Map<String, Map<String, Int>>
) {
  val lead get() = selection.take(2)
}

data class PlayerBuilder(
  var name: String = "<none>",
  var selection: MutableList<String> = mutableListOf(),
  var beforeElo: Int? = 0,
  var afterElo: Int? = 0,
  var terastallization: Terastallization? = null,
  var ots: OpenTeamSheet? = null,
  val movesUsage: MutableMap<String, MutableMap<String, Int>> = mutableMapOf(),
) {

  private val teamPreviewPokemons = mutableListOf<TeamPreviewPokemon>()

  fun teamPreviewPokemon(pokemon: TeamPreviewPokemon) = teamPreviewPokemons.add(pokemon)

  fun selection(pokemon: String) {
    if (!selection.contains(pokemon)) {
      selection.add(pokemon)
    }
  }

  fun moveUsage(pokemonName: String, moveName: String) {
    val movesCount = movesUsage.getOrPut(pokemonName) { mutableMapOf() }
    movesCount[moveName] = movesCount.getOrPut(moveName) { 0 } + 1
  }

  fun build() = Player(
    name = name,
    teamPreview = TeamPreview(teamPreviewPokemons),
    selection = selection.toList(),
    beforeElo = beforeElo,
    afterElo = afterElo,
    terastallization = terastallization,
    ots = ots,
    movesUsage = movesUsage.toMap()
  )
}


@Serializable
data class RawSdReplay(
  @SerialName("formatid")
  val formatId: String,
  val players: List<String>,
  @SerialName("uploadtime")
  val uploadTime: Long,
  val rating: Int?,
  @SerialName("log")
  val logs: String
)