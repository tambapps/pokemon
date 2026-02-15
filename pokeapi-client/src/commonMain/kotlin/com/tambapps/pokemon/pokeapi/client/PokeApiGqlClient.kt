package com.tambapps.pokemon.pokeapi.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class PokeApiGqlClient(
  private val httpClient: HttpClient,
  private val baseUrl: String = GRAPHQL_URL,
) {

  companion object {
    const val GRAPHQL_URL = "https://graphql.pokeapi.co/v1beta2"
    private const val OPERATION_NAME = "getPokemonsAndMoves"
  }

  suspend fun getPokemonsAndMoves(pokemonNames: List<String>, moveNames: List<String>): GqlBatchResult {
    val pokemonNamesLiteral = pokemonNames.joinToString(", ") { "\"$it\"" }
    val moveNamesLiteral = moveNames.joinToString(", ") { "\"$it\"" }
    val query = """
      query $OPERATION_NAME {
        pokemon(where: {name: {_in: [$pokemonNamesLiteral]}}) {
          id
          name
          pokemonstats {
            base_stat
            stat {
              name
            }
          }
        }
        move(where: {name: {_in: [$moveNamesLiteral]}}) {
          name
          movenames(where: {language: {iso3166: {_eq: "us"}}}) {
            name
            language {
              iso3166
            }
          }
          accuracy
          movedamageclass {
            name
          }
          power
          priority
          pp
          type {
            name
          }
        }
      }
    """.trimIndent()

    val response = try {
      httpClient.post(baseUrl) {
        contentType(ContentType.Application.Json)
        setBody(GqlRequest(query = query, operationName = OPERATION_NAME))
      }
    } catch (e: Exception) {
      throw PokeApiException("Failed to execute GraphQL query", e)
    }

    if (!response.status.isSuccess()) {
      throw PokeApiException("GraphQL request failed with status ${response.status.value}")
    }

    return try {
      val data = response.body<GqlResponse>().data
      GqlBatchResult(pokemons = data.pokemon, moves = data.move)
    } catch (e: Exception) {
      throw PokeApiException("Failed to deserialize GraphQL response", e)
    }
  }
}
