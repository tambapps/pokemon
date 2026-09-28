package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.champions.data.Ability
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AbilityTogglesTest {

  @Test
  fun togglesFollowTheSourceCalculator() {
    for (ability in listOf(Ability.INTIMIDATE, Ability.PROTEAN, Ability.LIBERO, Ability.SUPERSWEET_SYRUP)) {
      assertTrue(ability.hasActiveToggle, "$ability")
      assertTrue(ability.isActiveByDefault, "$ability")
    }
    for (ability in listOf(
      Ability.FLASH_FIRE, Ability.PLUS, Ability.MINUS, Ability.TRACE, Ability.STAKEOUT,
      Ability.SAND_SPIT, Ability.ELECTROMORPHOSIS, Ability.SEED_SOWER,
    )) {
      assertTrue(ability.hasActiveToggle, "$ability")
      assertFalse(ability.isActiveByDefault, "$ability")
    }
  }

  @Test
  fun mostAbilitiesHaveNoToggle() {
    assertFalse(Ability.ROUGH_SKIN.hasActiveToggle)
    assertFalse(Ability.ROUGH_SKIN.isActiveByDefault)
    assertFalse(Ability.NO_ABILITY.hasActiveToggle)
    assertFalse(Ability.DROUGHT.hasActiveToggle)
  }

  @Test
  fun weatherAbilitiesSetTheWeather() {
    assertEquals(Weather.SUN, Ability.DROUGHT.weatherSetOnField(isActive = false))
    assertEquals(Weather.RAIN, Ability.DRIZZLE.weatherSetOnField(isActive = false))
    assertEquals(Weather.SAND, Ability.SAND_STREAM.weatherSetOnField(isActive = false))
    assertEquals(Weather.SNOW, Ability.SNOW_WARNING.weatherSetOnField(isActive = false))
    assertNull(Ability.INTIMIDATE.weatherSetOnField(isActive = true))
  }

  @Test
  fun sandSpitOnlySetsSandWhenActive() {
    assertEquals(Weather.SAND, Ability.SAND_SPIT.weatherSetOnField(isActive = true))
    assertNull(Ability.SAND_SPIT.weatherSetOnField(isActive = false))
  }

  @Test
  fun terrainAbilitiesSetTheTerrain() {
    assertEquals(Terrain.GRASSY, Ability.GRASSY_SURGE.terrainSetOnField(isActive = false))
    assertEquals(Terrain.ELECTRIC, Ability.ELECTRIC_SURGE.terrainSetOnField(isActive = false))
    assertEquals(Terrain.PSYCHIC, Ability.PSYCHIC_SURGE.terrainSetOnField(isActive = false))
    assertNull(Ability.DROUGHT.terrainSetOnField(isActive = false))
  }

  @Test
  fun seedSowerOnlySetsGrassyWhenActive() {
    assertEquals(Terrain.GRASSY, Ability.SEED_SOWER.terrainSetOnField(isActive = true))
    assertNull(Ability.SEED_SOWER.terrainSetOnField(isActive = false))
  }
}
