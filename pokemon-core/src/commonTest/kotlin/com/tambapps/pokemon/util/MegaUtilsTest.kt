package com.tambapps.pokemon.util

import com.tambapps.pokemon.ItemName
import com.tambapps.pokemon.PokemonName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MegaUtilsTest {


  @Test
  fun testSomeMappings() {
    assertEquals(PokemonName("venusaur-mega"), MegaUtils.MEGA_STONE_TO_POKEMON[ItemName("venusaurite")])
    assertEquals(PokemonName("gengar-mega"), MegaUtils.MEGA_STONE_TO_POKEMON[ItemName("gengarite")])
    assertEquals(PokemonName("lucario-mega"), MegaUtils.MEGA_STONE_TO_POKEMON[ItemName("lucarionite")])
  }

  @Test
  fun testDualFormMappings() {
    assertEquals(PokemonName("charizard-mega-x"), MegaUtils.MEGA_STONE_TO_POKEMON[ItemName("charizardite-x")])
    assertEquals(PokemonName("charizard-mega-y"), MegaUtils.MEGA_STONE_TO_POKEMON[ItemName("charizardite-y")])
    assertEquals(PokemonName("mewtwo-mega-x"), MegaUtils.MEGA_STONE_TO_POKEMON[ItemName("mewtwonite-x")])
    assertEquals(PokemonName("mewtwo-mega-y"), MegaUtils.MEGA_STONE_TO_POKEMON[ItemName("mewtwonite-y")])
  }

  @Test
  fun testAllValuesEndWithMega() {
    for ((item, pokemon) in MegaUtils.MEGA_STONE_TO_POKEMON) {
      assertTrue(pokemon.value.contains("mega"), "Expected ${item.value} to map to a mega pokemon, but got ${pokemon.value}")
    }
  }

  @Test
  fun testNonMegaStoneReturnsNull() {
    assertNull(MegaUtils.MEGA_STONE_TO_POKEMON[ItemName("choice-band")])
    assertNull(MegaUtils.MEGA_STONE_TO_POKEMON[ItemName("rusted-shield")])
    assertNull(MegaUtils.MEGA_STONE_TO_POKEMON[ItemName("")])
  }
}
