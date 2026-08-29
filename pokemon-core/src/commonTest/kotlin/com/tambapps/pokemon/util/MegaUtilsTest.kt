package com.tambapps.pokemon.util

import com.tambapps.pokemon.ItemName
import com.tambapps.pokemon.PokemonName
import com.tambapps.pokemon.util.MegaUtils.canMega
import com.tambapps.pokemon.util.MegaUtils.getMegaPokemons
import com.tambapps.pokemon.util.MegaUtils.isMegaStone
import com.tambapps.pokemon.util.MegaUtils.toMega
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
    assertEquals(PokemonName("floette-mega"), MegaUtils.getMegaPokemon(MegaUtils.FLOETTITE))
    assertEquals(PokemonName("floette-mega"), MegaUtils.getMegaPokemon(MegaUtils.FLOETTITE))
  }

  @Test
  fun testDualFormMappings() {
    assertEquals(PokemonName("charizard-mega-x"), MegaUtils.getMegaPokemon(MegaUtils.CHARIZARDITE_X))
    assertEquals(PokemonName("charizard-mega-y"), MegaUtils.getMegaPokemon(MegaUtils.CHARIZARDITE_Y))
    assertEquals(PokemonName("mewtwo-mega-x"), MegaUtils.getMegaPokemon(MegaUtils.MEWTWONITE_X))
    assertEquals(PokemonName("mewtwo-mega-y"), MegaUtils.getMegaPokemon(MegaUtils.MEWTWONITE_Y))
    assertEquals(PokemonName("raichu-mega-x"), MegaUtils.getMegaPokemon(MegaUtils.RAICHUNITE_X))
    assertEquals(PokemonName("raichu-mega-y"), MegaUtils.getMegaPokemon(MegaUtils.RAICHUNITE_Y))
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

  @Test
  fun testToMega() {
    assertEquals(PokemonName("venusaur-mega"), PokemonName("venusaur").toMega(MegaUtils.VENUSAURITE))
    assertEquals(PokemonName("gengar-mega"), PokemonName("gengar").toMega(MegaUtils.GENGARITE))
    assertEquals(PokemonName("lucario-mega"), PokemonName("lucario").toMega(MegaUtils.LUCARIONITE))
  }

  @Test
  fun testToMegaDualForm() {
    assertEquals(PokemonName("charizard-mega-x"), PokemonName("charizard").toMega(MegaUtils.CHARIZARDITE_X))
    assertEquals(PokemonName("charizard-mega-y"), PokemonName("charizard").toMega(MegaUtils.CHARIZARDITE_Y))
    assertEquals(PokemonName("mewtwo-mega-x"), PokemonName("mewtwo").toMega(MegaUtils.MEWTWONITE_X))
    assertEquals(PokemonName("mewtwo-mega-y"), PokemonName("mewtwo").toMega(MegaUtils.MEWTWONITE_Y))
  }

  @Test
  fun testToMegaRaichu() {
    assertEquals(PokemonName("raichu-mega-x"), PokemonName("raichu").toMega(MegaUtils.RAICHUNITE_X))
    assertEquals(PokemonName("raichu-mega-y"), PokemonName("raichu").toMega(MegaUtils.RAICHUNITE_Y))
    assertNull(PokemonName("raichu").toMega(MegaUtils.CHARIZARDITE_X))
    assertNull(PokemonName("pikachu").toMega(MegaUtils.RAICHUNITE_X))
    assertTrue(PokemonName("raichu").canMega)
  }

  @Test
  fun testToMegaWrongPokemon() {
    assertNull(PokemonName("venusaur").toMega(MegaUtils.GENGARITE))
    assertNull(PokemonName("charizard").toMega(MegaUtils.LUCARIONITE))
    assertNull(PokemonName("mewtwo").toMega(MegaUtils.CHARIZARDITE_X))
  }

  @Test
  fun testToMegaNonMegaStone() {
    assertNull(PokemonName("venusaur").toMega(ItemName("choice-band")))
    assertNull(PokemonName("charizard").toMega(ItemName("")))
  }

  @Test
  fun testToMegaNoMegaEvolution() {
    assertNull(PokemonName("pikachu").toMega(ItemName("pikachite")))
  }

  @Test
  fun testGetMegaPokemons() {
    assertEquals(setOf(PokemonName("charizard-mega-x"), PokemonName("charizard-mega-y")), getMegaPokemons(PokemonName("charizard")))
    assertEquals(setOf(PokemonName("raichu-mega-x"), PokemonName("raichu-mega-y")), getMegaPokemons(PokemonName("raichu")))
    assertEquals(setOf(PokemonName("floette-mega")), getMegaPokemons(PokemonName("floette-eternal")))
  }

  @Test
  fun testCanMega() {
    assertTrue(PokemonName("venusaur").canMega)
    assertTrue(PokemonName("gengar").canMega)
    assertTrue(PokemonName("charizard").canMega)
    assertTrue(PokemonName("charizard").canMega)
    assertTrue(PokemonName("mewtwo").canMega)
    assertTrue(PokemonName("floette-eternal").canMega)
    assertTrue(PokemonName("floette").canMega)
  }

  @Test
  fun testCanMega_normalization() {
    assertTrue(PokemonName("Venusaur").canMega)
    assertTrue(PokemonName("Charizard").canMega)
  }

  @Test
  fun testCanMega_false() {
    assertFalse(PokemonName("arceus").canMega)
    assertFalse(PokemonName("pikachu").canMega)
  }
}
