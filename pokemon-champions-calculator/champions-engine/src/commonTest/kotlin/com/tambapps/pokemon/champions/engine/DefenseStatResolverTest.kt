package com.tambapps.pokemon.champions.engine

import kotlin.test.Test
import kotlin.test.assertEquals

class DefenseStatResolverTest {

  // Toxapex, base Defense 152 / Sp. Def 142, 0 Stat Points, neutral nature -> raw Defense 172, raw Sp. Def 162.
  private val plainToxapex = testPokemon("Toxapex")
  private val garchomp = testPokemon("Garchomp")

  private fun defense(move: String, defender: BattlePokemon, attacker: BattlePokemon = garchomp, hitsPhysical: Boolean = true, isCritical: Boolean = false, field: Battlefield = Battlefield()) =
    DefenseStatResolver.resolve(testMove(move), attacker, defender, hitsPhysical, isCritical, field)

  @Test
  fun unboostedDefenseUsesTheRawStat() {
    assertEquals(172, defense("Earthquake", plainToxapex))
  }

  @Test
  fun specialHitsUseSpecialDefenseInstead() {
    assertEquals(162, defense("Flamethrower", plainToxapex, hitsPhysical = false))
  }

  @Test
  fun positiveAndNegativeBoostsApplyNormally() {
    assertEquals(258, defense("Earthquake", testPokemon("Toxapex", boosts = StatBoosts(defense = 1))))
    assertEquals(114, defense("Earthquake", testPokemon("Toxapex", boosts = StatBoosts(defense = -1))))
  }

  @Test
  fun criticalHitsIgnoreTheDefendersOwnPositiveBoost() {
    val boosted = testPokemon("Toxapex", boosts = StatBoosts(defense = 1))
    assertEquals(172, defense("Earthquake", boosted, isCritical = true))
  }

  @Test
  fun criticalHitsDoNotIgnoreTheDefendersOwnNegativeBoost() {
    val lowered = testPokemon("Toxapex", boosts = StatBoosts(defense = -1))
    assertEquals(114, defense("Earthquake", lowered, isCritical = true))
  }

  @Test
  fun unawareOnTheAttackerIgnoresTheDefendersBoostEitherWay() {
    val unawareAttacker = testPokemon("Garchomp", ability = "Unaware")
    val boosted = testPokemon("Toxapex", boosts = StatBoosts(defense = 1))
    assertEquals(172, defense("Earthquake", boosted, unawareAttacker))
  }

  @Test
  fun sacredSwordIgnoresTheDefendersBoostRegardless() {
    val boosted = testPokemon("Toxapex", boosts = StatBoosts(defense = 1))
    assertEquals(172, defense("Sacred Sword", boosted))
  }

  @Test
  fun sandstormBoostsRockTypesSpecialDefense() {
    val rampardos = testPokemon("Rampardos") // mono-Rock
    val base = defense("Flamethrower", rampardos, hitsPhysical = false)
    val inSand = defense("Flamethrower", rampardos, hitsPhysical = false, field = Battlefield(weather = Weather.SAND))
    assertEquals(base * 3 / 2, inSand)
  }

  @Test
  fun snowBoostsIceTypesDefense() {
    val glaceon = testPokemon("Glaceon") // mono-Ice
    val base = defense("Earthquake", glaceon)
    val inSnow = defense("Earthquake", glaceon, field = Battlefield(weather = Weather.SNOW))
    assertEquals(base * 3 / 2, inSnow)
  }

  @Test
  fun megaSolCancelsTheSandstormAndSnowBoosts() {
    val rampardos = testPokemon("Rampardos")
    val megaSolAttacker = testPokemon("Garchomp", ability = "Mega Sol")
    val base = defense("Flamethrower", rampardos, megaSolAttacker, hitsPhysical = false)
    val inSand = defense("Flamethrower", rampardos, megaSolAttacker, hitsPhysical = false, field = Battlefield(weather = Weather.SAND))
    assertEquals(base, inSand)
  }
}
