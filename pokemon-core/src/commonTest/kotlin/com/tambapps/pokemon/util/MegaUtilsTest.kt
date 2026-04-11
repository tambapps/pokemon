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
    assertEquals(PokemonName("venusaur-mega"), MegaUtils.getMegaPokemon(ItemName("venusaurite")))
    assertEquals(PokemonName("gengar-mega"), MegaUtils.getMegaPokemon(ItemName("gengarite")))
    assertEquals(PokemonName("lucario-mega"), MegaUtils.getMegaPokemon(ItemName("lucarionite")))
  }

  @Test
  fun testDualFormMappings() {
    assertEquals(PokemonName("charizard-mega-x"), MegaUtils.getMegaPokemon(ItemName("charizardite-x")))
    assertEquals(PokemonName("charizard-mega-y"), MegaUtils.getMegaPokemon(ItemName("charizardite-y")))
    assertEquals(PokemonName("mewtwo-mega-x"), MegaUtils.getMegaPokemon(ItemName("mewtwonite-x")))
    assertEquals(PokemonName("mewtwo-mega-y"), MegaUtils.getMegaPokemon(ItemName("mewtwonite-y")))
  }

  @Test
  fun testNormalization() {
    // uppercase
    assertEquals(PokemonName("venusaur-mega"), MegaUtils.getMegaPokemon(ItemName("Venusaurite")))
    assertEquals(PokemonName("gengar-mega"), MegaUtils.getMegaPokemon(ItemName("GENGARITE")))
    // spaces instead of dashes
    assertEquals(PokemonName("charizard-mega-x"), MegaUtils.getMegaPokemon(ItemName("Charizardite X")))
    assertEquals(PokemonName("mewtwo-mega-y"), MegaUtils.getMegaPokemon(ItemName("Mewtwonite Y")))
  }

  @Test
  fun testNonMegaStoneReturnsNull() {
    assertNull(MegaUtils.getMegaPokemon(ItemName("choice-band")))
    assertNull(MegaUtils.getMegaPokemon(ItemName("rusted-shield")))
    assertNull(MegaUtils.getMegaPokemon(ItemName("")))
  }
}
