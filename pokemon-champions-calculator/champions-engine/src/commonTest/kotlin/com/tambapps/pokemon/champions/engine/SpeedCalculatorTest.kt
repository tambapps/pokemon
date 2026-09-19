package com.tambapps.pokemon.champions.engine

import kotlin.test.Test
import kotlin.test.assertEquals

class SpeedCalculatorTest {

  // Garchomp, base Speed 102, 0 Stat Points, neutral nature -> raw Speed stat of 122.
  private fun garchomp(ability: String = "", item: String? = null, status: Status = Status.HEALTHY) =
    testPokemon("Garchomp", ability = ability, item = item, status = status)

  private fun speed(pokemon: BattlePokemon, field: Battlefield = Battlefield()) =
    SpeedCalculator.effectiveSpeed(pokemon, field, isAttackerSide = true)

  @Test
  fun rawSpeedWithNoModifiers() {
    assertEquals(122, speed(garchomp()))
  }

  @Test
  fun choiceScarfBoostsSpeedByHalf() {
    assertEquals(183, speed(garchomp(item = "Choice Scarf")))
  }

  @Test
  fun ironBallHalvesSpeed() {
    assertEquals(61, speed(garchomp(item = "Iron Ball")))
  }

  @Test
  fun chlorophyllDoublesSpeedInSun() {
    assertEquals(244, speed(garchomp(ability = "Chlorophyll"), Battlefield(weather = Weather.SUN)))
  }

  @Test
  fun chlorophyllDoesNothingWithoutSun() {
    assertEquals(122, speed(garchomp(ability = "Chlorophyll")))
  }

  @Test
  fun swiftSwimDoublesSpeedInRain() {
    assertEquals(244, speed(garchomp(ability = "Swift Swim"), Battlefield(weather = Weather.RAIN)))
  }

  @Test
  fun sandRushDoublesSpeedInSand() {
    assertEquals(244, speed(garchomp(ability = "Sand Rush"), Battlefield(weather = Weather.SAND)))
  }

  @Test
  fun slushRushDoublesSpeedInHailOrSnow() {
    assertEquals(244, speed(garchomp(ability = "Slush Rush"), Battlefield(weather = Weather.HAIL)))
    assertEquals(244, speed(garchomp(ability = "Slush Rush"), Battlefield(weather = Weather.SNOW)))
  }

  @Test
  fun surgeSurferDoublesSpeedInElectricTerrain() {
    assertEquals(244, speed(garchomp(ability = "Surge Surfer"), Battlefield(terrain = Terrain.ELECTRIC)))
  }

  @Test
  fun unburdenDoublesSpeedWithNoItemHeld() {
    assertEquals(244, speed(garchomp(ability = "Unburden")))
  }

  @Test
  fun tailwindDoublesSpeedOnThatSide() {
    val field = Battlefield(attackerSide = SideConditions(hasTailwind = true))
    assertEquals(244, speed(garchomp(), field))
  }

  @Test
  fun tailwindOnlyAppliesToItsOwnSide() {
    val field = Battlefield(defenderSide = SideConditions(hasTailwind = true))
    assertEquals(122, speed(garchomp(), field)) // checked as the attacker side, so the defender's Tailwind doesn't apply
  }

  @Test
  fun paralysisHalvesSpeed() {
    assertEquals(61, speed(garchomp(status = Status.PARALYZED)))
  }

  @Test
  fun quickFeetBoostsInsteadOfBeingHalvedWhileParalyzed() {
    assertEquals(183, speed(garchomp(ability = "Quick Feet", status = Status.PARALYZED)))
  }

  @Test
  fun modifiersStackBeforeTheSpeedCap() {
    // Choice Scarf (1.5x) and Tailwind (2x) both apply: 122 * 1.5 * 2 = 366
    val field = Battlefield(attackerSide = SideConditions(hasTailwind = true))
    assertEquals(366, speed(garchomp(item = "Choice Scarf"), field))
  }
}
