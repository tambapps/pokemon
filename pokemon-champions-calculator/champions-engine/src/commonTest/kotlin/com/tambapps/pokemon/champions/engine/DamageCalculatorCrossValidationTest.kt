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
 * Every expected roll list and description here was produced by running the REAL NCP-VGC-Damage-Calculator
 * engine (damage_MASTER.js + damage_SV.js, unmodified) directly, bypassing its jQuery/DOM UI,
 * against the same inputs -- see tools/oracle.js and the scenario of the same name in tools/scenarios.json.
 * This is what gives this port confidence: it isn't just internally consistent, it matches the source
 * calculator hit-for-hit, down to the description text.
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
    boosts: StatBoosts = StatBoosts.NONE,
    abilityIsActive: Boolean = false,
  ) = BattlePokemon(
    species = ChampionsDex.species(PokemonName(species)),
    ability = AbilityName(ability),
    nature = nature,
    statPoints = PokeStats(hp = hp, attack = attack, defense = defense, specialAttack = specialAttack, specialDefense = specialDefense, speed = speed),
    item = item?.let(::ItemName),
    status = status,
    gender = gender,
    boosts = boosts,
    abilityIsActive = abilityIsActive,
  )

  // the spreads most scenarios share
  private fun garchomp(ability: String = "Rough Skin", item: String? = null, status: Status = Status.HEALTHY, boosts: StatBoosts = StatBoosts.NONE) =
    pokemon("Garchomp", ability, Nature.ADAMANT, 20, 20, 8, 0, 8, 10, item = item, status = status, boosts = boosts)
  private fun toxapex(ability: String = "Merciless", item: String? = null, status: Status = Status.HEALTHY, boosts: StatBoosts = StatBoosts.NONE) =
    pokemon("Toxapex", ability, Nature.BOLD, 20, 0, 20, 0, 16, 6, item = item, status = status, boosts = boosts)
  private fun specialAttacker(species: String, ability: String, nature: Nature = Nature.MODEST, boosts: StatBoosts = StatBoosts.NONE) =
    pokemon(species, ability, nature, 20, 0, 6, 20, 8, 12, boosts = boosts)
  private fun venusaur() = pokemon("Venusaur", "Overgrow", Nature.BOLD, 20, 0, 20, 6, 10, 10)

  private fun moveUse(name: String, isCritical: Boolean = false) =
    MoveUse(ChampionsDex.move(MoveName(name)), isCritical = isCritical)

  private fun calc(attacker: BattlePokemon, defender: BattlePokemon, move: MoveUse, field: Battlefield = Battlefield(), statDisplay: StatDisplay = StatDisplay.STAT_POINTS) =
    DamageCalculator.calculateSingleHit(attacker, defender, move, field, statDisplay)

  private fun rolls(attacker: BattlePokemon, defender: BattlePokemon, move: MoveUse, field: Battlefield = Battlefield()) =
    calc(attacker, defender, move, field).rolls

  private fun description(attacker: BattlePokemon, defender: BattlePokemon, move: MoveUse, field: Battlefield = Battlefield()) =
    calc(attacker, defender, move, field).description

  private fun assertCalc(
    expectedRolls: List<Int>,
    expectedDescription: String,
    attacker: BattlePokemon,
    defender: BattlePokemon,
    move: MoveUse,
    field: Battlefield = Battlefield(),
    statDisplay: StatDisplay = StatDisplay.STAT_POINTS,
  ) {
    val result = calc(attacker, defender, move, field, statDisplay)
    assertEquals(expectedRolls, result.rolls)
    assertEquals(expectedDescription, result.description)
    // a single-hit move's whole-move description is its hit's
    assertEquals(expectedDescription, DamageCalculator.calculateMove(attacker, defender, move, field, statDisplay = statDisplay).description)
  }

  @Test
  fun basicNeutralMatchup() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(listOf(74, 74, 78, 78, 78, 80, 80, 80, 80, 84, 84, 84, 86, 86, 86, 90), rolls(garchomp, toxapex, moveUse("Earthquake")))
    assertEquals("20+ Atk Garchomp Earthquake vs. 20 HP  / 20+ Def Toxapex", description(garchomp, toxapex, moveUse("Earthquake")))
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
    assertEquals(
      "20+ SpA Solar Power Charizard Weather Ball (100 BP Fire) vs. 20 HP  / 10 SpD Venusaur in Sun",
      description(charizard, venusaur, moveUse("Weather Ball"), field),
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
    assertEquals(
      "20 Atk Garchomp Earthquake vs. 20 HP  / 20+ Def Toxapex on a critical hit",
      description(garchomp, toxapex, moveUse("Earthquake", isCritical = true)),
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
    assertEquals("20 SpA Charcoal Charizard Fire Blast vs. 20 HP  / 10 SpD Venusaur", description(charizard, venusaur, moveUse("Fire Blast")))
  }

  @Test
  fun technicianBoostsLowPowerMove() {
    val scizor = pokemon("Scizor", "Technician", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(listOf(15, 15, 15, 16, 16, 16, 16, 17, 17, 17, 17, 18, 18, 18, 18, 18), rolls(scizor, toxapex, moveUse("Bullet Punch")))
    assertEquals("20+ Atk Technician Scizor Bullet Punch vs. 20 HP  / 20+ Def Toxapex", description(scizor, toxapex, moveUse("Bullet Punch")))
  }

  @Test
  fun adaptabilityDoublesStabInsteadOfOneAndHalf() {
    val garchomp = pokemon("Garchomp", "Adaptability", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(listOf(100, 100, 104, 104, 104, 108, 108, 108, 108, 112, 112, 112, 116, 116, 116, 120), rolls(garchomp, toxapex, moveUse("Earthquake")))
    assertEquals("20+ Atk Adaptability Garchomp Earthquake vs. 20 HP  / 20+ Def Toxapex", description(garchomp, toxapex, moveUse("Earthquake")))
  }

  @Test
  fun reflectHalvesPhysicalDamageInDoubles() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    val field = Battlefield(defenderSide = SideConditions(hasReflect = true))
    assertEquals(listOf(49, 49, 52, 52, 52, 53, 53, 53, 53, 56, 56, 56, 57, 57, 57, 60), rolls(garchomp, toxapex, moveUse("Earthquake"), field))
    assertEquals("20+ Atk Garchomp Earthquake vs. 20 HP  / 20+ Def Toxapex through Reflect", description(garchomp, toxapex, moveUse("Earthquake"), field))
  }

  @Test
  fun lifeOrbBoostsDamage() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10, item = "Life Orb")
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(listOf(96, 96, 101, 101, 101, 104, 104, 104, 104, 109, 109, 109, 112, 112, 112, 117), rolls(garchomp, toxapex, moveUse("Earthquake")))
    assertEquals("20+ Atk Life Orb Garchomp Earthquake vs. 20 HP  / 20+ Def Toxapex", description(garchomp, toxapex, moveUse("Earthquake")))
  }

  @Test
  fun burnHalvesPhysicalDamage() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10, status = Status.BURNED)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(listOf(37, 37, 39, 39, 39, 40, 40, 40, 40, 42, 42, 42, 43, 43, 43, 45), rolls(garchomp, toxapex, moveUse("Earthquake")))
    assertEquals("20+ Atk burned Garchomp Earthquake vs. 20 HP  / 20+ Def Toxapex", description(garchomp, toxapex, moveUse("Earthquake")))
  }

  @Test
  fun bodyPressUsesUsersOwnDefenseAsAttackStat() {
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    assertEquals(listOf(52, 53, 53, 54, 55, 55, 56, 57, 57, 58, 58, 59, 60, 60, 61, 62), rolls(toxapex, garchomp, moveUse("Body Press")))
    assertEquals("20+ Def Toxapex Body Press vs. 20 HP  / 8 Def Garchomp", description(toxapex, garchomp, moveUse("Body Press")))
  }

  @Test
  fun foulPlayUsesTargetsAttackStat() {
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    assertEquals(listOf(55, 55, 56, 57, 57, 58, 59, 59, 60, 61, 61, 62, 63, 63, 64, 65), rolls(toxapex, garchomp, moveUse("Foul Play")))
    assertEquals("Toxapex Foul Play vs. 20 HP  / 20+ Atk / 8 Def Garchomp", description(toxapex, garchomp, moveUse("Foul Play")))
  }

  @Test
  fun gyroBallScalesWithSpeedRatio() {
    val toxapex = pokemon("Toxapex", "Merciless", Nature.RELAXED, 20, 0, 20, 0, 16, 0)
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.JOLLY, 20, 20, 6, 0, 6, 14)
    assertEquals(listOf(21, 21, 21, 22, 22, 22, 22, 23, 23, 23, 23, 24, 24, 24, 24, 25), rolls(toxapex, garchomp, moveUse("Gyro Ball")))
    assertEquals("0 Atk Toxapex Gyro Ball (77 BP) vs. 20 HP  / 6 Def Garchomp", description(toxapex, garchomp, moveUse("Gyro Ball")))
  }

  @Test
  fun expertBeltBoostsSuperEffectiveHits() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10, item = "Expert Belt")
    val charizard = pokemon("Charizard", "Blaze", Nature.TIMID, 20, 0, 6, 20, 8, 12)
    assertEquals(
      listOf(187, 187, 192, 192, 192, 197, 197, 202, 202, 206, 206, 211, 211, 216, 216, 221),
      rolls(garchomp, charizard, moveUse("Rock Slide")),
    )
    assertEquals("20+ Atk Expert Belt Garchomp Rock Slide vs. 20 HP  / 6 Def Charizard", description(garchomp, charizard, moveUse("Rock Slide")))
  }

  @Test
  fun friendGuardReducesDamageTakenByAlly() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    val field = Battlefield(defenderSide = SideConditions(hasFriendGuard = true))
    assertEquals(listOf(55, 55, 58, 58, 58, 60, 60, 60, 60, 63, 63, 63, 64, 64, 64, 67), rolls(garchomp, toxapex, moveUse("Earthquake"), field))
    assertEquals("20+ Atk Garchomp Earthquake vs. 20 HP  / 20+ Def Toxapex with Friend Guard", description(garchomp, toxapex, moveUse("Earthquake"), field))
  }

  @Test
  fun rivalrySameGenderBoostsDamage() {
    val garchomp = pokemon("Garchomp", "Rivalry", Nature.ADAMANT, 20, 20, 8, 0, 8, 10, gender = Gender.MALE)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6, gender = Gender.MALE)
    assertEquals(listOf(92, 92, 96, 96, 96, 98, 98, 102, 102, 102, 104, 104, 104, 108, 108, 110), rolls(garchomp, toxapex, moveUse("Earthquake")))
    assertEquals("20+ Atk Rivalry (1.25x) Garchomp Earthquake vs. 20 HP  / 20+ Def Toxapex", description(garchomp, toxapex, moveUse("Earthquake")))
  }

  @Test
  fun resistBerryHalvesSuperEffectiveDamage() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6, item = "Shuca Berry")
    assertEquals(listOf(37, 37, 39, 39, 39, 40, 40, 40, 40, 42, 42, 42, 43, 43, 43, 45), rolls(garchomp, toxapex, moveUse("Earthquake")))
    assertEquals("20+ Atk Garchomp Earthquake vs. 20 HP  / 20+ Def Shuca Berry Toxapex", description(garchomp, toxapex, moveUse("Earthquake")))
  }

  @Test
  fun multiscaleHalvesDamageAtFullHp() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val dragonite = pokemon("Dragonite", "Multiscale", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    assertEquals(listOf(69, 70, 70, 72, 72, 73, 75, 75, 76, 76, 78, 78, 79, 79, 81, 82), rolls(garchomp, dragonite, moveUse("Dragon Claw")))
    assertEquals("20+ Atk Garchomp Dragon Claw vs. 20 HP  / 8 Def Multiscale Dragonite", description(garchomp, dragonite, moveUse("Dragon Claw")))
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
    assertEquals("20+ Atk Garchomp Earthquake vs. 20 HP  / 20+ Def Toxapex", description(garchomp, toxapex, moveUse("Earthquake"), field))
  }

  @Test
  fun acrobaticsIsStrongerWithNoHeldItem() {
    val talonflame = pokemon("Talonflame", "Gale Wings", Nature.JOLLY, 20, 20, 6, 0, 6, 14)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(listOf(36, 36, 37, 37, 37, 39, 39, 39, 39, 40, 40, 40, 42, 42, 42, 43), rolls(talonflame, toxapex, moveUse("Acrobatics")))
    assertEquals("20 Atk Talonflame Acrobatics (110 BP) vs. 20 HP  / 20+ Def Toxapex", description(talonflame, toxapex, moveUse("Acrobatics")))
  }

  @Test
  fun facadeIsStrongerWhileStatused() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10, status = Status.PARALYZED)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(listOf(47, 48, 48, 49, 49, 50, 50, 51, 52, 52, 53, 53, 54, 54, 55, 56), rolls(garchomp, toxapex, moveUse("Facade")))
    assertEquals("20+ Atk Garchomp Facade (140 BP) vs. 20 HP  / 20+ Def Toxapex", description(garchomp, toxapex, moveUse("Facade")))
  }

  @Test
  fun bulletSeedSingleHitPower() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(listOf(9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 11), rolls(garchomp, toxapex, moveUse("Bullet Seed")))
    assertEquals(
      "20+ Atk Garchomp Bullet Seed (2 hits) vs. 20 HP  / 20+ Def Toxapex",
      DamageCalculator.calculateMove(garchomp, toxapex, moveUse("Bullet Seed"), Battlefield(), hits = 2).description,
    )
  }

  @Test
  fun sturdyBlocksOhkoMoves() {
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val skarmory = pokemon("Skarmory", "Sturdy", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(listOf(0), rolls(garchomp, skarmory, moveUse("Sheer Cold")))
    assertEquals("Garchomp Sheer Cold vs. Sturdy Skarmory", description(garchomp, skarmory, moveUse("Sheer Cold")))
  }

  @Test
  fun flashFireGrantsFireImmunity() {
    val charizard = pokemon("Charizard", "Blaze", Nature.TIMID, 20, 0, 6, 20, 8, 12)
    val toxapex = pokemon("Toxapex", "Flash Fire", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(listOf(0), rolls(charizard, toxapex, moveUse("Fire Blast")))
    assertEquals("Charizard Fire Blast vs. Flash Fire Toxapex", description(charizard, toxapex, moveUse("Fire Blast")))
  }

  @Test
  fun struggleIsNeutralWithNoStab() {
    val gengar = pokemon("Gengar", "Cursed Body", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val target = pokemon("Gengar", "Cursed Body", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertCalc(
      listOf(21, 21, 21, 22, 22, 22, 22, 23, 23, 23, 23, 24, 24, 24, 24, 25),
      "20+ Atk Gengar Struggle vs. 20 HP  / 20+ Def Gengar",
      gengar, target, moveUse("Struggle"),
    )
  }

  @Test
  fun parentalBondHitsTwice() {
    val garchomp = garchomp(ability = "Parental Bond")
    val result = DamageCalculator.calculateMove(garchomp, toxapex(), moveUse("Dragon Claw"), Battlefield())
    assertEquals(listOf(42, 42, 42, 43, 43, 43, 45, 45, 45, 46, 46, 46, 48, 48, 48, 49), result.hits[0].rolls)
    assertEquals(listOf(9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 12), result.hits[1].rolls)
    assertEquals("20+ Atk Parental Bond Garchomp Dragon Claw (2 hits) vs. 20 HP  / 20+ Def Toxapex", result.description)
  }

  @Test
  fun piercingDrillPunchesThroughProtect() {
    assertCalc(
      listOf(7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9),
      "20+ Atk Piercing Drill Garchomp Body Slam vs. 20 HP  / 20+ Def Toxapex through Protect",
      garchomp(ability = "Piercing Drill"), toxapex(), moveUse("Body Slam"), Battlefield(defenderSide = SideConditions(isProtected = true)),
    )
  }

  @Test
  fun protectAloneDoesNotReduceDamage() {
    assertCalc(
      listOf(29, 30, 30, 30, 31, 31, 31, 32, 32, 32, 33, 33, 33, 34, 34, 35),
      "20+ Atk Garchomp Body Slam vs. 20 HP  / 20+ Def Toxapex",
      garchomp(), toxapex(), moveUse("Body Slam"), Battlefield(defenderSide = SideConditions(isProtected = true)),
    )
  }

  @Test
  fun glaiveRushVulnerabilityDoublesDamage() {
    assertCalc(
      listOf(152, 152, 156, 156, 158, 162, 162, 164, 164, 168, 170, 170, 174, 174, 176, 180),
      "20+ Atk Garchomp Earthquake vs. 20 HP  / 20+ Def Toxapex after using Glaive Rush",
      garchomp(), toxapex().copy(isVulnerableFromGlaiveRush = true), moveUse("Earthquake"),
    )
  }

  @Test
  fun statDisplayInEvs() {
    assertCalc(
      listOf(74, 74, 78, 78, 78, 80, 80, 80, 80, 84, 84, 84, 86, 86, 86, 90),
      "156+ Atk Garchomp Earthquake vs. 156 HP  / 156+ Def Toxapex",
      garchomp(), toxapex(), moveUse("Earthquake"), statDisplay = StatDisplay.EVS,
    )
  }

  @Test
  fun statDisplayInRawStats() {
    assertCalc(
      listOf(74, 74, 78, 78, 78, 80, 80, 80, 80, 84, 84, 84, 86, 86, 86, 90),
      "187 Atk Garchomp Earthquake vs. 145 HP  / 211 Def Toxapex",
      garchomp(), toxapex(), moveUse("Earthquake"), statDisplay = StatDisplay.STATS,
    )
  }

  @Test
  fun foulPlayAgainstABoostedTargetInEvs() {
    assertCalc(
      listOf(82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97),
      "+1 Toxapex Foul Play vs. 156 HP  / 156+ Atk / 60 Def Garchomp",
      toxapex(), garchomp(boosts = StatBoosts(attack = 1)), moveUse("Foul Play"), statDisplay = StatDisplay.EVS,
    )
  }

  @Test
  fun boostsOnBothSides() {
    assertCalc(
      listOf(168, 170, 174, 174, 176, 180, 180, 182, 186, 186, 188, 192, 192, 194, 198, 200),
      "+1 20+ Atk Garchomp Earthquake vs. -1 20 HP  / 20+ Def Toxapex",
      garchomp(boosts = StatBoosts(attack = 1)), toxapex(boosts = StatBoosts(defense = -1)), moveUse("Earthquake"),
    )
  }

  @Test
  fun megaCharizardXInSunThroughReflect() {
    val charizard = pokemon("Mega Charizard X", "Tough Claws", Nature.ADAMANT, 2, 32, 0, 0, 0, 32, item = "Life Orb", boosts = StatBoosts(attack = 1))
    val incineroar = pokemon("Incineroar", "Intimidate", Nature.CAREFUL, 32, 2, 0, 0, 32, 0)
    assertCalc(
      listOf(156, 158, 160, 161, 163, 165, 166, 169, 171, 173, 174, 176, 178, 179, 182, 184),
      "+1 32+ Atk Life Orb Tough Claws Mega Charizard X Flare Blitz vs. 32 HP  / 0 Def Incineroar in Sun through Reflect",
      charizard, incineroar, moveUse("Flare Blitz"), Battlefield(weather = Weather.SUN, defenderSide = SideConditions(hasReflect = true)),
    )
  }

  @Test
  fun rainBoostsWater() {
    assertCalc(
      listOf(123, 124, 126, 127, 129, 130, 132, 133, 135, 136, 138, 139, 141, 142, 144, 145),
      "20+ SpA Pelipper Hydro Pump vs. 20 HP  / 8 SpD Garchomp in Rain",
      specialAttacker("Pelipper", "Drizzle"), garchomp(), moveUse("Hydro Pump"), Battlefield(weather = Weather.RAIN),
    )
  }

  @Test
  fun rainWeakensFire() {
    assertCalc(
      listOf(62, 62, 62, 66, 66, 66, 66, 68, 68, 68, 68, 72, 72, 72, 72, 74),
      "20+ SpA Charizard Flamethrower vs. 20 HP  / 10 SpD Venusaur in Rain",
      specialAttacker("Charizard", "Blaze"), venusaur(), moveUse("Flamethrower"), Battlefield(weather = Weather.RAIN),
    )
  }

  @Test
  fun sandAndMistyTerrain() {
    val tyranitar = pokemon("Tyranitar", "Sand Stream", Nature.CAREFUL, 20, 10, 10, 0, 20, 6)
    assertCalc(
      listOf(12, 12, 13, 13, 13, 13, 13, 13, 13, 14, 14, 14, 14, 14, 14, 15),
      "20+ SpA Charizard Dragon Pulse vs. 20 HP  / 20+ SpD Tyranitar in Sand and Misty Terrain",
      specialAttacker("Charizard", "Blaze"), tyranitar, moveUse("Dragon Pulse"), Battlefield(weather = Weather.SAND, terrain = Terrain.MISTY),
    )
  }

  @Test
  fun snowBoostsIceTypesDefense() {
    val glalie = pokemon("Glalie", "Inner Focus", Nature.IMPISH, 20, 10, 20, 0, 6, 10)
    assertCalc(
      listOf(40, 40, 40, 42, 42, 42, 43, 43, 43, 45, 45, 45, 46, 46, 46, 48),
      "20+ Atk Garchomp Earthquake vs. 20 HP  / 20+ Def Glalie in Snow",
      garchomp(), glalie, moveUse("Earthquake"), Battlefield(weather = Weather.SNOW),
    )
  }

  @Test
  fun megaSolIsCreditedForTheSunBoost() {
    assertCalc(
      listOf(29, 29, 30, 30, 30, 31, 31, 31, 32, 32, 32, 33, 33, 33, 34, 34),
      "20+ SpA Mega Sol Mega Meganium Flamethrower vs. 20 HP  / 16 SpD Toxapex",
      specialAttacker("Mega Meganium", "Mega Sol"), toxapex(), moveUse("Flamethrower"),
    )
  }

  @Test
  fun terrainPulseInElectricTerrain() {
    val pelipper = pokemon("Pelipper", "Drizzle", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertCalc(
      listOf(364, 364, 372, 376, 384, 384, 388, 396, 396, 400, 408, 412, 412, 420, 424, 432),
      "20 SpA Raichu Terrain Pulse (100 BP Electric) vs. 20 HP  / 16 SpD Pelipper in Electric Terrain",
      specialAttacker("Raichu", "Lightning Rod", Nature.TIMID), pelipper, moveUse("Terrain Pulse"), Battlefield(terrain = Terrain.ELECTRIC),
    )
  }

  @Test
  fun grassyTerrainHalvesEarthquake() {
    assertCalc(
      listOf(38, 38, 38, 42, 42, 42, 42, 42, 42, 44, 44, 44, 44, 44, 44, 48),
      "20+ Atk Garchomp Earthquake vs. 20 HP  / 20+ Def Toxapex in Grassy Terrain",
      garchomp(), toxapex(), moveUse("Earthquake"), Battlefield(terrain = Terrain.GRASSY),
    )
  }

  @Test
  fun psychicTerrainBlocksPriority() {
    val kingambit = pokemon("Kingambit", "Defiant", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    assertCalc(listOf(0), "Kingambit Sucker Punch vs. Toxapex in Psychic Terrain", kingambit, toxapex(), moveUse("Sucker Punch"), Battlefield(terrain = Terrain.PSYCHIC))
  }

  @Test
  fun lightScreen() {
    assertCalc(
      listOf(85, 85, 88, 88, 89, 89, 92, 92, 93, 93, 96, 96, 97, 97, 100, 101),
      "20+ SpA Charizard Flamethrower vs. 20 HP  / 10 SpD Venusaur through Light Screen",
      specialAttacker("Charizard", "Blaze"), venusaur(), moveUse("Flamethrower"), Battlefield(defenderSide = SideConditions(hasLightScreen = true)),
    )
  }

  @Test
  fun auroraVeilIsMentionedOverReflect() {
    assertCalc(
      listOf(49, 49, 52, 52, 52, 53, 53, 53, 53, 56, 56, 56, 57, 57, 57, 60),
      "20+ Atk Garchomp Earthquake vs. 20 HP  / 20+ Def Toxapex through Aurora Veil",
      garchomp(), toxapex(), moveUse("Earthquake"), Battlefield(defenderSide = SideConditions(hasAuroraVeil = true, hasReflect = true)),
    )
  }

  @Test
  fun gravityGroundsFlyingTypes() {
    val charizard = pokemon("Charizard", "Blaze", Nature.TIMID, 20, 0, 6, 20, 8, 12)
    assertCalc(
      listOf(152, 156, 158, 158, 162, 162, 164, 168, 168, 170, 170, 174, 176, 176, 180, 182),
      "20+ Atk Garchomp Earthquake vs. 20 HP  / 6 Def Charizard under Gravity",
      garchomp(), charizard, moveUse("Earthquake"), Battlefield(isGravity = true),
    )
  }

  @Test
  fun thickFatDefender() {
    val snorlax = pokemon("Snorlax", "Thick Fat", Nature.CAREFUL, 20, 10, 10, 0, 20, 6)
    assertCalc(
      listOf(25, 27, 27, 27, 27, 27, 28, 28, 28, 28, 28, 30, 30, 30, 30, 31),
      "20+ SpA Charizard Flamethrower vs. 20 HP  / 20+ SpD Thick Fat Snorlax",
      specialAttacker("Charizard", "Blaze"), snorlax, moveUse("Flamethrower"),
    )
  }

  @Test
  fun unawareDefenderIgnoresAttackBoost() {
    val clefable = pokemon("Clefable", "Unaware", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertCalc(
      listOf(64, 64, 66, 66, 67, 67, 69, 69, 70, 70, 72, 72, 73, 73, 75, 76),
      "+2 20+ Atk Garchomp Earthquake vs. 20 HP  / 20+ Def Unaware Clefable",
      garchomp(boosts = StatBoosts(attack = 2)), clefable, moveUse("Earthquake"),
    )
  }

  @Test
  fun unawareAttackerIgnoresDefenseBoost() {
    assertCalc(
      listOf(140, 144, 144, 146, 146, 150, 150, 152, 156, 156, 158, 158, 162, 162, 164, 168),
      "20+ SpA Unaware Clefable Moonblast vs. +1 20 HP  / 8 SpD Garchomp",
      specialAttacker("Clefable", "Unaware"), garchomp(boosts = StatBoosts(specialDefense = 1)), moveUse("Moonblast"),
    )
  }

  @Test
  fun solidRock() {
    val rhyperior = pokemon("Rhyperior", "Solid Rock", Nature.IMPISH, 20, 10, 20, 0, 6, 10)
    assertCalc(
      listOf(63, 64, 64, 64, 67, 67, 67, 69, 69, 69, 72, 72, 72, 73, 73, 76),
      "20+ Atk Garchomp Earthquake vs. 20 HP  / 20+ Def Solid Rock Rhyperior",
      garchomp(), rhyperior, moveUse("Earthquake"),
    )
  }

  @Test
  fun drySkinTakesMoreFireDamage() {
    assertCalc(
      listOf(29, 30, 30, 30, 30, 31, 31, 32, 32, 33, 33, 33, 33, 34, 34, 35),
      "20+ SpA Charizard Flamethrower vs. 20 HP  / 16 SpD Dry Skin Toxapex",
      specialAttacker("Charizard", "Blaze"), toxapex(ability = "Dry Skin"), moveUse("Flamethrower"),
    )
  }

  @Test
  fun fluffyHalvesContactDamage() {
    assertCalc(
      listOf(21, 21, 21, 21, 21, 21, 22, 22, 22, 23, 23, 23, 24, 24, 24, 24),
      "20+ Atk Garchomp Dragon Claw vs. 20 HP  / 20+ Def Fluffy Toxapex",
      garchomp(), toxapex(ability = "Fluffy"), moveUse("Dragon Claw"),
    )
  }

  @Test
  fun airBalloonImmunity() {
    assertCalc(listOf(0), "Garchomp Earthquake vs. Air Balloon Toxapex", garchomp(), toxapex(item = "Air Balloon"), moveUse("Earthquake"))
  }

  @Test
  fun levitateImmunity() {
    val rotom = pokemon("Rotom-Wash", "Levitate", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertCalc(listOf(0), "Garchomp Earthquake vs. Levitate Rotom-Wash", garchomp(), rotom, moveUse("Earthquake"))
  }

  @Test
  fun knockOffAgainstAnItemHolder() {
    val kingambit = pokemon("Kingambit", "Defiant", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    assertCalc(
      listOf(51, 51, 51, 52, 52, 54, 54, 54, 55, 55, 57, 57, 57, 58, 58, 60),
      "20+ Atk Kingambit Knock Off (97.5 BP) vs. 20 HP  / 20+ Def Toxapex",
      kingambit, toxapex(item = "Leftovers"), moveUse("Knock Off"),
    )
  }

  @Test
  fun hexAgainstAStatusedTarget() {
    assertCalc(
      listOf(70, 72, 72, 73, 73, 75, 75, 76, 78, 78, 79, 79, 81, 81, 82, 84),
      "20 SpA Gengar Hex (130 BP) vs. 20 HP  / 16 SpD Toxapex",
      specialAttacker("Gengar", "Cursed Body", Nature.TIMID), toxapex(status = Status.POISONED), moveUse("Hex"),
    )
  }

  @Test
  fun solarBeamInRain() {
    assertCalc(
      listOf(30, 30, 30, 31, 31, 31, 31, 33, 33, 33, 33, 34, 34, 34, 34, 36),
      "20+ SpA Venusaur Solar Beam (60 BP) vs. 20 HP  / 16 SpD Toxapex in Rain",
      specialAttacker("Venusaur", "Chlorophyll"), toxapex(), moveUse("Solar Beam"), Battlefield(weather = Weather.RAIN),
    )
  }

  @Test
  fun storedPowerWithBoosts() {
    assertCalc(
      listOf(117, 118, 120, 120, 121, 123, 124, 126, 127, 129, 130, 132, 133, 135, 136, 138),
      "+2 20+ SpA Hatterene Stored Power (60 BP) vs. 20 HP  / 8 SpD Garchomp",
      specialAttacker("Hatterene", "Magic Bounce", boosts = StatBoosts(specialAttack = 2)), garchomp(), moveUse("Stored Power"),
    )
  }

  @Test
  fun lastRespectsAfterFaintedAllies() {
    val gengar = pokemon("Gengar", "Cursed Body", Nature.ADAMANT, 20, 20, 6, 0, 8, 12)
    assertCalc(
      listOf(46, 46, 48, 48, 48, 49, 49, 51, 51, 51, 52, 52, 52, 54, 54, 55),
      "20+ Atk Gengar Last Respects (150 BP) vs. 20 HP  / 20+ Def Toxapex",
      gengar, toxapex(), moveUse("Last Respects").copy(priorPowerBoosts = 2),
    )
  }

  @Test
  fun waterSpoutAlwaysMentionsItsPower() {
    assertCalc(
      listOf(84, 84, 85, 87, 87, 88, 90, 90, 91, 93, 93, 94, 96, 96, 97, 99),
      "20+ SpA Pelipper Water Spout (150 BP) vs. 20 HP  / 8 SpD Garchomp",
      specialAttacker("Pelipper", "Drizzle"), garchomp(), moveUse("Water Spout"),
    )
  }

  @Test
  fun helpingHandPowerSpotAndBattery() {
    assertCalc(
      listOf(320, 326, 330, 332, 338, 342, 344, 348, 354, 356, 360, 362, 368, 372, 374, 380),
      "20+ SpA Charizard Helping Hand Power Spot Battery Flamethrower vs. 20 HP  / 10 SpD Venusaur",
      specialAttacker("Charizard", "Blaze"), venusaur(), moveUse("Flamethrower"),
      Battlefield(attackerSide = SideConditions(hasHelpingHand = true, hasPowerSpot = true, hasBattery = true)),
    )
  }

  @Test
  fun allySteelySpirit() {
    val metagross = pokemon("Metagross", "Clear Body", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    assertCalc(
      listOf(31, 32, 32, 33, 33, 33, 33, 34, 34, 35, 35, 36, 36, 36, 36, 37),
      "20+ Atk Metagross Ally Steely Spirit Iron Head vs. 20 HP  / 20+ Def Toxapex",
      metagross, toxapex(), moveUse("Iron Head"), Battlefield(attackerSide = SideConditions(hasAllySteelySpirit = true)),
    )
  }

  @Test
  fun chargedElectricMove() {
    assertCalc(
      listOf(150, 150, 152, 152, 156, 158, 158, 162, 162, 164, 168, 168, 170, 170, 174, 176),
      "20 SpA Raichu Charged Thunderbolt vs. 20 HP  / 16 SpD Toxapex",
      specialAttacker("Raichu", "Lightning Rod", Nature.TIMID), toxapex(), moveUse("Thunderbolt"), Battlefield(isCharge = true),
    )
  }

  @Test
  fun supremeOverlord() {
    val kingambit = pokemon("Kingambit", "Supreme Overlord", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    assertCalc(
      listOf(52, 54, 54, 54, 55, 55, 57, 57, 58, 58, 58, 60, 60, 61, 61, 63),
      "20+ Atk Supreme Overlord (2 allies down) Kingambit Kowtow Cleave vs. 20 HP  / 20+ Def Toxapex",
      kingambit, toxapex(), moveUse("Kowtow Cleave").copy(faintedAllyCount = 2),
    )
  }

  @Test
  fun rivalryOppositeGender() {
    val garchomp = pokemon("Garchomp", "Rivalry", Nature.ADAMANT, 20, 20, 8, 0, 8, 10, gender = Gender.MALE)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6, gender = Gender.FEMALE)
    assertCalc(
      listOf(56, 56, 60, 60, 60, 60, 60, 62, 62, 62, 62, 66, 66, 66, 66, 68),
      "20+ Atk Rivalry (0.75x) Garchomp Earthquake vs. 20 HP  / 20+ Def Toxapex",
      garchomp, toxapex, moveUse("Earthquake"),
    )
  }

  @Test
  fun sandForce() {
    assertCalc(
      listOf(98, 98, 98, 102, 102, 104, 104, 104, 108, 108, 110, 110, 110, 114, 114, 116),
      "20+ Atk Sand Force Garchomp Earthquake vs. 20 HP  / 20+ Def Toxapex in Sand",
      garchomp(ability = "Sand Force"), toxapex(), moveUse("Earthquake"), Battlefield(weather = Weather.SAND),
    )
  }

  @Test
  fun scrappyHitsGhosts() {
    val gengar = pokemon("Gengar", "Cursed Body", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertCalc(
      listOf(38, 39, 39, 40, 40, 40, 41, 41, 42, 42, 43, 43, 44, 44, 45, 45),
      "20+ Atk Scrappy Garchomp Close Combat vs. 20 HP  / 20+ Def Gengar",
      garchomp(ability = "Scrappy"), gengar, moveUse("Close Combat"),
    )
  }

  @Test
  fun liquidVoice() {
    assertCalc(
      listOf(61, 63, 63, 64, 64, 66, 66, 67, 67, 69, 69, 70, 70, 72, 72, 73),
      "20+ SpA Liquid Voice Primarina Hyper Voice vs. 20 HP  / 8 SpD Garchomp",
      specialAttacker("Primarina", "Liquid Voice"), garchomp(), moveUse("Hyper Voice"),
    )
  }

  @Test
  fun pixilate() {
    assertCalc(
      listOf(134, 134, 138, 138, 140, 140, 144, 144, 146, 146, 150, 150, 152, 152, 156, 158),
      "20+ SpA Pixilate Sylveon Hyper Voice vs. 20 HP  / 8 SpD Garchomp",
      specialAttacker("Sylveon", "Pixilate"), garchomp(), moveUse("Hyper Voice"),
    )
  }

  @Test
  fun activeLibero() {
    val cinderace = pokemon("Cinderace", "Libero", Nature.JOLLY, 20, 20, 8, 0, 8, 10, abilityIsActive = true)
    assertCalc(
      listOf(25, 26, 26, 27, 27, 27, 27, 27, 28, 28, 28, 29, 29, 30, 30, 30),
      "20 Atk Libero Cinderace Close Combat vs. 20 HP  / 20+ Def Toxapex",
      cinderace, toxapex(), moveUse("Close Combat"),
    )
  }

  @Test
  fun hustle() {
    assertCalc(
      listOf(114, 114, 116, 116, 120, 120, 120, 122, 122, 126, 126, 128, 128, 132, 132, 134),
      "20+ Atk Hustle Garchomp Earthquake vs. 20 HP  / 20+ Def Toxapex",
      garchomp(ability = "Hustle"), toxapex(), moveUse("Earthquake"),
    )
  }

  @Test
  fun gutsIgnoresTheBurn() {
    assertCalc(
      listOf(114, 114, 116, 116, 120, 120, 120, 122, 122, 126, 126, 128, 128, 132, 132, 134),
      "20+ Atk Guts Garchomp Earthquake vs. 20 HP  / 20+ Def Toxapex",
      garchomp(ability = "Guts", status = Status.BURNED), toxapex(), moveUse("Earthquake"),
    )
  }

  @Test
  fun statusMove() {
    assertCalc(listOf(0), "Garchomp Swords Dance vs. Toxapex", garchomp(), toxapex(), moveUse("Swords Dance"))
  }

  @Test
  fun bulletSeedThreeHits() {
    val result = DamageCalculator.calculateMove(garchomp(), toxapex(), moveUse("Bullet Seed"), Battlefield(), hits = 3)
    assertEquals(List(3) { listOf(9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 11) }, result.hits.map { it.rolls })
    assertEquals("20+ Atk Garchomp Bullet Seed (3 hits) vs. 20 HP  / 20+ Def Toxapex", result.description)
  }

  @Test
  fun tripleAxel() {
    val result = DamageCalculator.calculateMove(garchomp(), toxapex(), moveUse("Triple Axel"), Battlefield(), hits = 3)
    assertEquals(
      listOf(
        listOf(3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4),
        listOf(7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8),
        listOf(10, 10, 10, 11, 11, 11, 11, 11, 11, 11, 11, 12, 12, 12, 12, 12),
      ),
      result.hits.map { it.rolls },
    )
    assertEquals("20+ Atk Garchomp Triple Axel (3 hits) vs. 20 HP  / 20+ Def Toxapex", result.description)
  }

  @Test
  fun dragonDartsOneHit() {
    val result = DamageCalculator.calculateMove(garchomp(), toxapex(), moveUse("Dragon Darts"), Battlefield(), hits = 1)
    assertEquals(listOf(listOf(25, 27, 27, 27, 27, 27, 28, 28, 28, 28, 28, 30, 30, 30, 30, 31)), result.hits.map { it.rolls })
    assertEquals("20+ Atk Garchomp Dragon Darts (1 hits) vs. 20 HP  / 20+ Def Toxapex", result.description)
  }

  @Test
  fun abilityNameLookupIsCaseInsensitive() {
    // Exercises the AbilityName -> Ability resolution boundary (BattlePokemon.resolvedAbility)
    // rather than assuming callers already hand over Champions' exact display casing.
    val lowercased = pokemon("Garchomp", "adaptability", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val exact = pokemon("Garchomp", "Adaptability", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(calc(exact, toxapex, moveUse("Earthquake")), calc(lowercased, toxapex, moveUse("Earthquake")))
  }

  @Test
  fun unrecognizedAbilityIsTreatedAsNoAbility() {
    val typo = pokemon("Garchomp", "Rough Skinn", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val none = pokemon("Garchomp", "", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    val toxapex = pokemon("Toxapex", "Merciless", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertEquals(rolls(none, toxapex, moveUse("Earthquake")), rolls(typo, toxapex, moveUse("Earthquake")))
  }
}
