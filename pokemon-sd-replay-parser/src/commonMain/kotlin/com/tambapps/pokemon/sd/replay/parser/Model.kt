package com.tambapps.pokemon.sd.replay.parser

import com.tambapps.pokemon.PokemonName
import com.tambapps.pokemon.TeraType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class Terastallization(
  val pokemon: PokemonName,
  val type: TeraType
)

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

data class OpenTeamSheet(
  val pokemons: List<OtsPokemon>,
)

data class OtsPokemon(
  val name: PokemonName,
  val item: String,
  val ability: String,
  val moves: List<String>,
  val level: Int,
  val teraType: TeraType?
)

data class TeamPreview(
  val pokemons: List<TeamPreviewPokemon>,
)

data class TeamPreviewPokemon(
  val name: PokemonName,
  val level: Int?
)

data class Player(
  val name: String,
  val teamPreview: TeamPreview,
  val selection: List<PokemonName>,
  val beforeElo: Int?,
  val afterElo: Int?,
  val terastallization: Terastallization?,
  val ots: OpenTeamSheet?,
  val movesUsage: Map<PokemonName, Map<String, Int>>
) {
  val lead get() = selection.take(2)
}

data class PlayerBuilder(
  var name: String = "<none>",
  var selection: MutableList<PokemonName> = mutableListOf(),
  var beforeElo: Int? = null,
  var afterElo: Int? = null,
  var terastallization: Terastallization? = null,
  var ots: OpenTeamSheet? = null,
  val movesUsage: MutableMap<PokemonName, MutableMap<String, Int>> = mutableMapOf(),
) {

  private val teamPreviewPokemons = mutableListOf<TeamPreviewPokemon>()

  fun teamPreviewPokemon(pokemon: TeamPreviewPokemon) = teamPreviewPokemons.add(pokemon)

  fun selection(pokemon: PokemonName) {
    if (!selection.contains(pokemon)) {
      selection.add(pokemon)
    }
  }

  fun moveUsage(pokemonName: PokemonName, moveName: String) {
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