package com.tambapps.pokemon.champions.engine

import kotlin.test.Test
import kotlin.test.assertEquals

class BasePowerResolverTest {

  // Garchomp: Speed 122 (raw, 0 SP), weight 95kg.
  // Toxapex: Speed 55 (raw, 0 SP), weight 14.5kg, HP 125 (0 SP).
  private val garchomp = testPokemon("Garchomp")
  private val toxapex = testPokemon("Toxapex")

  private fun basePower(moveName: String, attacker: BattlePokemon, defender: BattlePokemon, moveUse: MoveUse = MoveUse(testMove(moveName)), field: Battlefield = Battlefield()): Int =
    BasePowerResolver.resolve(testMove(moveName), moveUse, attacker, defender, field)

  @Test
  fun movesWithoutACustomFormulaKeepTheirStaticPower() {
    assertEquals(100, basePower("Earthquake", garchomp, toxapex))
  }

  @Test
  fun gyroBallScalesWithTheDefendersSpeedAdvantage() {
    assertEquals(12, basePower("Gyro Ball", garchomp, toxapex))
  }

  @Test
  fun electroBallScalesWithTheAttackersSpeedAdvantage() {
    assertEquals(80, basePower("Electro Ball", garchomp, toxapex))
  }

  @Test
  fun lowKickAndGrassKnotScaleWithDefenderWeight() {
    assertEquals(40, basePower("Low Kick", garchomp, toxapex)) // Toxapex 14.5kg -> the >=10kg tier
    assertEquals(40, basePower("Grass Knot", garchomp, toxapex))
  }

  @Test
  fun heavySlamAndHeatCrashScaleWithTheWeightRatio() {
    assertEquals(120, basePower("Heavy Slam", garchomp, toxapex)) // 95kg / 14.5kg = 6.55x -> the >=5x tier
    assertEquals(120, basePower("Heat Crash", garchomp, toxapex))
  }

  @Test
  fun eruptionAndWaterSpoutScaleWithTheAttackersRemainingHp() {
    val fullHp = testPokemon("Toxapex")
    val lowHp = testPokemon("Toxapex", currentHp = 13)
    assertEquals(150, basePower("Eruption", fullHp, garchomp))
    assertEquals(15, basePower("Eruption", lowHp, garchomp))
  }

  @Test
  fun flailAndReversalGetStrongerAsTheAttackersHpDrops() {
    val fullHp = testPokemon("Toxapex")
    val lowHp = testPokemon("Toxapex", currentHp = 13)
    val nearDeath = testPokemon("Toxapex", currentHp = 1)
    assertEquals(20, basePower("Flail", fullHp, garchomp))
    assertEquals(150, basePower("Flail", lowHp, garchomp))
    assertEquals(200, basePower("Flail", nearDeath, garchomp))
  }

  @Test
  fun hardPressScalesWithTheDefendersRemainingHp() {
    val fullHp = testPokemon("Toxapex")
    val halfHp = testPokemon("Toxapex", currentHp = 62)
    assertEquals(100, basePower("Hard Press", garchomp, fullHp))
    assertEquals(49, basePower("Hard Press", garchomp, halfHp))
  }

  @Test
  fun storedPowerAndPowerTripScaleWithTheUsersPositiveBoosts() {
    val boosted = testPokemon("Garchomp", boosts = StatBoosts(attack = 2, defense = 1, specialDefense = 1))
    assertEquals(100, basePower("Stored Power", boosted, toxapex))
    assertEquals(100, basePower("Power Trip", boosted, toxapex))
  }

  @Test
  fun storedPowerIgnoresNegativeBoosts() {
    val mixed = testPokemon("Garchomp", boosts = StatBoosts(attack = 2, defense = -2))
    assertEquals(60, basePower("Stored Power", mixed, toxapex)) // 20 + 20*2 (only the +2 Attack counts)
  }

  @Test
  fun acrobaticsIsStrongerWithNoHeldItem() {
    assertEquals(110, basePower("Acrobatics", testPokemon("Garchomp"), toxapex))
    assertEquals(55, basePower("Acrobatics", testPokemon("Garchomp", item = "Leftovers"), toxapex))
  }

  @Test
  fun hexAndInfernalParadeDoubleAgainstAStatusedTarget() {
    assertEquals(65, basePower("Hex", garchomp, testPokemon("Toxapex")))
    assertEquals(130, basePower("Hex", garchomp, testPokemon("Toxapex", status = Status.BURNED)))
    assertEquals(65, basePower("Infernal Parade", garchomp, testPokemon("Toxapex")))
    assertEquals(130, basePower("Infernal Parade", garchomp, testPokemon("Toxapex", status = Status.PARALYZED)))
  }

  @Test
  fun lastRespectsAndRageFistStackWithPriorPowerBoosts() {
    val move = testMove("Last Respects")
    assertEquals(50, basePower("Last Respects", garchomp, toxapex, moveUse = MoveUse(move, priorPowerBoosts = 0)))
    assertEquals(150, basePower("Last Respects", garchomp, toxapex, moveUse = MoveUse(move, priorPowerBoosts = 2)))
  }

  @Test
  fun tripleAxelEscalatesWithEachHitOfTheCombo() {
    val move = testMove("Triple Axel")
    assertEquals(move.basePower, basePower("Triple Axel", garchomp, toxapex, moveUse = MoveUse(move, hitNumber = 1)))
    assertEquals(move.basePower * 3, basePower("Triple Axel", garchomp, toxapex, moveUse = MoveUse(move, hitNumber = 3)))
  }
}
