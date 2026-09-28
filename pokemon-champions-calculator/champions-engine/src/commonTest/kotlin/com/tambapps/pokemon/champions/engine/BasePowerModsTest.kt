package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.Gender
import kotlin.test.Test
import kotlin.test.assertEquals

class BasePowerModsTest {

  private fun combinedMod(
    moveName: String,
    attacker: BattlePokemon,
    defender: BattlePokemon = testPokemon("Toxapex"),
    field: Battlefield = Battlefield(),
    moveUse: MoveUse? = null,
    basePower: Int = testMove(moveName).basePower,
  ): Int {
    val move = testMove(moveName)
    val effectiveType = effectiveTypeOf(move, attacker, field)
    val use = moveUse ?: MoveUse(move)
    return chainMods(BasePowerMods.resolve(basePower, move, effectiveType, use, attacker, defender, field))
  }

  @Test
  fun rivalryBoostsSameGenderAndWeakensOppositeGender() {
    val male = testPokemon("Garchomp", ability = "Rivalry", gender = Gender.MALE)
    assertEquals(0x1400, combinedMod("Earthquake", male, testPokemon("Toxapex", gender = Gender.MALE)))
    assertEquals(0x0C00, combinedMod("Earthquake", male, testPokemon("Toxapex", gender = Gender.FEMALE)))
  }

  @Test
  fun rivalryDoesNothingWhenEitherSideIsGenderless() {
    val male = testPokemon("Garchomp", ability = "Rivalry", gender = Gender.MALE)
    assertEquals(0x1000, combinedMod("Earthquake", male, testPokemon("Toxapex", gender = Gender.ASEXUAL)))
  }

  @Test
  fun ateAbilitiesBoostTheRetypedMoveOnTopOfRetyping() {
    val aerilate = testPokemon("Garchomp", ability = "Aerilate")
    assertEquals(0x1333, combinedMod("Double-Edge", aerilate))
  }

  @Test
  fun recklessBoostsRecoilMoves() {
    val reckless = testPokemon("Garchomp", ability = "Reckless")
    assertEquals(0x1333, combinedMod("Double-Edge", reckless))
  }

  @Test
  fun ironFistBoostsPunchingMoves() {
    val ironFist = testPokemon("Garchomp", ability = "Iron Fist")
    assertEquals(0x1333, combinedMod("Thunder Punch", ironFist))
  }

  @Test
  fun allyFieldAbilitiesBoostMatchingMoves() {
    val battery = Battlefield(attackerSide = SideConditions(hasBattery = true))
    assertEquals(0x14CD, combinedMod("Flamethrower", testPokemon("Charizard"), field = battery)) // Special move
    assertEquals(0x1000, combinedMod("Earthquake", testPokemon("Garchomp"), field = battery)) // Physical, Battery doesn't apply

    val powerSpot = Battlefield(attackerSide = SideConditions(hasPowerSpot = true))
    assertEquals(0x14CD, combinedMod("Earthquake", testPokemon("Garchomp"), field = powerSpot))

    val allySteelySpirit = Battlefield(attackerSide = SideConditions(hasAllySteelySpirit = true))
    assertEquals(0x1800, combinedMod("Iron Head", testPokemon("Garchomp"), field = allySteelySpirit))
  }

  @Test
  fun sheerForceBoostsMovesWithASecondaryEffect() {
    val sheerForce = testPokemon("Garchomp", ability = "Sheer Force")
    assertEquals(0x14CD, combinedMod("Flamethrower", sheerForce)) // has a secondary effect
    assertEquals(0x1000, combinedMod("Dragon Claw", sheerForce)) // no secondary effect
  }

  @Test
  fun sandForceBoostsRockGroundAndSteelMovesInSand() {
    val sandForce = testPokemon("Garchomp", ability = "Sand Force")
    assertEquals(0x14CD, combinedMod("Earthquake", sandForce, field = Battlefield(weather = Weather.SAND)))
    assertEquals(0x1000, combinedMod("Earthquake", sandForce)) // no sand
  }

  @Test
  fun analyticBoostsWhenMovingSecond() {
    val slowAnalytic = testPokemon("Toxapex", ability = "Analytic") // much slower than Garchomp
    assertEquals(0x14CD, combinedMod("Earthquake", slowAnalytic, testPokemon("Garchomp")))
  }

  @Test
  fun analyticDoesNothingWhenMovingFirst() {
    val fastAnalytic = testPokemon("Garchomp", ability = "Analytic")
    assertEquals(0x1000, combinedMod("Earthquake", fastAnalytic, testPokemon("Toxapex")))
  }

  @Test
  fun toughClawsBoostsContactMoves() {
    val toughClaws = testPokemon("Garchomp", ability = "Tough Claws")
    assertEquals(0x14CD, combinedMod("Bug Bite", toughClaws))
    assertEquals(0x1000, combinedMod("Flamethrower", toughClaws)) // no contact
  }

  @Test
  fun punkRockBoostsSoundMoves() {
    val punkRock = testPokemon("Garchomp", ability = "Punk Rock")
    assertEquals(0x14CD, combinedMod("Hyper Voice", punkRock))
  }

  @Test
  fun technicianBoostsMovesAtSixtyPowerOrBelowOnly() {
    val technician = testPokemon("Garchomp", ability = "Technician")
    assertEquals(0x1800, combinedMod("Bug Bite", technician, basePower = 60))
    assertEquals(0x1000, combinedMod("Bug Bite", technician, basePower = 61))
  }

  @Test
  fun megaLauncherBoostsPulseMoves() {
    val megaLauncher = testPokemon("Garchomp", ability = "Mega Launcher")
    assertEquals(0x1800, combinedMod("Dragon Pulse", megaLauncher))
  }

  @Test
  fun strongJawBoostsBitingMoves() {
    val strongJaw = testPokemon("Garchomp", ability = "Strong Jaw")
    assertEquals(0x1800, combinedMod("Crunch", strongJaw))
  }

  @Test
  fun steelySpiritBoostsTheUsersOwnSteelMoves() {
    val steelySpirit = testPokemon("Garchomp", ability = "Steely Spirit")
    val steelMove = testMove("Iron Head")
    assertEquals(0x1800, chainMods(BasePowerMods.resolve(steelMove.basePower, steelMove, steelMove.type, MoveUse(steelMove), steelySpirit, testPokemon("Toxapex"), Battlefield())))
  }

  @Test
  fun drySkinIncreasesIncomingFireDamage() {
    val drySkin = testPokemon("Toxapex", ability = "Dry Skin")
    assertEquals(0x1400, combinedMod("Flamethrower", testPokemon("Charizard"), drySkin))
  }

  @Test
  fun physicalAndSpecialBoostingItemsApplyToTheMatchingCategoryOnly() {
    assertEquals(0x1199, combinedMod("Earthquake", testPokemon("Garchomp", item = "Muscle Band")))
    assertEquals(0x1000, combinedMod("Flamethrower", testPokemon("Charizard", item = "Muscle Band")))
    assertEquals(0x1199, combinedMod("Flamethrower", testPokemon("Charizard", item = "Wise Glasses")))
  }

  @Test
  fun typeBoostingItemsBoostTheirMatchingType() {
    assertEquals(0x1333, combinedMod("Flamethrower", testPokemon("Charizard", item = "Charcoal")))
  }

  @Test
  fun solarBeamAndSolarBladeAreWeakenedByNonSunWeather() {
    val attacker = testPokemon("Venusaur")
    assertEquals(0x800, combinedMod("Solar Beam", attacker, field = Battlefield(weather = Weather.RAIN)))
    assertEquals(0x1000, combinedMod("Solar Beam", attacker, field = Battlefield(weather = Weather.SUN)))
    assertEquals(0x1000, combinedMod("Solar Beam", attacker))
  }

  @Test
  fun helpingHandBoostsDamage() {
    val field = Battlefield(attackerSide = SideConditions(hasHelpingHand = true))
    assertEquals(0x1800, combinedMod("Earthquake", testPokemon("Garchomp"), field = field))
  }

  @Test
  fun chargeAndElectromorphosisBoostElectricMoves() {
    val chargeField = Battlefield(isCharge = true)
    assertEquals(0x2000, combinedMod("Thunder Punch", testPokemon("Garchomp"), field = chargeField))

    val electromorphosis = testPokemon("Garchomp", ability = "Electromorphosis", abilityIsActive = true)
    assertEquals(0x2000, combinedMod("Thunder Punch", electromorphosis))
  }

  @Test
  fun facadeDoublesWhileTheUserIsStatused() {
    val statused = testPokemon("Garchomp", status = Status.PARALYZED)
    assertEquals(0x2000, combinedMod("Facade", statused))
    assertEquals(0x1000, combinedMod("Facade", testPokemon("Garchomp")))
  }

  @Test
  fun venoshockAndBarbBarrageDoubleAgainstAPoisonedTarget() {
    val poisoned = testPokemon("Toxapex", status = Status.POISONED)
    assertEquals(0x2000, combinedMod("Venoshock", testPokemon("Garchomp"), poisoned))
    assertEquals(0x2000, combinedMod("Barb Barrage", testPokemon("Garchomp"), poisoned))
  }

  @Test
  fun electricTerrainBoostsGroundedElectricMovesOnly() {
    val field = Battlefield(terrain = Terrain.ELECTRIC)
    assertEquals(0x14CD, combinedMod("Thunder Punch", testPokemon("Toxapex"), field = field)) // grounded
    assertEquals(0x1000, combinedMod("Thunder Punch", testPokemon("Charizard"), field = field)) // Flying-type, airborne
  }

  @Test
  fun mistyTerrainWeakensDragonMovesAgainstGroundedTargets() {
    val field = Battlefield(terrain = Terrain.MISTY)
    val groundedDefender = testPokemon("Toxapex")
    assertEquals(0x800, combinedMod("Dragon Claw", testPokemon("Garchomp"), groundedDefender, field))
  }

  @Test
  fun grassyTerrainWeakensEarthquakeAndBulldozeAgainstGroundedTargets() {
    val field = Battlefield(terrain = Terrain.GRASSY)
    val groundedDefender = testPokemon("Toxapex")
    assertEquals(0x800, combinedMod("Earthquake", testPokemon("Garchomp"), groundedDefender, field))
    assertEquals(0x800, combinedMod("Bulldoze", testPokemon("Garchomp"), groundedDefender, field))
  }

  @Test
  fun supremeOverlordScalesWithFaintedAllies() {
    val supremeOverlord = testPokemon("Garchomp", ability = "Supreme Overlord")
    val move = testMove("Earthquake")
    assertEquals(0x1000, combinedMod("Earthquake", supremeOverlord, moveUse = MoveUse(move, faintedAllyCount = 0)))
    assertEquals(0x119A, combinedMod("Earthquake", supremeOverlord, moveUse = MoveUse(move, faintedAllyCount = 1)))
    assertEquals(0x1800, combinedMod("Earthquake", supremeOverlord, moveUse = MoveUse(move, faintedAllyCount = 5)))
  }

  @Test
  fun fairyAuraBoostsFairyMovesOnly() {
    val auraField = Battlefield(isFairyAura = true)
    assertEquals(0x1548, combinedMod("Moonblast", testPokemon("Sylveon"), field = auraField))
    assertEquals(0x1000, combinedMod("Moonblast", testPokemon("Sylveon")))
    assertEquals(0x1000, combinedMod("Hyper Voice", testPokemon("Sylveon"), field = auraField))
  }

  @Test
  fun fairyAuraAppliesToMovesRetypedToFairy() {
    val pixilate = testPokemon("Sylveon", ability = "Pixilate")
    assertEquals(chainMods(listOf(0x1333, 0x1548)), combinedMod("Hyper Voice", pixilate, field = Battlefield(isFairyAura = true)))
  }

  @Test
  fun knockOffBoostsWhenTheTargetHoldsAnItem() {
    assertEquals(0x1800, combinedMod("Knock Off", testPokemon("Garchomp"), testPokemon("Toxapex", item = "Leftovers")))
    assertEquals(0x1000, combinedMod("Knock Off", testPokemon("Garchomp"), testPokemon("Toxapex")))
  }
}
