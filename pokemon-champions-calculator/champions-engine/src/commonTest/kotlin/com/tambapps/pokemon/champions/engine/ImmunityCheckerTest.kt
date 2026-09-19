package com.tambapps.pokemon.champions.engine

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImmunityCheckerTest {

  private val plainAttacker = testPokemon("Garchomp")

  private fun isImmune(moveName: String, defender: BattlePokemon, attacker: BattlePokemon = plainAttacker, field: Battlefield = Battlefield()): Boolean {
    val move = testMove(moveName)
    return ImmunityChecker.isImmune(move, move.type, attacker, defender, field)
  }

  @Test
  fun sapSipperBlocksGrassMoves() {
    val sapSipper = testPokemon("Toxapex", ability = "Sap Sipper")
    assertTrue(isImmune("Giga Drain", sapSipper))
  }

  @Test
  fun flashFireBlocksFireMoves() {
    val flashFire = testPokemon("Toxapex", ability = "Flash Fire")
    assertTrue(isImmune("Fire Blast", flashFire))
  }

  @Test
  fun dryOrWaterAbsorbBlocksWaterMoves() {
    val drySkin = testPokemon("Toxapex", ability = "Dry Skin")
    val waterAbsorb = testPokemon("Toxapex", ability = "Water Absorb")
    assertTrue(isImmune("Scald", drySkin))
    assertTrue(isImmune("Scald", waterAbsorb))
  }

  @Test
  fun motorDriveVoltAbsorbAndLightningRodBlockElectricMoves() {
    for (ability in listOf("Motor Drive", "Volt Absorb", "Lightning Rod")) {
      assertTrue(isImmune("Thunderbolt", testPokemon("Toxapex", ability = ability)), "expected $ability to block Thunderbolt")
    }
  }

  @Test
  fun earthEaterBlocksGroundMoves() {
    val earthEater = testPokemon("Toxapex", ability = "Earth Eater")
    assertTrue(isImmune("Earthquake", earthEater))
  }

  @Test
  fun levitateAndAirBalloonBlockGroundMoves() {
    assertTrue(isImmune("Earthquake", testPokemon("Toxapex", ability = "Levitate")))
    assertTrue(isImmune("Earthquake", testPokemon("Toxapex", item = "Air Balloon")))
  }

  @Test
  fun gravityRemovesTheLevitateAndAirBalloonGroundImmunity() {
    val levitating = testPokemon("Toxapex", ability = "Levitate")
    assertFalse(isImmune("Earthquake", levitating, field = Battlefield(isGravity = true)))
  }

  @Test
  fun bulletproofBlocksBulletMoves() {
    val bulletproof = testPokemon("Toxapex", ability = "Bulletproof")
    assertTrue(isImmune("Bullet Seed", bulletproof))
  }

  @Test
  fun soundproofBlocksSoundMoves() {
    val soundproof = testPokemon("Toxapex", ability = "Soundproof")
    assertTrue(isImmune("Hyper Voice", soundproof))
  }

  @Test
  fun dampBlocksExplosiveMovesFromEitherSide() {
    val dampDefender = testPokemon("Toxapex", ability = "Damp")
    val dampAttacker = testPokemon("Garchomp", ability = "Damp")
    val plainDefender = testPokemon("Toxapex")
    assertTrue(isImmune("Explosion", dampDefender))
    assertTrue(isImmune("Explosion", plainDefender, attacker = dampAttacker))
  }

  @Test
  fun sturdyBlocksOhkoMoves() {
    val sturdy = testPokemon("Toxapex", ability = "Sturdy")
    assertTrue(isImmune("Sheer Cold", sturdy))
  }

  @Test
  fun sturdyDoesNotBlockRegularMoves() {
    val sturdy = testPokemon("Toxapex", ability = "Sturdy")
    assertFalse(isImmune("Earthquake", sturdy))
  }

  @Test
  fun queenlyMajestyAndArmorTailBlockPriorityMoves() {
    assertTrue(isImmune("Quick Attack", testPokemon("Toxapex", ability = "Queenly Majesty")))
    assertTrue(isImmune("Quick Attack", testPokemon("Toxapex", ability = "Armor Tail")))
  }

  @Test
  fun queenlyMajestyDoesNotBlockNonPriorityMoves() {
    assertFalse(isImmune("Earthquake", testPokemon("Toxapex", ability = "Queenly Majesty")))
  }

  @Test
  fun psychicTerrainBlocksPriorityMovesAgainstGroundedTargets() {
    val grounded = testPokemon("Toxapex")
    assertTrue(isImmune("Quick Attack", grounded, field = Battlefield(terrain = Terrain.PSYCHIC)))
  }

  @Test
  fun psychicTerrainDoesNotBlockPriorityAgainstAirborneTargets() {
    val airborne = testPokemon("Charizard") // Flying-type, not grounded
    assertFalse(isImmune("Quick Attack", airborne, field = Battlefield(terrain = Terrain.PSYCHIC)))
  }

  @Test
  fun typeImmunityStillAppliesThroughIsImmune() {
    // Ground move vs a Flying-type: no ability involved, just the type chart.
    assertTrue(isImmune("Earthquake", testPokemon("Charizard")))
  }

  @Test
  fun normalMoveAgainstANonImmuneTargetIsNotImmune() {
    assertFalse(isImmune("Earthquake", testPokemon("Toxapex")))
  }
}
