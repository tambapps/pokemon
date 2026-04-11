package com.tambapps.pokemon.util

import com.tambapps.pokemon.ItemName
import com.tambapps.pokemon.PokemonName
import com.tambapps.pokemon.util.MegaUtils.isMegaStone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MegaUtilsTest {

  @Test
  fun testSomeMappings() {
    assertEquals(PokemonName("venusaur-mega"), MegaUtils.getMegaPokemon(MegaUtils.VENUSAURITE))
    assertEquals(PokemonName("gengar-mega"), MegaUtils.getMegaPokemon(MegaUtils.GENGARITE))
    assertEquals(PokemonName("lucario-mega"), MegaUtils.getMegaPokemon(MegaUtils.LUCARIONITE))
  }

  @Test
  fun testDualFormMappings() {
    assertEquals(PokemonName("charizard-mega-x"), MegaUtils.getMegaPokemon(MegaUtils.CHARIZARDITE_X))
    assertEquals(PokemonName("charizard-mega-y"), MegaUtils.getMegaPokemon(MegaUtils.CHARIZARDITE_Y))
    assertEquals(PokemonName("mewtwo-mega-x"), MegaUtils.getMegaPokemon(MegaUtils.MEWTWONITE_X))
    assertEquals(PokemonName("mewtwo-mega-y"), MegaUtils.getMegaPokemon(MegaUtils.MEWTWONITE_Y))
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

  @Test
  fun testIsMegaStone() {
    assertTrue(MegaUtils.VENUSAURITE.isMegaStone)
    assertTrue(MegaUtils.GENGARITE.isMegaStone)
    assertTrue(MegaUtils.CHARIZARDITE_X.isMegaStone)
    assertTrue(MegaUtils.MEWTWONITE_Y.isMegaStone)
  }

  @Test
  fun testIsMegaStone_normalization() {
    assertTrue(ItemName("Venusaurite").isMegaStone)
    assertTrue(ItemName("GENGARITE").isMegaStone)
    assertTrue(ItemName("Charizardite X").isMegaStone)
  }

  @Test
  fun testIsMegaStone_nonMegaStone() {
    assertFalse(ItemName("choice-band").isMegaStone)
    assertFalse(ItemName("rusted-shield").isMegaStone)
    assertFalse(ItemName("").isMegaStone)
  }
}
