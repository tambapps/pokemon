package com.tambapps.pokemon

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PokemonNameTest {

  @Test
  fun testIsMega_simpleMega() {
    assertTrue(PokemonName("venusaur-mega").isMega)
    assertTrue(PokemonName("gengar-mega").isMega)
    assertTrue(PokemonName("lucario-mega").isMega)
  }

  @Test
  fun testIsMega_dualForms() {
    assertTrue(PokemonName("charizard-mega-x").isMega)
    assertTrue(PokemonName("charizard-mega-y").isMega)
    assertTrue(PokemonName("mewtwo-mega-x").isMega)
    assertTrue(PokemonName("mewtwo-mega-y").isMega)
  }

  @Test
  fun testIsMega_normalization() {
    assertTrue(PokemonName("Venusaur-Mega").isMega)
    assertTrue(PokemonName("Charizard-Mega-Y").isMega)
    assertTrue(PokemonName("MEWTWO-MEGA-X").isMega)
  }

  @Test
  fun testIsMega_nonMega() {
    assertFalse(PokemonName("charizard").isMega)
    assertFalse(PokemonName("pikachu").isMega)
    assertFalse(PokemonName("zamazenta-crowned").isMega)
    assertFalse(PokemonName("ogerpon-cornerstone").isMega)
  }

  @Test
  fun testPretty_simpleMega() {
    assertEquals("Mega Venusaur", PokemonName("venusaur-mega").pretty)
    assertEquals("Mega Gengar", PokemonName("gengar-mega").pretty)
    assertEquals("Mega Lucario", PokemonName("lucario-mega").pretty)
  }

  @Test
  fun testPretty_dualFormMega() {
    assertEquals("Mega Charizard X", PokemonName("charizard-mega-x").pretty)
    assertEquals("Mega Charizard Y", PokemonName("charizard-mega-y").pretty)
    assertEquals("Mega Mewtwo X", PokemonName("mewtwo-mega-x").pretty)
    assertEquals("Mega Mewtwo Y", PokemonName("mewtwo-mega-y").pretty)
  }

  @Test
  fun testPretty_megaNormalization() {
    assertEquals("Mega Venusaur", PokemonName("Venusaur-Mega").pretty)
    assertEquals("Mega Charizard Y", PokemonName("Charizard-Mega-Y").pretty)
  }

  @Test
  fun testPretty_nonMega() {
    assertEquals("Charizard", PokemonName("charizard").pretty)
    assertEquals("Tapu Koko", PokemonName("tapu-koko").pretty)
  }
}
