package com.tambapps.pokemon.champions.engine

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GroundedTest {

  @Test
  fun nonFlyingTypeWithNoItemOrAbilityIsGrounded() {
    val toxapex = testPokemon("Toxapex")
    assertTrue(toxapex.isGrounded(Battlefield()))
  }

  @Test
  fun flyingTypeIsAirborneByDefault() {
    val charizard = testPokemon("Charizard")
    assertFalse(charizard.isGrounded(Battlefield()))
  }

  @Test
  fun gravityGroundsEvenFlyingTypes() {
    val charizard = testPokemon("Charizard")
    assertTrue(charizard.isGrounded(Battlefield(isGravity = true)))
  }

  @Test
  fun ironBallGroundsEvenFlyingTypes() {
    val charizard = testPokemon("Charizard", item = "Iron Ball")
    assertTrue(charizard.isGrounded(Battlefield()))
  }

  @Test
  fun airBalloonMakesAGroundTypeAirborne() {
    val toxapex = testPokemon("Toxapex", item = "Air Balloon")
    assertFalse(toxapex.isGrounded(Battlefield()))
  }

  @Test
  fun levitateMakesAGroundTypeAirborne() {
    val toxapex = testPokemon("Toxapex", ability = "Levitate")
    assertFalse(toxapex.isGrounded(Battlefield()))
  }

  @Test
  fun eelevateMakesAGroundTypeAirborne() {
    val toxapex = testPokemon("Toxapex", ability = "Eelevate")
    assertFalse(toxapex.isGrounded(Battlefield()))
  }

  @Test
  fun klutzSuppressesAirBalloonSoItsHolderStaysGrounded() {
    val toxapex = testPokemon("Toxapex", ability = "Klutz", item = "Air Balloon")
    assertTrue(toxapex.isGrounded(Battlefield()))
  }
}
