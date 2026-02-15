package com.tambapps.pokemon.pokeapi.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class PokeApiException(message: String, cause: Throwable? = null) : Exception(message, cause)

data class GqlBatchResult(
  val pokemons: List<GqlPokemon>,
  val moves: List<GqlMove>,
)

// GraphQL request

@Serializable
internal data class GqlRequest(
  val query: String,
  val operationName: String,
)

// GraphQL response

@Serializable
internal data class GqlResponse(
  val data: GqlData,
)

@Serializable
internal data class GqlData(
  val pokemon: List<GqlPokemon>,
  val move: List<GqlMove>,
)

// Pokemon models

@Serializable
data class GqlPokemon(
  val id: Int,
  val name: String,
  @SerialName("pokemonstats")
  val stats: List<GqlPokemonStat>,
)

@Serializable
data class GqlPokemonStat(
  @SerialName("base_stat")
  val baseStat: Int,
  val stat: GqlStatName,
)

@Serializable
data class GqlStatName(
  val name: String,
)

// Move models

@Serializable
data class GqlMove(
  val name: String,
  val accuracy: Int?,
  val power: Int?,
  val priority: Int,
  val pp: Int,
  @SerialName("movenames")
  val moveNames: List<GqlMoveName>,
  @SerialName("movedamageclass")
  val damageClass: GqlNamedResource,
  val type: GqlNamedResource,
)

@Serializable
data class GqlMoveName(
  val name: String,
  val language: GqlMoveLanguage,
)

@Serializable
data class GqlMoveLanguage(
  val iso3166: String,
)

@Serializable
data class GqlNamedResource(
  val name: String,
)
