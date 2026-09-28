package com.tambapps.pokemon.champions.engine

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GroundedTest {

  @Test
  fun nonFlyingTypeWithNoItemOrAbilityIsGrounded() {
    val toxapex = testPokemon("Toxapex")
    assertTrue(toxapex.isGrounded(Battlefield(), SideConditions.NONE))
  }

  @Test
  fun flyingTypeIsAirborneByDefault() {
    val charizard = testPokemon("Charizard")
    assertFalse(charizard.isGrounded(Battlefield(), SideConditions.NONE))
  }

  @Test
  fun gravityGroundsEvenFlyingTypes() {
    val charizard = testPokemon("Charizard")
    assertTrue(charizard.isGrounded(Battlefield(isGravity = true), SideConditions.NONE))
  }

  @Test
  fun ironBallGroundsEvenFlyingTypes() {
    val charizard = testPokemon("Charizard", item = "Iron Ball")
    assertTrue(charizard.isGrounded(Battlefield(), SideConditions.NONE))
  }

  @Test
  fun ingrainGroundsEvenFlyingTypes() {
    val charizard = testPokemon("Charizard")
    assertTrue(charizard.isGrounded(Battlefield(), SideConditions(isIngrained = true)))
  }

  @Test
  fun eachPokemonIsGroundedByItsOwnSideIngrain() {
    val charizard = testPokemon("Charizard")
    val attackerIngrained = Battlefield(attackerSide = SideConditions(isIngrained = true))
    assertTrue(charizard.isAttackerGrounded(attackerIngrained))
    assertFalse(charizard.isDefenderGrounded(attackerIngrained))
    val defenderIngrained = Battlefield(defenderSide = SideConditions(isIngrained = true))
    assertFalse(charizard.isAttackerGrounded(defenderIngrained))
    assertTrue(charizard.isDefenderGrounded(defenderIngrained))
  }

  @Test
  fun airBalloonMakesAGroundTypeAirborne() {
    val toxapex = testPokemon("Toxapex", item = "Air Balloon")
    assertFalse(toxapex.isGrounded(Battlefield(), SideConditions.NONE))
  }

  @Test
  fun levitateMakesAGroundTypeAirborne() {
    val toxapex = testPokemon("Toxapex", ability = "Levitate")
    assertFalse(toxapex.isGrounded(Battlefield(), SideConditions.NONE))
  }

  @Test
  fun eelevateMakesAGroundTypeAirborne() {
    val toxapex = testPokemon("Toxapex", ability = "Eelevate")
    assertFalse(toxapex.isGrounded(Battlefield(), SideConditions.NONE))
  }

  @Test
  fun klutzSuppressesAirBalloonSoItsHolderStaysGrounded() {
    val toxapex = testPokemon("Toxapex", ability = "Klutz", item = "Air Balloon")
    assertTrue(toxapex.isGrounded(Battlefield(), SideConditions.NONE))
  }
}
