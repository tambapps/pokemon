package com.tambapps.pokemon.champions.engine

import kotlin.test.Test
import kotlin.test.assertEquals

class DefenseStatModsTest {

  private fun combinedMod(defender: BattlePokemon, field: Battlefield = Battlefield(), hitsPhysical: Boolean = true) =
    chainMods(DefenseStatMods.resolve(defender, field, hitsPhysical))

  @Test
  fun marvelScaleBoostsPhysicalDefenseWhileStatused() {
    val marvelScale = testPokemon("Toxapex", ability = "Marvel Scale", status = Status.PARALYZED)
    assertEquals(0x1800, combinedMod(marvelScale))
  }

  @Test
  fun marvelScaleDoesNothingWhileHealthyOrAgainstSpecialMoves() {
    assertEquals(0x1000, combinedMod(testPokemon("Toxapex", ability = "Marvel Scale")))
    val statused = testPokemon("Toxapex", ability = "Marvel Scale", status = Status.PARALYZED)
    assertEquals(0x1000, combinedMod(statused, hitsPhysical = false))
  }

  @Test
  fun grassPeltBoostsPhysicalDefenseInGrassyTerrain() {
    val grassPelt = testPokemon("Toxapex", ability = "Grass Pelt")
    assertEquals(0x1800, combinedMod(grassPelt, field = Battlefield(terrain = Terrain.GRASSY)))
    assertEquals(0x1000, combinedMod(grassPelt)) // no terrain
  }

  @Test
  fun furCoatDoublesPhysicalDefenseOnly() {
    val furCoat = testPokemon("Toxapex", ability = "Fur Coat")
    assertEquals(0x2000, combinedMod(furCoat))
    assertEquals(0x1000, combinedMod(furCoat, hitsPhysical = false))
  }
}
