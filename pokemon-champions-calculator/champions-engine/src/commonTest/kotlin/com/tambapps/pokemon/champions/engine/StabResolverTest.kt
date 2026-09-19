package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import kotlin.test.Test
import kotlin.test.assertEquals

class StabResolverTest {

  @Test
  fun noStabWhenTheMoveTypeDoesntMatchEitherOfTheUsersTypes() {
    val garchomp = testPokemon("Garchomp") // Dragon/Ground
    assertEquals(0x1000, stabMultiplier(testMove("Ice Beam"), PokeType.ICE, garchomp))
  }

  @Test
  fun oneAndHalfStabWhenTheMoveTypeMatches() {
    val garchomp = testPokemon("Garchomp")
    assertEquals(0x1800, stabMultiplier(testMove("Earthquake"), PokeType.GROUND, garchomp))
  }

  @Test
  fun adaptabilityDoublesStabInsteadOfOneAndHalf() {
    val garchomp = testPokemon("Garchomp", ability = "Adaptability")
    assertEquals(0x2000, stabMultiplier(testMove("Earthquake"), PokeType.GROUND, garchomp))
  }

  @Test
  fun protetanGrantsStabOnAnOffTypeMoveWhenActive() {
    val protean = testPokemon("Garchomp", ability = "Protean", abilityIsActive = true)
    assertEquals(0x1800, stabMultiplier(testMove("Ice Beam"), PokeType.ICE, protean))
  }

  @Test
  fun protetanGrantsNoStabWhenNotActive() {
    val protean = testPokemon("Garchomp", ability = "Protean", abilityIsActive = false)
    assertEquals(0x1000, stabMultiplier(testMove("Ice Beam"), PokeType.ICE, protean))
  }

  @Test
  fun struggleNeverGetsStabEvenOnAMatchingType() {
    // Struggle is statically Normal-typed; a Normal-type user would otherwise get STAB.
    val normalMon = testPokemon("Snorlax")
    assertEquals(0x1000, stabMultiplier(testMove("Struggle"), PokeType.NORMAL, normalMon))
  }
}
