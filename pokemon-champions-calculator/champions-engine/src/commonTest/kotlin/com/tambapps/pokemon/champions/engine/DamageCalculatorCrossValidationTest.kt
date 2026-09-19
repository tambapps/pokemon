package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.AbilityName
import com.tambapps.pokemon.Gender
import com.tambapps.pokemon.ItemName
import com.tambapps.pokemon.MoveName
import com.tambapps.pokemon.Nature
import com.tambapps.pokemon.PokeStats
import com.tambapps.pokemon.PokemonName
import com.tambapps.pokemon.champions.data.ChampionsDex
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Every expected roll list here was produced by running the REAL NCP-VGC-Damage-Calculator
 * engine (damage_MASTER.js + damage_SV.js, unmodified) directly, bypassing its jQuery/DOM UI,
 * against the same inputs -- see tools/oracle.js. This is what gives this port confidence:
 * it isn't just internally consistent, it matches the source calculator hit-for-hit.
 */
class DamageCalculatorCrossValidationTest {

  private fun pokemon(
    species: String,
    ability: String,
    nature: Nature,
    hp: Int, attack: Int, defense: Int, specialAttack: Int, specialDefense: Int, speed: Int,
    item: String? = null,
    status: Status = Status.HEALTHY,
    gender: Gender = Gender.ASEXUAL,
  ) = BattlePokemon(
    species = ChampionsDex.species(PokemonName(species)),
    ability = AbilityName(ability),
    nature = nature,
    statPoints = PokeStats(hp = hp, attack = attack, defense = defense, specialAttack = specialAttack, specialDefense = specialDefense, speed = speed),
    item = item?.let(::ItemName),
    status = status,
    gender = gender,
  )

  private fun moveUse(name: String, isCritical: Boolean = false) =
    MoveUse(ChampionsDex.move(MoveName(name)), isCritical = isCritical)

  private fun rolls(attacker: BattlePokemon, defender: BattlePokemon, move: MoveUse, field: Battlefield = Battlefield()) =
    DamageCalculator.calculateSingleHit(attacker, defender, move, field).rolls

  @Test
  fun basicNeutralMatchup() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(listOf(74, 74, 78, 78, 78, 80, 80, 80, 80, 84, 84, 84, 86, 86, 86, 90), rolls(garchomp, toxapex, moveUse("Earthquake")))
  }

  @Test
  fun stabAndSuperEffectiveWeatherBallInSun() {
    val charizard = pokemon("Charizard", "Solar Power", Nature.MODEST, 20, 0, 6, 20, 8, 12)
    val venusaur = pokemon("Venusaur", "Overgrow", Nature.BOLD, 20, 0, 20, 6, 10, 10)
    val field = Battlefield(weather = Weather.SUN)
    assertEquals(
      listOf(320, 324, 326, 330, 336, 338, 342, 344, 350, 354, 356, 360, 366, 368, 372, 378),
      rolls(charizard, venusaur, moveUse("Weather Ball"), field),
    )
  }

  @Test
  fun criticalHitIgnoresDefenderDefenseBoost() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.JOLLY, 20, 20, 6, 0, 6, 14)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6).copy(boosts = StatBoosts(defense = 2))
    assertEquals(
      listOf(104, 108, 108, 108, 110, 110, 114, 114, 116, 116, 116, 120, 120, 122, 122, 126),
      rolls(garchomp, toxapex, moveUse("Earthquake", isCritical = true)),
    )
  }

  @Test
  fun typeBoostingItem() {
    val charizard = pokemon("Charizard", "Blaze", Nature.TIMID, 20, 0, 6, 20, 8, 12, item = "Charcoal")
    val venusaur = pokemon("Venusaur", "Overgrow", Nature.BOLD, 20, 0, 20, 6, 10, 10)
    assertEquals(
      listOf(170, 174, 176, 176, 180, 182, 182, 186, 188, 188, 192, 194, 194, 198, 200, 204),
      rolls(charizard, venusaur, moveUse("Fire Blast")),
    )
  }

  @Test
  fun technicianBoostsLowPowerMove() {
    val scizor = pokemon("Scizor", "Technician", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(listOf(15, 15, 15, 16, 16, 16, 16, 17, 17, 17, 17, 18, 18, 18, 18, 18), rolls(scizor, toxapex, moveUse("Bullet Punch")))
  }

  @Test
  fun adaptabilityDoublesStabInsteadOfOneAndHalf() {
    val garchomp = pokemon("Garchomp", "Adaptability", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(listOf(100, 100, 104, 104, 104, 108, 108, 108, 108, 112, 112, 112, 116, 116, 116, 120), rolls(garchomp, toxapex, moveUse("Earthquake")))
  }

  @Test
  fun reflectHalvesPhysicalDamageInDoubles() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    val field = Battlefield(defenderSide = SideConditions(hasReflect = true))
    assertEquals(listOf(49, 49, 52, 52, 52, 53, 53, 53, 53, 56, 56, 56, 57, 57, 57, 60), rolls(garchomp, toxapex, moveUse("Earthquake"), field))
  }

  @Test
  fun lifeOrbBoostsDamage() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10, item = "Life Orb")
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(listOf(96, 96, 101, 101, 101, 104, 104, 104, 104, 109, 109, 109, 112, 112, 112, 117), rolls(garchomp, toxapex, moveUse("Earthquake")))
  }

  @Test
  fun burnHalvesPhysicalDamage() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10, status = Status.BURNED)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(listOf(37, 37, 39, 39, 39, 40, 40, 40, 40, 42, 42, 42, 43, 43, 43, 45), rolls(garchomp, toxapex, moveUse("Earthquake")))
  }

  @Test
  fun bodyPressUsesUsersOwnDefenseAsAttackStat() {
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    assertEquals(listOf(52, 53, 53, 54, 55, 55, 56, 57, 57, 58, 58, 59, 60, 60, 61, 62), rolls(toxapex, garchomp, moveUse("Body Press")))
  }

  @Test
  fun foulPlayUsesTargetsAttackStat() {
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    assertEquals(listOf(55, 55, 56, 57, 57, 58, 59, 59, 60, 61, 61, 62, 63, 63, 64, 65), rolls(toxapex, garchomp, moveUse("Foul Play")))
  }

  @Test
  fun gyroBallScalesWithSpeedRatio() {
    val toxapex = pokemon("Toxapex", "Merciless", Nature.RELAXED, 20, 0, 20, 0, 16, 0)
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.JOLLY, 20, 20, 6, 0, 6, 14)
    assertEquals(listOf(21, 21, 21, 22, 22, 22, 22, 23, 23, 23, 23, 24, 24, 24, 24, 25), rolls(toxapex, garchomp, moveUse("Gyro Ball")))
  }

  @Test
  fun expertBeltBoostsSuperEffectiveHits() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10, item = "Expert Belt")
    val charizard = pokemon("Charizard", "Blaze", Nature.TIMID, 20, 0, 6, 20, 8, 12)
    assertEquals(
      listOf(187, 187, 192, 192, 192, 197, 197, 202, 202, 206, 206, 211, 211, 216, 216, 221),
      rolls(garchomp, charizard, moveUse("Rock Slide")),
    )
  }

  @Test
  fun friendGuardReducesDamageTakenByAlly() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    val field = Battlefield(defenderSide = SideConditions(hasFriendGuard = true))
    assertEquals(listOf(55, 55, 58, 58, 58, 60, 60, 60, 60, 63, 63, 63, 64, 64, 64, 67), rolls(garchomp, toxapex, moveUse("Earthquake"), field))
  }

  @Test
  fun rivalrySameGenderBoostsDamage() {
    val garchomp = pokemon("Garchomp", "Rivalry", Nature.ADAMANT, 20, 20, 8, 0, 8, 10, gender = Gender.MALE)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6, gender = Gender.MALE)
    assertEquals(listOf(92, 92, 96, 96, 96, 98, 98, 102, 102, 102, 104, 104, 104, 108, 108, 110), rolls(garchomp, toxapex, moveUse("Earthquake")))
  }

  @Test
  fun resistBerryHalvesSuperEffectiveDamage() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6, item = "Shuca Berry")
    assertEquals(listOf(37, 37, 39, 39, 39, 40, 40, 40, 40, 42, 42, 42, 43, 43, 43, 45), rolls(garchomp, toxapex, moveUse("Earthquake")))
  }

  @Test
  fun multiscaleHalvesDamageAtFullHp() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val dragonite = pokemon("Dragonite", "Multiscale", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    assertEquals(listOf(69, 70, 70, 72, 72, 73, 75, 75, 76, 76, 78, 78, 79, 79, 81, 82), rolls(garchomp, dragonite, moveUse("Dragon Claw")))
  }

  @Test
  fun spreadMovePenaltyOnlyAppliesInDoubles() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    val field = Battlefield(format = BattleFormat.SINGLES)
    assertEquals(
      listOf(102, 102, 102, 104, 104, 108, 108, 108, 110, 110, 114, 114, 114, 116, 116, 120),
      rolls(garchomp, toxapex, moveUse("Earthquake"), field),
    )
  }

  @Test
  fun acrobaticsIsStrongerWithNoHeldItem() {
    val talonflame = pokemon("Talonflame", "Gale Wings", Nature.JOLLY, 20, 20, 6, 0, 6, 14)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(listOf(36, 36, 37, 37, 37, 39, 39, 39, 39, 40, 40, 40, 42, 42, 42, 43), rolls(talonflame, toxapex, moveUse("Acrobatics")))
  }

  @Test
  fun facadeIsStrongerWhileStatused() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10, status = Status.PARALYZED)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(listOf(47, 48, 48, 49, 49, 50, 50, 51, 52, 52, 53, 53, 54, 54, 55, 56), rolls(garchomp, toxapex, moveUse("Facade")))
  }

  @Test
  fun bulletSeedSingleHitPower() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(listOf(9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 11), rolls(garchomp, toxapex, moveUse("Bullet Seed")))
  }

  @Test
  fun sturdyBlocksOhkoMoves() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val skarmory = pokemon("Skarmory", "Sturdy", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(listOf(0), rolls(garchomp, skarmory, moveUse("Sheer Cold")))
  }

  @Test
  fun flashFireGrantsFireImmunity() {
    val charizard = pokemon("Charizard", "Blaze", Nature.TIMID, 20, 0, 6, 20, 8, 12)
    val toxapex = pokemon("Toxapex", "Flash Fire", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(listOf(0), rolls(charizard, toxapex, moveUse("Fire Blast")))
  }

  @Test
  fun abilityNameLookupIsCaseInsensitive() {
    // Exercises the AbilityName -> Ability resolution boundary (BattlePokemon.resolvedAbility)
    // rather than assuming callers already hand over Champions' exact display casing.
    val lowercased = pokemon("Garchomp", "adaptability", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val exact = pokemon("Garchomp", "Adaptability", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(rolls(exact, toxapex, moveUse("Earthquake")), rolls(lowercased, toxapex, moveUse("Earthquake")))
  }

  @Test
  fun unrecognizedAbilityIsTreatedAsNoAbility() {
    val typo = pokemon("Garchomp", "Rough Skinn", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val none = pokemon("Garchomp", "", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(rolls(none, toxapex, moveUse("Earthquake")), rolls(typo, toxapex, moveUse("Earthquake")))
  }
}
