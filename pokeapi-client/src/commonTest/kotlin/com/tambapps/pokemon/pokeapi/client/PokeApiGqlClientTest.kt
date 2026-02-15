package com.tambapps.pokemon.pokeapi.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class PokeApiGqlClientTest {

  private fun mockClient(responseBody: String, status: HttpStatusCode = HttpStatusCode.OK): PokeApiGqlClient {
    val mockEngine = MockEngine { _ ->
      respond(
        content = responseBody,
        status = status,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
      )
    }
    val httpClient = HttpClient(mockEngine) {
      install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
      }
    }
    return PokeApiGqlClient(httpClient = httpClient)
  }

  @Test
  fun getPokemonsAndMovesReturnsExpectedData() = runTest {
    val client = mockClient(GQL_RESPONSE)
    val result = client.getPokemonsAndMoves(
      pokemonNames = listOf("chien-pao", "ogerpon"),
      moveNames = listOf("pound", "karate-chop")
    )

    // Pokemon assertions
    assertEquals(2, result.pokemons.size)

    val chienPao = result.pokemons.first { it.name == "chien-pao" }
    assertEquals(1002, chienPao.id)
    assertEquals(6, chienPao.stats.size)
    assertEquals(80, chienPao.stats.first { it.stat.name == "hp" }.baseStat)
    assertEquals(120, chienPao.stats.first { it.stat.name == "attack" }.baseStat)
    assertEquals(135, chienPao.stats.first { it.stat.name == "speed" }.baseStat)

    val ogerpon = result.pokemons.first { it.name == "ogerpon" }
    assertEquals(1017, ogerpon.id)
    assertEquals(80, ogerpon.stats.first { it.stat.name == "hp" }.baseStat)
    assertEquals(110, ogerpon.stats.first { it.stat.name == "speed" }.baseStat)

    // Move assertions
    assertEquals(2, result.moves.size)

    val pound = result.moves.first { it.name == "pound" }
    assertEquals("Pound", pound.moveNames.first().name)
    assertEquals("us", pound.moveNames.first().language.iso3166)
    assertEquals(100, pound.accuracy)
    assertEquals(40, pound.power)
    assertEquals(0, pound.priority)
    assertEquals(35, pound.pp)
    assertEquals("physical", pound.damageClass.name)
    assertEquals("normal", pound.type.name)

    val karateChop = result.moves.first { it.name == "karate-chop" }
    assertEquals("Karate Chop", karateChop.moveNames.first().name)
    assertEquals(100, karateChop.accuracy)
    assertEquals(50, karateChop.power)
    assertEquals(25, karateChop.pp)
    assertEquals("fighting", karateChop.type.name)
  }

  @Test
  fun getPokemonsAndMovesFailsOnErrorStatus() = runTest {
    val client = mockClient("{}", HttpStatusCode.InternalServerError)
    assertFailsWith<PokeApiException> {
      client.getPokemonsAndMoves(listOf("bulbasaur"), listOf("pound"))
    }
  }

  companion object {
    private val GQL_RESPONSE = """
      {
        "data": {
          "pokemon": [
            {
              "id": 1002,
              "name": "chien-pao",
              "pokemonstats": [
                { "base_stat": 80,  "stat": { "name": "hp" } },
                { "base_stat": 120, "stat": { "name": "attack" } },
                { "base_stat": 80,  "stat": { "name": "defense" } },
                { "base_stat": 90,  "stat": { "name": "special-attack" } },
                { "base_stat": 65,  "stat": { "name": "special-defense" } },
                { "base_stat": 135, "stat": { "name": "speed" } }
              ]
            },
            {
              "id": 1017,
              "name": "ogerpon",
              "pokemonstats": [
                { "base_stat": 80,  "stat": { "name": "hp" } },
                { "base_stat": 120, "stat": { "name": "attack" } },
                { "base_stat": 84,  "stat": { "name": "defense" } },
                { "base_stat": 60,  "stat": { "name": "special-attack" } },
                { "base_stat": 96,  "stat": { "name": "special-defense" } },
                { "base_stat": 110, "stat": { "name": "speed" } }
              ]
            }
          ],
          "move": [
            {
              "name": "pound",
              "movenames": [{ "name": "Pound", "language": { "iso3166": "us" } }],
              "accuracy": 100,
              "movedamageclass": { "name": "physical" },
              "power": 40,
              "priority": 0,
              "pp": 35,
              "type": { "name": "normal" }
            },
            {
              "name": "karate-chop",
              "movenames": [{ "name": "Karate Chop", "language": { "iso3166": "us" } }],
              "accuracy": 100,
              "movedamageclass": { "name": "physical" },
              "power": 50,
              "priority": 0,
              "pp": 25,
              "type": { "name": "fighting" }
            }
          ]
        }
      }
    """.trimIndent()
  }
}
