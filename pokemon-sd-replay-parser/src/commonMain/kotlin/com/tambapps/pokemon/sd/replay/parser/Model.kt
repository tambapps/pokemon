package com.tambapps.pokemon.sd.replay.parser

import kotlinx.serialization.Serializable

@Serializable
data class Terastallization(
  val pokemon: String,
  val type: String
)

@Serializable
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

@Serializable
data class OpenTeamSheet(
  val pokemons: List<PokemonOts>,
)

@Serializable
data class PokemonOts(
  val name: String,
  val moves: List<String>,
  val teraType: String?
)

@Serializable
data class Player(
  val name: String,
  val pokemons: List<String>,
  val selection: List<String>,
  val beforeElo: Int?,
  val afterElo: Int?,
  val terastallization: Terastallization?,
  val ots: OpenTeamSheet
) {
  val lead get() = selection.take(2)
}