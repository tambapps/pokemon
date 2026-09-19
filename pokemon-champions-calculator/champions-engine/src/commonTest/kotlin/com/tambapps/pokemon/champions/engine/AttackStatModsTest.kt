package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import kotlin.test.Test
import kotlin.test.assertEquals

class AttackStatModsTest {

  private fun combinedMod(moveName: String, attacker: BattlePokemon, defender: BattlePokemon = testPokemon("Garchomp"), field: Battlefield = Battlefield()): Int {
    val move = testMove(moveName)
    val effectiveType = effectiveTypeOf(move, attacker, field)
    return chainMods(AttackStatMods.resolve(move, effectiveType, attacker, defender, field))
  }

  @Test
  fun noModifiersMeansNoChange() {
    assertEquals(0x1000, combinedMod("Earthquake", testPokemon("Garchomp")))
  }

  @Test
  fun gutsBoostsPhysicalMovesWhileStatused() {
    assertEquals(0x1800, combinedMod("Earthquake", testPokemon("Garchomp", ability = "Guts", status = Status.BURNED)))
  }

  @Test
  fun gutsDoesNothingWhileHealthy() {
    assertEquals(0x1000, combinedMod("Earthquake", testPokemon("Garchomp", ability = "Guts")))
  }

  @Test
  fun theHiddenPowerAbilitiesBoostTheirTypeBelowAThirdHp() {
    val maxHp = testPokemon("Venusaur", hp = 0).maxHp
    val lowHp = testPokemon("Venusaur", ability = "Overgrow", currentHp = maxHp / 4)
    assertEquals(0x1800, combinedMod("Giga Drain", lowHp))
  }

  @Test
  fun theHiddenPowerAbilitiesDoNothingAboveAThirdHp() {
    val healthy = testPokemon("Venusaur", ability = "Overgrow")
    assertEquals(0x1000, combinedMod("Giga Drain", healthy))
  }

  @Test
  fun flashFireBoostsFireMovesOnceActive() {
    val active = testPokemon("Charizard", ability = "Flash Fire", abilityIsActive = true)
    assertEquals(0x1800, combinedMod("Flamethrower", active))
  }

  @Test
  fun sharpnessBoostsSlicingMoves() {
    val sharp = testPokemon("Garchomp", ability = "Sharpness")
    assertEquals(0x1800, combinedMod("Solar Blade", sharp))
  }

  @Test
  fun plusAndMinusBoostSpecialMovesOnceActive() {
    val active = testPokemon("Garchomp", ability = "Plus", abilityIsActive = true)
    assertEquals(0x1800, combinedMod("Flamethrower", active))
  }

  @Test
  fun solarPowerBoostsSpecialMovesInSun() {
    val solarPower = testPokemon("Charizard", ability = "Solar Power")
    assertEquals(0x1800, combinedMod("Flamethrower", solarPower, field = Battlefield(weather = Weather.SUN)))
  }

  @Test
  fun thickFatHalvesIncomingFireAndIceMoves() {
    val thickFat = testPokemon("Toxapex", ability = "Thick Fat")
    assertEquals(0x800, combinedMod("Flamethrower", testPokemon("Charizard"), thickFat))
  }

  @Test
  fun purifyingSaltHalvesIncomingGhostMoves() {
    val purifyingSalt = testPokemon("Toxapex", ability = "Purifying Salt")
    assertEquals(0x800, combinedMod("Shadow Ball", testPokemon("Garchomp"), purifyingSalt))
  }

  @Test
  fun heatproofHalvesIncomingFireMoves() {
    val heatproof = testPokemon("Toxapex", ability = "Heatproof")
    assertEquals(0x800, combinedMod("Flamethrower", testPokemon("Charizard"), heatproof))
  }

  @Test
  fun waterBubbleBoostsTheUsersOwnWaterMovesAndHalvesIncomingFireMoves() {
    val userSide = testPokemon("Toxapex", ability = "Water Bubble")
    assertEquals(0x2000, combinedMod("Scald", userSide))
    val defenderSide = testPokemon("Toxapex", ability = "Water Bubble")
    assertEquals(0x800, combinedMod("Flamethrower", testPokemon("Charizard"), defenderSide))
  }

  @Test
  fun hugePowerAndPurePowerDoublePhysicalMoves() {
    assertEquals(0x2000, combinedMod("Earthquake", testPokemon("Kangaskhan", ability = "Huge Power")))
    assertEquals(0x2000, combinedMod("Earthquake", testPokemon("Medicham", ability = "Pure Power")))
  }

  @Test
  fun stakeoutDoublesDamageOnceActive() {
    val active = testPokemon("Garchomp", ability = "Stakeout", abilityIsActive = true)
    assertEquals(0x2000, combinedMod("Earthquake", active))
  }

  @Test
  fun lightBallDoublesPikachusAttack() {
    val pikachu = testPokemon("Pikachu", item = "Light Ball")
    assertEquals(0x2000, combinedMod("Thunder Punch", pikachu))
  }

  @Test
  fun lightBallDoesNothingForOtherSpecies() {
    val notPikachu = testPokemon("Garchomp", item = "Light Ball")
    assertEquals(0x1000, combinedMod("Earthquake", notPikachu))
  }
}
