package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.Nature
import com.tambapps.pokemon.champions.data.ChampionsCalcException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Full-pipeline behaviors not already covered by DamageCalculatorCrossValidationTest, each
 * still cross-checked against the real damage_MASTER.js/damage_SV.js via tools/oracle.js.
 */
class DamageCalculatorTest {

  private fun rolls(attacker: BattlePokemon, defender: BattlePokemon, move: MoveUse, field: Battlefield = Battlefield()) =
    DamageCalculator.calculateSingleHit(attacker, defender, move, field).rolls

  @Test
  fun struggleIsNeutralWithNoStabRegardlessOfTheUsersType() {
    val gengar = testPokemon("Gengar", ability = "Cursed Body", nature = Nature.ADAMANT, attack = 20, defense = 8, specialDefense = 8, speed = 10, hp = 20)
    val target = testPokemon("Gengar", ability = "Cursed Body", nature = Nature.BOLD, defense = 20, specialDefense = 16, speed = 6, hp = 20)
    assertEquals(listOf(21, 21, 21, 22, 22, 22, 22, 23, 23, 23, 23, 24, 24, 24, 24, 25), rolls(gengar, target, MoveUse(testMove("Struggle"))))
  }

  @Test
  fun parentalBondsSecondHitIsWeakerThanTheFirst() {
    val garchomp = testPokemon("Garchomp", ability = "Parental Bond", nature = Nature.ADAMANT, hp = 20, attack = 20, defense = 8, specialDefense = 8, speed = 10)
    val toxapex = testPokemon("Toxapex", ability = "Merciless", nature = Nature.BOLD, hp = 20, defense = 20, specialDefense = 16, speed = 6)
    val hits = DamageCalculator.calculateParentalBondHits(garchomp, toxapex, MoveUse(testMove("Dragon Claw")), Battlefield())
    assertEquals(listOf(42, 42, 42, 43, 43, 43, 45, 45, 45, 46, 46, 46, 48, 48, 48, 49), hits.firstHit.rolls)
    assertEquals(listOf(9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 12), hits.secondHit.rolls)
  }

  @Test
  fun calculateParentalBondHitsRequiresTheAbility() {
    val noBond = testPokemon("Garchomp", ability = "Rough Skin", hp = 20, attack = 20, defense = 8, specialDefense = 8, speed = 10)
    assertFailsWith<ChampionsCalcException> {
      DamageCalculator.calculateParentalBondHits(noBond, testPokemon("Toxapex"), MoveUse(testMove("Dragon Claw")), Battlefield())
    }
  }

  @Test
  fun piercingDrillPunchesThroughProtectAtAQuarterDamage() {
    val garchomp = testPokemon("Garchomp", ability = "Piercing Drill", nature = Nature.ADAMANT, hp = 20, attack = 20, defense = 8, specialDefense = 8, speed = 10)
    val toxapex = testPokemon("Toxapex", ability = "Merciless", nature = Nature.BOLD, hp = 20, defense = 20, specialDefense = 16, speed = 6)
    val field = Battlefield(defenderSide = SideConditions(isProtected = true))
    assertEquals(listOf(7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9), rolls(garchomp, toxapex, MoveUse(testMove("Body Slam")), field))
  }

  @Test
  fun protectAloneDoesNotReduceDamageForAnOrdinaryAttacker() {
    // This calculator models Protect only as the specific Z-move/Piercing-Drill/Unseen-Fist
    // exceptions -- an ordinary attacker's move isn't quartered (or blocked) by isProtected
    // alone, matching damage_MASTER.js's setIsQuarteredByProtect exactly.
    val garchomp = testPokemon("Garchomp", ability = "Rough Skin", nature = Nature.ADAMANT, hp = 20, attack = 20, defense = 8, specialDefense = 8, speed = 10)
    val toxapex = testPokemon("Toxapex", ability = "Merciless", nature = Nature.BOLD, hp = 20, defense = 20, specialDefense = 16, speed = 6)
    val field = Battlefield(defenderSide = SideConditions(isProtected = true))
    assertEquals(listOf(29, 30, 30, 30, 31, 31, 31, 32, 32, 32, 33, 33, 33, 34, 34, 35), rolls(garchomp, toxapex, MoveUse(testMove("Body Slam")), field))
  }

  @Test
  fun glaiveRushVulnerabilityDoublesIncomingDamage() {
    val garchomp = testPokemon("Garchomp", ability = "Rough Skin", nature = Nature.ADAMANT, hp = 20, attack = 20, defense = 8, specialDefense = 8, speed = 10)
    val vulnerable = testPokemon("Toxapex", ability = "Merciless", nature = Nature.BOLD, hp = 20, defense = 20, specialDefense = 16, speed = 6, isVulnerableFromGlaiveRush = true)
    assertEquals(
      listOf(152, 152, 156, 156, 158, 162, 162, 164, 164, 168, 170, 170, 174, 174, 176, 180),
      rolls(garchomp, vulnerable, MoveUse(testMove("Earthquake"))),
    )
  }
}
