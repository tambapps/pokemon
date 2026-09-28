package com.tambapps.pokemon.champions.data

import com.tambapps.pokemon.AbilityName
import com.tambapps.pokemon.ItemName
import com.tambapps.pokemon.MoveName
import com.tambapps.pokemon.PokeStats
import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.PokemonName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChampionsDexTest {

  @Test
  fun loadsTheFullChampionsDex() {
    assertTrue(ChampionsDex.allSpecies.size > 300)
    assertTrue(ChampionsDex.allMoves.size > 400)
  }

  @Test
  fun parsesADualTypeSpeciesWithBaseStatsAndWeight() {
    val charizard = ChampionsDex.species(PokemonName("Charizard"))
    assertEquals(PokeType.FIRE, charizard.primaryType)
    assertEquals(PokeType.FLYING, charizard.secondaryType)
    assertEquals(PokeStats(hp = 78, attack = 84, defense = 78, specialAttack = 109, specialDefense = 85, speed = 100), charizard.baseStats)
    assertEquals(90.5, charizard.weightKg)
  }

  @Test
  fun speciesLookupIsCaseAndSpacingInsensitive() {
    val bySpaces = ChampionsDex.species(PokemonName("charizard"))
    val byHyphenatedId = ChampionsDex.species(PokemonName("CHARIZARD"))
    assertEquals(bySpaces, byHyphenatedId)
  }

  @Test
  fun parsesAMonoTypeSpecies() {
    val pikachu = ChampionsDex.species(PokemonName("Pikachu"))
    assertEquals(PokeType.ELECTRIC, pikachu.primaryType)
    assertNull(pikachu.secondaryType)
  }

  @Test
  fun parsesMoveFlags() {
    val bodyPress = ChampionsDex.move(MoveName("Body Press"))
    assertEquals(80, bodyPress.basePower)
    assertEquals(PokeType.FIGHTING, bodyPress.type)
    assertEquals(MoveCategory.PHYSICAL, bodyPress.category)
    assertTrue(bodyPress.makesContact)
  }

  @Test
  fun moveLookupIsCaseAndSpacingInsensitive() {
    assertEquals(ChampionsDex.move(MoveName("earthquake")), ChampionsDex.move(MoveName("Earthquake")))
  }

  @Test
  fun parsesVariableHitCount() {
    val bulletSeed = ChampionsDex.move(MoveName("Bullet Seed"))
    assertEquals(HitCount.Variable(2, 5), bulletSeed.hitCount)
  }

  @Test
  fun parsesFixedHitCount() {
    val dualWingbeat = ChampionsDex.move(MoveName("Dual Wingbeat"))
    assertEquals(HitCount.Fixed(2), dualWingbeat.hitCount)
  }

  @Test
  fun typeChartMatchesKnownMatchups() {
    val chart = ChampionsDex.typeChart
    assertEquals(0.0, chart.effectivenessOf(PokeType.NORMAL, PokeType.GHOST))
    assertEquals(2.0, chart.effectivenessOf(PokeType.WATER, PokeType.FIRE))
    assertEquals(0.5, chart.effectivenessOf(PokeType.FIRE, PokeType.WATER))
  }

  @Test
  fun resolvesAbilityFromNormalizedAbilityName() {
    assertEquals(Ability.ROUGH_SKIN, Ability.from(AbilityName("rough skin")))
    assertEquals(Ability.ROUGH_SKIN, Ability.from(AbilityName("Rough Skin")))
    // Every Pokemon has some ability, so an unrecognized name resolves to a sentinel, not null.
    assertEquals(Ability.NO_ABILITY, Ability.from(AbilityName("not-a-real-ability")))
  }

  @Test
  fun parsesTheDefaultAbilityOfEverySpecies() {
    assertEquals(AbilityName("Intimidate"), ChampionsDex.species(PokemonName("Incineroar")).defaultAbility)
    assertEquals(AbilityName("Stance Change"), ChampionsDex.species(PokemonName("Aegislash-Shield")).defaultAbility)
    // every default ability is one the engine recognizes
    assertTrue(ChampionsDex.allSpecies.all { Ability.from(it.defaultAbility) != Ability.NO_ABILITY })
  }

  @Test
  fun nullableLookupsReturnNullForUnknownNames() {
    assertNull(ChampionsDex.speciesOrNull(PokemonName("not-a-real-pokemon")))
    assertNull(ChampionsDex.moveOrNull(MoveName("not-a-real-move")))
    assertEquals(ChampionsDex.species(PokemonName("Incineroar")), ChampionsDex.speciesOrNull(PokemonName("incineroar")))
    assertEquals(ChampionsDex.move(MoveName("Moonblast")), ChampionsDex.moveOrNull(MoveName("moonblast")))
  }

  @Test
  fun lookupsThrowCalcExceptionForUnknownNames() {
    assertFailsWith<ChampionsCalcException> { ChampionsDex.species(PokemonName("not-a-real-pokemon")) }
    assertFailsWith<ChampionsCalcException> { ChampionsDex.move(MoveName("not-a-real-move")) }
  }

  @Test
  fun resolvesItemFromNormalizedItemName() {
    assertEquals(Item.LIFE_ORB, Item.from(ItemName("life orb")))
    assertEquals(Item.LIFE_ORB, Item.from(ItemName("Life Orb")))
    assertNull(Item.from(ItemName("not-a-real-item")))
  }
}
