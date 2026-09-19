package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EffectiveMoveTypeTest {

  private val plainAttacker = testPokemon("Garchomp")

  @Test
  fun weatherBallMatchesTheActiveWeather() {
    val move = testMove("Weather Ball")
    assertEquals(PokeType.FIRE, effectiveTypeOf(move, plainAttacker, Battlefield(weather = Weather.SUN)))
    assertEquals(PokeType.WATER, effectiveTypeOf(move, plainAttacker, Battlefield(weather = Weather.RAIN)))
    assertEquals(PokeType.ROCK, effectiveTypeOf(move, plainAttacker, Battlefield(weather = Weather.SAND)))
    assertEquals(PokeType.ICE, effectiveTypeOf(move, plainAttacker, Battlefield(weather = Weather.HAIL)))
    assertEquals(PokeType.ICE, effectiveTypeOf(move, plainAttacker, Battlefield(weather = Weather.SNOW)))
    assertEquals(PokeType.NORMAL, effectiveTypeOf(move, plainAttacker, Battlefield()))
  }

  @Test
  fun terrainPulseMatchesTheActiveTerrainWhenGrounded() {
    val move = testMove("Terrain Pulse")
    val grounded = testPokemon("Toxapex")
    assertEquals(PokeType.ELECTRIC, effectiveTypeOf(move, grounded, Battlefield(terrain = Terrain.ELECTRIC)))
    assertEquals(PokeType.GRASS, effectiveTypeOf(move, grounded, Battlefield(terrain = Terrain.GRASSY)))
    assertEquals(PokeType.FAIRY, effectiveTypeOf(move, grounded, Battlefield(terrain = Terrain.MISTY)))
    assertEquals(PokeType.PSYCHIC, effectiveTypeOf(move, grounded, Battlefield(terrain = Terrain.PSYCHIC)))
  }

  @Test
  fun terrainPulseIsNormalWhenTheUserIsntGrounded() {
    val move = testMove("Terrain Pulse")
    val airborne = testPokemon("Charizard") // Flying-type
    assertEquals(PokeType.NORMAL, effectiveTypeOf(move, airborne, Battlefield(terrain = Terrain.ELECTRIC)))
  }

  @Test
  fun ragingBullMatchesTheUsersTaurosPaldeaForm() {
    val move = testMove("Raging Bull")
    assertEquals(PokeType.FIGHTING, effectiveTypeOf(move, testPokemon("Tauros-Paldea-Combat"), Battlefield()))
    assertEquals(PokeType.FIRE, effectiveTypeOf(move, testPokemon("Tauros-Paldea-Blaze"), Battlefield()))
    assertEquals(PokeType.WATER, effectiveTypeOf(move, testPokemon("Tauros-Paldea-Aqua"), Battlefield()))
    assertEquals(PokeType.NORMAL, effectiveTypeOf(move, testPokemon("Garchomp"), Battlefield()))
  }

  @Test
  fun ateAbilitiesRetypeNormalMovesAndGrantTheBoostFlag() {
    val doubleEdge = testMove("Double-Edge") // Normal-type
    val aerilate = testPokemon("Garchomp", ability = "Aerilate")
    assertEquals(PokeType.FLYING, effectiveTypeOf(doubleEdge, aerilate, Battlefield()))
    assertTrue(hasAteAbilityBoost(doubleEdge, aerilate))

    assertEquals(PokeType.FAIRY, effectiveTypeOf(doubleEdge, testPokemon("Garchomp", ability = "Pixilate"), Battlefield()))
    assertEquals(PokeType.ICE, effectiveTypeOf(doubleEdge, testPokemon("Garchomp", ability = "Refrigerate"), Battlefield()))
    assertEquals(PokeType.DRAGON, effectiveTypeOf(doubleEdge, testPokemon("Garchomp", ability = "Dragonize"), Battlefield()))
  }

  @Test
  fun ateAbilitiesDontRetypeAlreadyNonNormalMoves() {
    val earthquake = testMove("Earthquake") // Ground-type
    val aerilate = testPokemon("Garchomp", ability = "Aerilate")
    assertEquals(PokeType.GROUND, effectiveTypeOf(earthquake, aerilate, Battlefield()))
    assertFalse(hasAteAbilityBoost(earthquake, aerilate))
  }

  @Test
  fun ateAbilitiesDontApplyToWeatherBallOrTerrainPulse() {
    val weatherBall = testMove("Weather Ball")
    val aerilate = testPokemon("Garchomp", ability = "Aerilate")
    // Weather Ball is still Normal-typed with no weather active; -ate abilities are exempt from it either way.
    assertEquals(PokeType.NORMAL, effectiveTypeOf(weatherBall, aerilate, Battlefield()))
    assertFalse(hasAteAbilityBoost(weatherBall, aerilate))
  }

  @Test
  fun liquidVoiceRetypesSoundMovesToWater() {
    val hyperVoice = testMove("Hyper Voice")
    val liquidVoice = testPokemon("Garchomp", ability = "Liquid Voice")
    assertEquals(PokeType.WATER, effectiveTypeOf(hyperVoice, liquidVoice, Battlefield()))
  }
}
