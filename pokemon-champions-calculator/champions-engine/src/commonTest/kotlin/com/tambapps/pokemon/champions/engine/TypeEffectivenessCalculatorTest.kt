package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import kotlin.test.Test
import kotlin.test.assertEquals

class TypeEffectivenessCalculatorTest {

  private val nonScrappyAttacker = testPokemon("Garchomp")

  private fun effectiveness(move: String, effectiveType: PokeType, defender: BattlePokemon, attacker: BattlePokemon = nonScrappyAttacker, field: Battlefield = Battlefield()) =
    TypeEffectivenessCalculator.effectivenessOf(testMove(move), effectiveType, attacker, defender, field)

  @Test
  fun multipliesBothOfTheDefendersTypes() {
    // Ice vs Dragon = 2x, Ice vs Ground = 2x -> 4x total against Garchomp (Dragon/Ground)
    val garchomp = testPokemon("Garchomp")
    assertEquals(4.0, effectiveness("Ice Beam", PokeType.ICE, garchomp))
  }

  @Test
  fun normalMoveIsImmuneAgainstGhostByDefault() {
    val gengar = testPokemon("Gengar") // Ghost/Poison
    assertEquals(0.0, effectiveness("Double-Edge", PokeType.NORMAL, gengar))
  }

  @Test
  fun scrappyOnTheAttackerBypassesGhostImmunityToNormalAndFighting() {
    val gengar = testPokemon("Gengar")
    val scrappyAttacker = testPokemon("Garchomp", ability = "Scrappy")
    // Normal vs Poison (Gengar's other type) is neutral, so bypassing Ghost's immunity leaves 1x.
    assertEquals(1.0, effectiveness("Double-Edge", PokeType.NORMAL, gengar, attacker = scrappyAttacker))
  }

  @Test
  fun scrappyOnTheDefenderHasNoEffect() {
    // Scrappy is an offensive ability; giving it to the defender must not bypass its own immunity.
    val gengar = testPokemon("Gengar", ability = "Scrappy")
    assertEquals(0.0, effectiveness("Double-Edge", PokeType.NORMAL, gengar))
  }

  @Test
  fun freezeDryIsAlwaysSuperEffectiveAgainstWater() {
    val vaporeon = testPokemon("Vaporeon") // mono-Water, so nothing from a second type complicates the multiplier
    assertEquals(2.0, effectiveness("Freeze-Dry", PokeType.WATER, vaporeon))
  }

  @Test
  fun flyingPressAppliesBothFightingAndFlyingEffectiveness() {
    // Fighting vs Bug = 0.5x, Flying vs Bug = 2x -> 1x combined against mono-Bug Pinsir
    val pinsir = testPokemon("Pinsir")
    assertEquals(1.0, effectiveness("Flying Press", PokeType.FIGHTING, pinsir))
  }

  @Test
  fun groundIsImmuneAgainstFlyingByDefault() {
    val charizard = testPokemon("Charizard") // Fire/Flying
    assertEquals(0.0, effectiveness("Earthquake", PokeType.GROUND, charizard))
  }

  @Test
  fun gravityRemovesGroundsImmunityToFlyingTypes() {
    val charizard = testPokemon("Charizard")
    // Ground vs Fire = 2x, Ground vs Flying (bypassed by Gravity) = 1x -> 2x combined
    assertEquals(2.0, effectiveness("Earthquake", PokeType.GROUND, charizard, field = Battlefield(isGravity = true)))
  }

  @Test
  fun defenderHoldingIronBallLosesGroundImmunityToo() {
    val charizard = testPokemon("Charizard", item = "Iron Ball")
    assertEquals(2.0, effectiveness("Earthquake", PokeType.GROUND, charizard))
  }

  @Test
  fun struggleIsAlwaysNeutralRegardlessOfDefenderTypes() {
    val gengar = testPokemon("Gengar") // would normally be immune to Normal
    assertEquals(1.0, effectiveness("Struggle", PokeType.NORMAL, gengar))
  }
}
