package com.tambapps.pokemon.pokeapi.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class PokeApiGqlClient(
  private val httpClient: HttpClient = defaultHttpClient(),
  private val baseUrl: String = GRAPHQL_URL,
) {

  companion object {
    const val GRAPHQL_URL = "https://graphql.pokeapi.co/v1beta2"
    private const val OPERATION_NAME = "getPokemonsByNames"

    fun defaultHttpClient() = HttpClient {
      install(ContentNegotiation) {
        json(Json {
          ignoreUnknownKeys = true
          coerceInputValues = true
        })
      }
    }
  }

  suspend fun getPokemons(names: List<String>): List<GqlPokemon> {
    val query = """
      query $OPERATION_NAME {
        pokemon(where: {name: {_in: [${names.joinToString(", ") { "\"$it\"" }}]}}) {
          id
          name
          pokemonstats {
            base_stat
            stat {
              name
            }
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
      response.body<GqlPokemonsResponse>().data.pokemon
    } catch (e: Exception) {
      throw PokeApiException("Failed to deserialize GraphQL response", e)
    }
  }
}
