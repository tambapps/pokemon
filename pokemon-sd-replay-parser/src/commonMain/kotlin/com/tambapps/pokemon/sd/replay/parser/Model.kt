package com.tambapps.pokemon.sd.replay.parser

data class Terastallization(
  val pokemon: String,
  val type: String
)

data class SdReplay(
  val players: List<Player>,
  val uploadTime: Long,
  val format: String,
  val rating: Int?,
  val parserVersion: String?
) {
  val player1 get() = players[0]
  val player2 get() = players[1]
}

data class OpenTeamSheet(
  val pokemons: List<PokemonOts>,
)

data class PokemonOts(
  val name: String,
  val moves: List<String>,
  val teraType: String?
)

data class Player(
  val name: String,
  val pokemons: List<String>,
  val selection: List<String>,
  val beforeElo: Int?,
  val afterElo: Int?,
  val terastallization: Terastallization?,
  val ots: OpenTeamSheet
) {
  val leads get() = selection.take(2)
}