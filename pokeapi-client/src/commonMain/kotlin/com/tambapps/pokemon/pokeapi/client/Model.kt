package com.tambapps.pokemon.pokeapi.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class PokeApiException(message: String, cause: Throwable? = null) : Exception(message, cause)

@Serializable
internal data class GqlRequest(
  val query: String,
  val operationName: String,
)

@Serializable
internal data class GqlPokemonsResponse(
  val data: GqlPokemonsData,
)

@Serializable
internal data class GqlPokemonsData(
  val pokemon: List<GqlPokemon>,
)

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
