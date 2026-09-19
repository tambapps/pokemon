package com.tambapps.pokemon.champions.engine

import kotlin.test.Test
import kotlin.test.assertEquals

class AttackStatResolverTest {

  // Garchomp, base Attack 130, 0 Stat Points, neutral nature -> raw Attack of 150.
  private val plainGarchomp = testPokemon("Garchomp")
  private val toxapex = testPokemon("Toxapex")

  private fun attack(move: String, attacker: BattlePokemon, defender: BattlePokemon = toxapex, isCritical: Boolean = false) =
    AttackStatResolver.resolve(testMove(move), attacker, defender, isCritical)

  @Test
  fun unboostedAttackUsesTheRawStat() {
    assertEquals(150, attack("Earthquake", plainGarchomp))
  }

  @Test
  fun positiveBoostIncreasesTheAttackStatNormally() {
    val boosted = testPokemon("Garchomp", boosts = StatBoosts(attack = 1))
    assertEquals(225, attack("Earthquake", boosted))
  }

  @Test
  fun negativeBoostDecreasesTheAttackStatNormally() {
    val lowered = testPokemon("Garchomp", boosts = StatBoosts(attack = -1))
    assertEquals(100, attack("Earthquake", lowered))
  }

  @Test
  fun criticalHitsIgnoreTheAttackersOwnNegativeBoost() {
    val lowered = testPokemon("Garchomp", boosts = StatBoosts(attack = -1))
    assertEquals(150, attack("Earthquake", lowered, isCritical = true))
  }

  @Test
  fun criticalHitsDoNotIgnoreTheAttackersOwnPositiveBoost() {
    val boosted = testPokemon("Garchomp", boosts = StatBoosts(attack = 1))
    assertEquals(225, attack("Earthquake", boosted, isCritical = true))
  }

  @Test
  fun unawareOnTheDefenderIgnoresTheAttackersBoostEitherWay() {
    val unaware = testPokemon("Toxapex", ability = "Unaware")
    val boosted = testPokemon("Garchomp", boosts = StatBoosts(attack = 1))
    assertEquals(150, attack("Earthquake", boosted, unaware))
  }

  @Test
  fun foulPlayUsesTheDefendersAttackStatInstead() {
    val weakAttacker = testPokemon("Toxapex", attack = 0) // low Attack
    val strongDefender = testPokemon("Garchomp") // its own Attack is used instead
    assertEquals(attack("Earthquake", strongDefender), attack("Foul Play", weakAttacker, strongDefender))
  }

  @Test
  fun bodyPressUsesTheUsersOwnDefenseStatAsItsAttackStat() {
    // Toxapex's Defense (base 152) stands in for Attack when it uses Body Press.
    val user = testPokemon("Toxapex")
    val defenseAsAttack = attack("Body Press", user)
    val realAttackStat = attack("Earthquake", user) // Toxapex's actual (much lower) Attack, for contrast
    assertEquals(true, defenseAsAttack > realAttackStat)
  }

  @Test
  fun hustleBoostsPhysicalAttackByHalf() {
    val hustle = testPokemon("Garchomp", ability = "Hustle")
    assertEquals(225, attack("Earthquake", hustle)) // 150 * 1.5
  }

  @Test
  fun hustleDoesNotApplyToSpecialMoves() {
    val hustle = testPokemon("Charizard", ability = "Hustle")
    assertEquals(attack("Flamethrower", testPokemon("Charizard")), attack("Flamethrower", hustle))
  }
}
