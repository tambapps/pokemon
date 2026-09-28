package com.tambapps.pokemon.champions.engine.description

import com.tambapps.pokemon.Gender
import com.tambapps.pokemon.Nature
import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.Stat
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Item
import com.tambapps.pokemon.champions.engine.Battlefield
import com.tambapps.pokemon.champions.engine.DamageCalculator
import com.tambapps.pokemon.champions.engine.MoveUse
import com.tambapps.pokemon.champions.engine.SideConditions
import com.tambapps.pokemon.champions.engine.StatBoosts
import com.tambapps.pokemon.champions.engine.Weather
import com.tambapps.pokemon.champions.engine.testMove
import com.tambapps.pokemon.champions.engine.testPokemon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The typed facts behind the descriptions, whose exact text DamageCalculatorCrossValidationTest checks against the source calculator. */
class CalcFactsTest {

  private val garchomp = testPokemon("Garchomp", ability = "Rough Skin", nature = Nature.ADAMANT, hp = 20, attack = 20, defense = 8, specialDefense = 8, speed = 10)
  private val toxapex = testPokemon("Toxapex", ability = "Merciless", nature = Nature.BOLD, hp = 20, defense = 20, specialDefense = 16, speed = 6)

  private fun facts(
    attacker: com.tambapps.pokemon.champions.engine.BattlePokemon,
    defender: com.tambapps.pokemon.champions.engine.BattlePokemon,
    move: String,
    field: Battlefield = Battlefield(),
  ) = DamageCalculator.calculateSingleHit(attacker, defender, MoveUse(testMove(move)), field).facts

  @Test
  fun descriptionIsTheFormattedFacts() {
    val result = DamageCalculator.calculateMove(garchomp, toxapex, MoveUse(testMove("Bullet Seed")), Battlefield(), hits = 3, statDisplay = StatDisplay.EVS)
    assertEquals(result.description, result.facts.format(StatDisplay.EVS))
    assertEquals(result.hits.first().description, result.hits.first().facts.format(StatDisplay.EVS))
  }

  @Test
  fun onlyMentionsWhatApplied() {
    val facts = facts(garchomp.copy(item = com.tambapps.pokemon.ItemName("Life Orb")), toxapex, "Earthquake")
    assertEquals(Item.LIFE_ORB, facts.attackerItem)
    // Rough Skin and Merciless don't affect Earthquake
    assertNull(facts.attackerAbility)
    assertNull(facts.defenderAbility)
    assertEquals(StatInvestment(Stat.ATTACK, statPoints = 20, value = 187, nature = Nature.ADAMANT), facts.attackStat)
    assertEquals(Stat.DEFENSE, facts.defenseStat?.stat)
    assertEquals(20, facts.hp?.statPoints)
  }

  @Test
  fun fieldFacts() {
    val field = Battlefield(weather = Weather.RAIN, defenderSide = SideConditions(hasReflect = true, hasFriendGuard = true))
    val facts = facts(garchomp, toxapex, "Earthquake", field)
    // the rain doesn't affect Earthquake
    assertNull(facts.weather)
    assertEquals(Screen.REFLECT, facts.screen)
    assertEquals(true, facts.isFriendGuard)
  }

  @Test
  fun changedPowerAndType() {
    val knockOff = facts(testPokemon("Kingambit", attack = 20), toxapex.copy(item = com.tambapps.pokemon.ItemName("Leftovers")), "Knock Off")
    assertEquals(97.5, knockOff.moveBasePower)
    assertNull(knockOff.moveType)

    val weatherBall = facts(testPokemon("Charizard", specialAttack = 20), toxapex, "Weather Ball", Battlefield(weather = Weather.SUN))
    assertEquals(100.0, weatherBall.moveBasePower)
    assertEquals(PokeType.FIRE, weatherBall.moveType)
    assertEquals(Weather.SUN, weatherBall.weather)
  }

  @Test
  fun abilitiesWrittenWithTheirEffect() {
    val rivalry = facts(garchomp.copy(ability = com.tambapps.pokemon.AbilityName("Rivalry"), gender = Gender.MALE), toxapex.copy(gender = Gender.MALE), "Earthquake")
    assertEquals(Ability.RIVALRY, rivalry.attackerAbility)
    assertEquals(RivalryEffect.SAME_GENDER, rivalry.rivalryEffect)

    val overlordResult = DamageCalculator.calculateMove(
      testPokemon("Kingambit", ability = "Supreme Overlord", attack = 20), toxapex,
      MoveUse(testMove("Kowtow Cleave"), faintedAllyCount = 1), Battlefield(),
    )
    val overlord = overlordResult.facts
    assertEquals(1, overlord.faintedAllies)
    // oracle.js output of the supreme-overlord-one-ally scenario
    assertEquals("20 Atk Supreme Overlord (1 ally down) Kingambit Kowtow Cleave vs. 20 HP  / 20+ Def Toxapex", overlord.format())
    assertEquals("71.41% chance to 3HKO", overlordResult.koChance.text)
  }

  @Test
  fun boostsAndWholeMoveFacts() {
    val boosted = garchomp.copy(boosts = StatBoosts(attack = 1))
    val result = DamageCalculator.calculateMove(boosted, toxapex, MoveUse(testMove("Bullet Seed")), Battlefield(), hits = 4)
    assertEquals(1, result.facts.attackBoost)
    assertEquals(4, result.facts.hits)
    // a hit's facts don't have the whole move's hit count
    assertNull(result.hits.first().facts.hits)
  }

  @Test
  fun immunityFacts() {
    val facts = facts(garchomp, toxapex.copy(item = com.tambapps.pokemon.ItemName("Air Balloon")), "Earthquake")
    assertEquals(Item.AIR_BALLOON, facts.defenderItem)
    // no damage was calculated
    assertNull(facts.attackStat)
    assertNull(facts.hp)
  }
}
