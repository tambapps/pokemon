package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.AbilityName
import com.tambapps.pokemon.ItemName
import com.tambapps.pokemon.MoveName
import com.tambapps.pokemon.Nature
import com.tambapps.pokemon.PokeStats
import com.tambapps.pokemon.PokemonName
import com.tambapps.pokemon.champions.data.ChampionsDex
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Like [DamageCalculatorCrossValidationTest], every expected value here is tools/oracle.js's output for the scenario of
 * the same name in tools/scenarios.json: the real calculator's CALCULATE_ALL_MOVES_SV, setup pass included. These
 * cover the mechanics the source handles beyond the plain damage formula: its setup pass (Intimidate, seeds, Trace...),
 * Mold Breaker, fixed-damage moves, and the state changing between the hits of a move.
 */
class SourceParityCrossValidationTest {

  private fun pokemon(
    species: String,
    ability: String,
    nature: Nature,
    hp: Int, attack: Int, defense: Int, specialAttack: Int, specialDefense: Int, speed: Int,
    item: String? = null,
    boosts: StatBoosts = StatBoosts.NONE,
    abilityIsActive: Boolean = false,
    hpFraction: Double? = null,
  ): BattlePokemon {
    val pokemon = BattlePokemon(
      species = ChampionsDex.species(PokemonName(species)),
      ability = AbilityName(ability),
      nature = nature,
      statPoints = PokeStats(hp = hp, attack = attack, defense = defense, specialAttack = specialAttack, specialDefense = specialDefense, speed = speed),
      item = item?.let(::ItemName),
      boosts = boosts,
      abilityIsActive = abilityIsActive,
    )
    // the oracle's curHpFraction, rounded like JS's Math.round
    return if (hpFraction == null) pokemon else pokemon.copy(currentHp = (pokemon.maxHp * hpFraction).roundToInt())
  }

  private fun garchomp(ability: String = "Rough Skin", item: String? = null, hpFraction: Double? = null) =
    pokemon("Garchomp", ability, Nature.ADAMANT, 20, 20, 8, 0, 8, 10, item = item, hpFraction = hpFraction)
  private fun toxapex(ability: String = "Merciless", item: String? = null, hpFraction: Double? = null) =
    pokemon("Toxapex", ability, Nature.BOLD, 20, 0, 20, 0, 16, 6, item = item, hpFraction = hpFraction)
  private fun kingambit(ability: String = "Defiant", item: String? = null) =
    pokemon("Kingambit", ability, Nature.ADAMANT, 20, 20, 8, 0, 8, 10, item = item)
  private fun specialAttacker(species: String, ability: String, nature: Nature = Nature.MODEST, boosts: StatBoosts = StatBoosts.NONE, hpFraction: Double? = null) =
    pokemon(species, ability, nature, 20, 0, 6, 20, 8, 12, boosts = boosts, hpFraction = hpFraction)
  private fun incineroar(nature: Nature = Nature.CAREFUL) =
    pokemon("Incineroar", "Intimidate", nature, 32, if (nature == Nature.CAREFUL) 2 else 20, 0, 0, if (nature == Nature.CAREFUL) 32 else 14, 0, abilityIsActive = true)
  private fun gengarWall() = pokemon("Gengar", "Cursed Body", Nature.TIMID, 0, 0, 0, 20, 32, 14)

  private fun move(name: String, isCritical: Boolean = false, isPowerDoubled: Boolean = false, countered: String? = null) = MoveUse(
    ChampionsDex.move(MoveName(name)),
    isCritical = isCritical,
    isPowerDoubled = isPowerDoubled,
    counteredMove = countered?.let { MoveUse(ChampionsDex.move(MoveName(it))) },
  )

  /**
   * [expectedHits] are the oracle's distinct hits in order (a single list when every hit is the same), the hits past
   * the last one being the same as it.
   */
  private fun assertMove(
    expectedHits: List<List<Int>>,
    expectedDescription: String,
    attacker: BattlePokemon,
    defender: BattlePokemon,
    moveUse: MoveUse,
    field: Battlefield = Battlefield(),
    hits: Int = 1,
  ) {
    val result = DamageCalculator.calculateMove(attacker, defender, moveUse, field, hits)
    val rolls = result.hits.map { it.rolls }
    assertEquals(expectedHits, rolls.take(expectedHits.size))
    rolls.drop(expectedHits.size).forEach { assertEquals(expectedHits.last(), it) }
    assertEquals(expectedDescription, result.description)
  }

  // --- Mold Breaker, crit-blocking abilities, contact, priority, spread ---

  @Test
  fun moldBreakerIgnoresMultiscale() {
    val dragonite = pokemon("Dragonite", "Multiscale", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    assertMove(
      listOf(listOf(138, 140, 140, 144, 144, 146, 150, 150, 152, 152, 156, 156, 158, 158, 162, 164)),
      "20+ Atk Mold Breaker Garchomp Dragon Claw vs. 20 HP  / 8 Def Dragonite",
      garchomp("Mold Breaker"), dragonite, move("Dragon Claw"),
    )
  }

  @Test
  fun moldBreakerIgnoresLevitateAndFriendGuard() {
    val rotom = pokemon("Rotom-Wash", "Levitate", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertMove(
      listOf(listOf(102, 102, 102, 104, 104, 108, 108, 108, 110, 110, 114, 114, 114, 116, 116, 120)),
      "20+ Atk Mold Breaker Garchomp Earthquake vs. 20 HP  / 20+ Def Rotom-Wash",
      garchomp("Mold Breaker"), rotom, move("Earthquake"), Battlefield(defenderSide = SideConditions(hasFriendGuard = true)),
    )
  }

  @Test
  fun battleArmorBlocksCrits() {
    assertMove(
      listOf(listOf(74, 74, 78, 78, 78, 80, 80, 80, 80, 84, 84, 84, 86, 86, 86, 90)),
      "20+ Atk Garchomp Earthquake vs. 20 HP  / 20+ Def Toxapex",
      garchomp(), toxapex("Battle Armor"), move("Earthquake", isCritical = true),
    )
  }

  @Test
  fun moldBreakerCritsThroughShellArmor() {
    assertMove(
      listOf(listOf(114, 114, 116, 116, 120, 120, 120, 122, 122, 126, 126, 128, 128, 132, 132, 134)),
      "20+ Atk Mold Breaker Garchomp Earthquake vs. 20 HP  / 20+ Def Toxapex on a critical hit",
      garchomp("Mold Breaker"), toxapex("Shell Armor"), move("Earthquake", isCritical = true),
    )
  }

  @Test
  fun longReachAvoidsFluffy() {
    assertMove(
      listOf(listOf(42, 42, 42, 43, 43, 43, 45, 45, 45, 46, 46, 46, 48, 48, 48, 49)),
      "20+ Atk Garchomp Dragon Claw vs. 20 HP  / 20+ Def Toxapex",
      garchomp("Long Reach"), toxapex("Fluffy"), move("Dragon Claw"),
    )
  }

  @Test
  fun physicalShellSideArmMakesContact() {
    val garchomp = pokemon("Garchomp", "Tough Claws", Nature.ADAMANT, 20, 32, 8, 0, 8, 10)
    assertMove(
      listOf(listOf(27, 27, 28, 28, 28, 29, 29, 29, 30, 30, 30, 31, 31, 31, 32, 32)),
      "32+ Atk Tough Claws Garchomp Shell Side Arm vs. 0 HP  / 0 Def Gengar",
      garchomp, gengarWall(), move("Shell Side Arm"),
    )
  }

  @Test
  fun galeWingsPriorityIsBlockedByPsychicTerrain() {
    val talonflame = pokemon("Talonflame", "Gale Wings", Nature.JOLLY, 20, 20, 6, 0, 6, 14)
    assertMove(listOf(listOf(0)), "Talonflame Brave Bird vs. Garchomp in Psychic Terrain", talonflame, garchomp(), move("Brave Bird"), Battlefield(terrain = Terrain.PSYCHIC))
  }

  @Test
  fun expandingForceInPsychicTerrain() {
    assertMove(
      listOf(listOf(134, 134, 138, 138, 140, 140, 144, 144, 146, 146, 150, 150, 152, 152, 156, 158)),
      "20+ SpA Gardevoir Expanding Force (120 BP) vs. 20 HP  / 16 SpD Toxapex in Psychic Terrain",
      specialAttacker("Gardevoir", "Trace"), toxapex(), move("Expanding Force"), Battlefield(terrain = Terrain.PSYCHIC),
    )
  }

  // --- move type and power ---

  @Test
  fun weatherBallWithMegaSol() {
    assertMove(
      listOf(listOf(32, 32, 33, 33, 33, 34, 34, 34, 35, 35, 36, 36, 36, 37, 37, 38)),
      "20+ SpA Mega Sol Mega Meganium Weather Ball (100 BP Fire) vs. 20 HP  / 16 SpD Toxapex",
      specialAttacker("Mega Meganium", "Mega Sol"), toxapex(), move("Weather Ball"),
    )
  }

  @Test
  fun auraWheelIsDarkForHangryMorpeko() {
    val morpeko = pokemon("Morpeko-Hangry", "Hunger Switch", Nature.JOLLY, 20, 20, 6, 0, 6, 14)
    assertMove(
      listOf(listOf(210, 212, 216, 218, 218, 222, 224, 228, 230, 234, 234, 236, 240, 242, 246, 248)),
      "20 Atk Morpeko-Hangry Aura Wheel vs. 0 HP  / 0 Def Gengar",
      morpeko, gengarWall(), move("Aura Wheel"),
    )
  }

  @Test
  fun paybackDoubled() {
    assertMove(
      listOf(listOf(52, 54, 54, 54, 55, 55, 57, 57, 58, 58, 58, 60, 60, 61, 61, 63)),
      "20+ Atk Kingambit Payback (100 BP) vs. 20 HP  / 20+ Def Toxapex",
      kingambit(), toxapex(), move("Payback", isPowerDoubled = true),
    )
  }

  @Test
  fun lashOutDoubled() {
    assertMove(
      listOf(listOf(78, 79, 79, 81, 82, 82, 84, 85, 85, 87, 87, 88, 90, 90, 91, 93)),
      "20+ Atk Kingambit Lash Out (150 BP) vs. 20 HP  / 20+ Def Toxapex",
      kingambit(), toxapex(), move("Lash Out", isPowerDoubled = true),
    )
  }

  @Test
  fun normalGem() {
    assertMove(
      listOf(listOf(38, 38, 39, 39, 40, 40, 40, 41, 41, 42, 42, 43, 43, 44, 44, 45)),
      "20+ Atk Normal Gem Garchomp Body Slam vs. 20 HP  / 20+ Def Toxapex",
      garchomp(item = "Normal Gem"), toxapex(), move("Body Slam"),
    )
  }

  @Test
  fun gravAppleUnderGravity() {
    val venusaur = pokemon("Venusaur", "Overgrow", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    assertMove(
      listOf(listOf(49, 49, 49, 51, 51, 52, 52, 52, 54, 54, 55, 55, 55, 57, 57, 58)),
      "20+ Atk Venusaur Grav Apple (135 BP) vs. 20 HP  / 20+ Def Toxapex",
      venusaur, toxapex(), move("Grav Apple"), Battlefield(isGravity = true),
    )
  }

  @Test
  fun mistyExplosionInMistyTerrain() {
    assertMove(
      listOf(listOf(204, 204, 206, 210, 212, 216, 216, 218, 222, 224, 228, 228, 230, 234, 236, 240)),
      "20+ SpA Gardevoir Misty Explosion (150 BP) vs. 20 HP  / 8 SpD Garchomp",
      specialAttacker("Gardevoir", "Trace"), garchomp(), move("Misty Explosion"), Battlefield(terrain = Terrain.MISTY),
    )
  }

  @Test
  fun flingIronBall() {
    assertMove(
      listOf(listOf(115, 117, 118, 120, 120, 121, 123, 124, 126, 127, 129, 130, 132, 133, 135, 136)),
      "20+ Atk Iron Ball Kingambit Fling (130 BP) vs. 20 HP  / 8 Def Garchomp",
      kingambit(item = "Iron Ball"), garchomp(), move("Fling"),
    )
  }

  @Test
  fun flingWithoutItemFails() {
    assertMove(listOf(listOf(0)), "Kingambit Fling vs. Garchomp", kingambit(), garchomp(), move("Fling"))
  }

  @Test
  fun flingingItsOwnMegaStoneFails() {
    val charizard = pokemon("Charizard", "Blaze", Nature.ADAMANT, 20, 20, 8, 0, 8, 10, item = "Charizardite X")
    assertMove(listOf(listOf(0)), "Charizardite X Charizard Fling vs. Garchomp", charizard, garchomp(), move("Fling"))
  }

  @Test
  fun flingingABerryAgainstUnnerveFails() {
    assertMove(listOf(listOf(0)), "Sitrus Berry Kingambit Fling vs. Garchomp", kingambit(item = "Sitrus Berry"), garchomp("Unnerve"), move("Fling"))
  }

  @Test
  fun meteorBeamRaisesSpAtkFirst() {
    assertMove(
      listOf(listOf(69, 70, 71, 72, 72, 73, 74, 75, 76, 77, 77, 78, 79, 80, 81, 82)),
      "+1 20+ SpA Gardevoir Meteor Beam vs. 20 HP  / 16 SpD Toxapex",
      specialAttacker("Gardevoir", "Trace"), toxapex(), move("Meteor Beam"),
    )
  }

  @Test
  fun meteorBeamWithContraryLowersSpAtk() {
    assertMove(
      listOf(listOf(69, 70, 71, 72, 72, 73, 74, 75, 76, 77, 77, 78, 79, 80, 81, 82)),
      "+1 20+ SpA Gardevoir Meteor Beam vs. 20 HP  / 16 SpD Toxapex",
      specialAttacker("Gardevoir", "Contrary", boosts = StatBoosts(specialAttack = 2)), toxapex(), move("Meteor Beam"),
    )
  }

  @Test
  fun gyroBallUsesTheFinalSpeeds() {
    val metagross = pokemon("Metagross", "Clear Body", Nature.BRAVE, 20, 20, 8, 0, 8, 0)
    val garchomp = pokemon("Garchomp", "Rough Skin", Nature.JOLLY, 20, 20, 8, 0, 8, 32, item = "Choice Scarf")
    assertMove(
      listOf(listOf(70, 72, 72, 73, 73, 75, 75, 76, 78, 78, 79, 79, 81, 81, 82, 84)),
      "20+ Atk Metagross Gyro Ball (79 BP) vs. 20 HP  / 8 Def Garchomp",
      metagross, garchomp, move("Gyro Ball"),
    )
  }

  @Test
  fun electroBallUnderTailwind() {
    val raichu = pokemon("Raichu", "Lightning Rod", Nature.TIMID, 20, 0, 6, 20, 8, 32)
    assertMove(
      listOf(listOf(126, 128, 128, 132, 132, 134, 134, 138, 138, 140, 140, 144, 144, 146, 146, 150)),
      "20 SpA Raichu Electro Ball (150 BP) vs. 20 HP  / 16 SpD Toxapex",
      raichu, toxapex(), move("Electro Ball"), Battlefield(attackerSide = SideConditions(hasTailwind = true)),
    )
  }

  @Test
  fun knockOffCantRemoveAMegaStone() {
    val charizard = pokemon("Mega Charizard X", "Tough Claws", Nature.ADAMANT, 20, 20, 8, 0, 8, 10, item = "Charizardite X")
    assertMove(
      listOf(listOf(51, 52, 52, 54, 54, 54, 55, 55, 57, 57, 57, 58, 58, 60, 60, 61)),
      "20+ Atk Kingambit Knock Off vs. 20 HP  / 8 Def Mega Charizard X",
      kingambit(), charizard, move("Knock Off"),
    )
  }

  @Test
  fun knockOffAgainstAKlutzHolder() {
    assertMove(
      listOf(listOf(51, 51, 51, 52, 52, 54, 54, 54, 55, 55, 57, 57, 57, 58, 58, 60)),
      "20+ Atk Kingambit Knock Off (97.5 BP) vs. 20 HP  / 20+ Def Toxapex",
      kingambit(), toxapex("Klutz", item = "Leftovers"), move("Knock Off"),
    )
  }

  @Test
  fun acrobaticsWithKlutzStillHoldsAnItem() {
    val talonflame = pokemon("Talonflame", "Klutz", Nature.JOLLY, 20, 20, 6, 0, 6, 14, item = "Life Orb")
    assertMove(
      listOf(listOf(18, 18, 19, 19, 19, 19, 19, 19, 19, 21, 21, 21, 21, 21, 21, 22)),
      "20 Atk Talonflame Acrobatics vs. 20 HP  / 20+ Def Toxapex",
      talonflame, toxapex(), move("Acrobatics"),
    )
  }

  @Test
  fun klutzCountsAsHoldingAnItemForAcrobatics() {
    val talonflame = pokemon("Talonflame", "Klutz", Nature.JOLLY, 20, 20, 6, 0, 6, 14)
    assertMove(
      listOf(listOf(18, 18, 19, 19, 19, 19, 19, 19, 19, 21, 21, 21, 21, 21, 21, 22)),
      "20 Atk Talonflame Acrobatics vs. 20 HP  / 20+ Def Toxapex",
      talonflame, toxapex(), move("Acrobatics"),
    )
  }

  @Test
  fun klutzCountsAsHoldingAnItemForKnockOff() {
    assertMove(
      listOf(listOf(51, 51, 51, 52, 52, 54, 54, 54, 55, 55, 57, 57, 57, 58, 58, 60)),
      "20+ Atk Kingambit Knock Off (97.5 BP) vs. 20 HP  / 20+ Def Toxapex",
      kingambit(), toxapex("Klutz"), move("Knock Off"),
    )
  }

  // --- type effectiveness and weight ---

  @Test
  fun levitateWithIronBall() {
    val rotom = pokemon("Rotom-Wash", "Levitate", Nature.BOLD, 20, 0, 20, 0, 16, 6, item = "Iron Ball")
    assertMove(
      listOf(listOf(102, 102, 102, 104, 104, 108, 108, 108, 110, 110, 114, 114, 114, 116, 116, 120)),
      "20+ Atk Garchomp Earthquake vs. 20 HP  / 20+ Def Rotom-Wash",
      garchomp(), rotom, move("Earthquake"),
    )
  }

  @Test
  fun ironBallFlyingTypeTakesNeutralGroundDamage() {
    val charizard = pokemon("Charizard", "Blaze", Nature.TIMID, 20, 0, 6, 20, 8, 12, item = "Iron Ball")
    assertMove(
      listOf(listOf(76, 78, 79, 79, 81, 81, 82, 84, 84, 85, 85, 87, 88, 88, 90, 91)),
      "20+ Atk Garchomp Earthquake vs. 20 HP  / 6 Def Iron Ball Charizard",
      garchomp(), charizard, move("Earthquake"),
    )
  }

  @Test
  fun heavySlamWithHeavyMetal() {
    val metagross = pokemon("Metagross", "Heavy Metal", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    assertMove(
      listOf(listOf(106, 108, 109, 109, 111, 112, 114, 115, 117, 117, 118, 120, 121, 123, 124, 126)),
      "20+ Atk Heavy Metal Metagross Heavy Slam (120 BP) vs. 20 HP  / 8 Def Garchomp",
      metagross, garchomp(), move("Heavy Slam"),
    )
  }

  @Test
  fun lowKickAgainstLightMetal() {
    val metagross = pokemon("Metagross", "Light Metal", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    assertMove(
      listOf(listOf(54, 55, 55, 56, 56, 57, 58, 58, 59, 60, 60, 61, 62, 62, 63, 64)),
      "20+ Atk Garchomp Low Kick (120 BP) vs. 20 HP  / 8 Def Light Metal Metagross",
      garchomp(), metagross, move("Low Kick"),
    )
  }

  // --- the source's setup pass ---

  @Test
  fun psychicSeedInPsychicTerrain() {
    assertMove(
      listOf(listOf(16, 16, 16, 16, 17, 17, 17, 17, 18, 18, 18, 18, 18, 18, 18, 19)),
      "20+ SpA Charizard Flamethrower vs. +1 20 HP  / 16 SpD Toxapex",
      specialAttacker("Charizard", "Blaze"), toxapex(item = "Psychic Seed"), move("Flamethrower"), Battlefield(terrain = Terrain.PSYCHIC),
    )
  }

  @Test
  fun electricSeedInElectricTerrain() {
    assertMove(
      listOf(listOf(50, 54, 54, 54, 54, 54, 56, 56, 56, 56, 56, 60, 60, 60, 60, 62)),
      "20+ Atk Garchomp Earthquake vs. +1 20 HP  / 20+ Def Toxapex",
      garchomp(), toxapex(item = "Electric Seed"), move("Earthquake"), Battlefield(terrain = Terrain.ELECTRIC),
    )
  }

  @Test
  fun intimidateLowersAttack() {
    assertMove(
      listOf(listOf(96, 96, 98, 98, 98, 102, 102, 102, 104, 104, 108, 108, 108, 110, 110, 114)),
      "-1 20+ Atk Garchomp Earthquake vs. 32 HP  / 0 Def Incineroar",
      garchomp(), incineroar(), move("Earthquake"),
    )
  }

  @Test
  fun intimidateTriggersDefiant() {
    assertMove(
      listOf(listOf(63, 63, 64, 65, 66, 66, 67, 68, 69, 69, 70, 71, 72, 72, 73, 74)),
      "+1 20+ Atk Kingambit Kowtow Cleave vs. 32 HP  / 0 Def Incineroar",
      kingambit(), incineroar(), move("Kowtow Cleave"),
    )
  }

  @Test
  fun intimidateIsBlockedByClearBody() {
    val metagross = pokemon("Metagross", "Clear Body", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    assertMove(
      listOf(listOf(39, 40, 40, 41, 42, 42, 42, 42, 43, 44, 44, 45, 45, 45, 46, 47)),
      "20+ Atk Metagross Iron Head vs. 32 HP  / 0 Def Incineroar",
      metagross, incineroar(), move("Iron Head"),
    )
  }

  @Test
  fun mirrorArmorReflectsIntimidate() {
    val corviknight = pokemon("Corviknight", "Mirror Armor", Nature.IMPISH, 20, 0, 20, 0, 16, 6)
    assertMove(
      listOf(listOf(18, 18, 19, 19, 19, 19, 20, 20, 20, 20, 20, 21, 21, 21, 21, 22)),
      "-1 Corviknight Foul Play vs. 32 HP  / 20+ Atk / 0 Def Incineroar",
      corviknight, incineroar(Nature.ADAMANT), move("Foul Play"),
    )
  }

  @Test
  fun attackersIntimidateLowersFoulPlay() {
    assertMove(
      listOf(listOf(55, 55, 57, 57, 58, 58, 60, 60, 60, 61, 61, 63, 63, 64, 64, 66)),
      "-1 Incineroar Foul Play vs. 20 HP  / 20+ Atk / 8 Def Garchomp",
      incineroar(), garchomp(), move("Foul Play"),
    )
  }

  @Test
  fun intimidateWithContrary() {
    assertMove(
      listOf(listOf(216, 218, 218, 222, 224, 228, 230, 234, 236, 236, 240, 242, 246, 248, 252, 254)),
      "+1 20+ Atk Garchomp Earthquake vs. 32 HP  / 0 Def Incineroar",
      garchomp("Contrary"), incineroar(), move("Earthquake"),
    )
  }

  @Test
  fun intimidateWithSimple() {
    assertMove(
      listOf(listOf(72, 72, 74, 74, 74, 78, 78, 78, 78, 80, 80, 80, 84, 84, 84, 86)),
      "-2 20+ Atk Garchomp Earthquake vs. 32 HP  / 0 Def Incineroar",
      garchomp("Simple"), incineroar(), move("Earthquake"),
    )
  }

  @Test
  fun traceCopiesPixilate() {
    val gardevoir = pokemon("Gardevoir", "Trace", Nature.MODEST, 20, 0, 6, 20, 8, 12, abilityIsActive = true)
    val sylveon = pokemon("Sylveon", "Pixilate", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertMove(
      listOf(listOf(51, 51, 51, 52, 52, 54, 54, 54, 55, 55, 57, 57, 57, 58, 58, 60)),
      "20+ SpA Pixilate Gardevoir Hyper Voice vs. 20 HP  / 16 SpD Sylveon",
      gardevoir, sylveon, move("Hyper Voice"),
    )
  }

  @Test
  fun cloudNineSuppressesTheSun() {
    val venusaur = pokemon("Venusaur", "Cloud Nine", Nature.BOLD, 20, 0, 20, 6, 10, 10)
    assertMove(
      listOf(listOf(128, 128, 132, 132, 134, 134, 138, 138, 140, 140, 144, 144, 146, 146, 150, 152)),
      "20+ SpA Charizard Flamethrower vs. 20 HP  / 10 SpD Venusaur",
      specialAttacker("Charizard", "Blaze"), venusaur, move("Flamethrower"), Battlefield(weather = Weather.SUN),
    )
  }

  @Test
  fun forecastCastformIsWaterTypeInRain() {
    val castform = pokemon("Castform", "Forecast", Nature.BOLD, 20, 0, 20, 0, 16, 6)
    assertMove(
      listOf(listOf(126, 128, 128, 132, 132, 134, 134, 138, 138, 140, 140, 144, 144, 146, 146, 150)),
      "20 SpA Raichu Thunderbolt vs. 20 HP  / 16 SpD Castform",
      specialAttacker("Raichu", "Lightning Rod", Nature.TIMID), castform, move("Thunderbolt"), Battlefield(weather = Weather.RAIN),
    )
  }

  @Test
  fun mimicryInElectricTerrain() {
    val skarmory = pokemon("Skarmory", "Mimicry", Nature.IMPISH, 20, 0, 20, 0, 16, 6)
    assertMove(
      listOf(listOf(80, 80, 80, 84, 84, 84, 86, 86, 86, 90, 90, 90, 92, 92, 92, 96)),
      "20+ Atk Garchomp Earthquake vs. 20 HP  / 20+ Def Skarmory",
      garchomp(), skarmory, move("Earthquake"), Battlefield(terrain = Terrain.ELECTRIC),
    )
  }

  @Test
  fun supersweetSyrupAndCompetitive() {
    val toxapex = pokemon("Toxapex", "Supersweet Syrup", Nature.BOLD, 20, 0, 20, 0, 16, 6, abilityIsActive = true)
    val garchomp = pokemon("Garchomp", "Competitive", Nature.ADAMANT, 20, 20, 8, 0, 8, 10, boosts = StatBoosts(specialAttack = 1))
    assertMove(
      listOf(listOf(136, 137, 139, 140, 142, 144, 145, 147, 148, 150, 152, 153, 155, 156, 158, 160)),
      "+3 Toxapex Foul Play vs. 20 HP  / 20+ Atk / 8 Def Garchomp",
      toxapex, garchomp, move("Foul Play"),
    )
  }

  @Test
  fun infiltratorIgnoresReflect() {
    val dragapult = pokemon("Dragapult", "Infiltrator", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    assertMove(
      listOf(listOf(39, 39, 39, 40, 40, 40, 42, 42, 42, 43, 43, 43, 45, 45, 45, 46)),
      "20+ Atk Dragapult Dragon Claw vs. 20 HP  / 20+ Def Toxapex",
      dragapult, toxapex(), move("Dragon Claw"), Battlefield(defenderSide = SideConditions(hasReflect = true)),
    )
  }

  // --- the hits of a multi-hit move ---

  @Test
  fun resistBerryIsConsumedByTheFirstHit() {
    val rhyperior = pokemon("Rhyperior", "Solid Rock", Nature.IMPISH, 20, 10, 20, 0, 6, 10, item = "Rindo Berry")
    assertMove(
      listOf(
        listOf(16, 16, 16, 16, 16, 16, 16, 16, 18, 18, 18, 18, 18, 18, 18, 19),
        listOf(33, 33, 33, 33, 33, 33, 33, 33, 36, 36, 36, 36, 36, 36, 36, 39),
      ),
      "20+ Atk Garchomp Bullet Seed (3 hits) vs. 20 HP  / 20+ Def Rindo Berry Solid Rock Rhyperior",
      garchomp(), rhyperior, move("Bullet Seed"), hits = 3,
    )
  }

  @Test
  fun theFirstHitBreaksMultiscale() {
    val dragonite = pokemon("Dragonite", "Multiscale", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    assertMove(
      listOf(
        listOf(22, 22, 22, 22, 24, 24, 24, 24, 24, 24, 25, 25, 25, 25, 25, 27),
        listOf(44, 44, 44, 44, 48, 48, 48, 48, 48, 48, 50, 50, 50, 50, 50, 54),
      ),
      "20+ Atk Garchomp Scale Shot (3 hits) vs. 20 HP  / 8 Def Multiscale Dragonite",
      garchomp(), dragonite, move("Scale Shot"), hits = 3,
    )
  }

  @Test
  fun weakArmorLowersDefenseEveryHit() {
    val skarmory = pokemon("Skarmory", "Weak Armor", Nature.IMPISH, 20, 0, 20, 0, 16, 6)
    assertMove(
      listOf(
        listOf(10, 10, 10, 10, 10, 10, 10, 11, 11, 11, 11, 11, 11, 11, 11, 12),
        listOf(14, 14, 14, 14, 15, 15, 15, 15, 15, 15, 16, 16, 16, 16, 16, 17),
        listOf(18, 18, 19, 19, 19, 19, 20, 20, 20, 20, 20, 21, 21, 21, 21, 22),
        listOf(23, 24, 24, 24, 24, 25, 25, 25, 26, 26, 26, 26, 27, 27, 27, 28),
      ),
      "20+ Atk Garchomp Rock Blast (4 hits) vs. 20 HP  / 20+ Def Weak Armor Skarmory",
      garchomp(), skarmory, move("Rock Blast"), hits = 4,
    )
  }

  @Test
  fun staminaRaisesDefenseEveryHit() {
    assertMove(
      listOf(
        listOf(13, 13, 13, 13, 13, 13, 15, 15, 15, 15, 15, 15, 15, 15, 15, 16),
        listOf(9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 12),
        listOf(7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 9),
        listOf(6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7),
        listOf(6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7),
      ),
      "20+ Atk Garchomp Scale Shot (5 hits) vs. 20 HP  / 20+ Def Stamina Toxapex",
      garchomp(), toxapex("Stamina"), move("Scale Shot"), hits = 5,
    )
  }

  @Test
  fun gooeyTriggersDefiant() {
    assertMove(
      listOf(
        listOf(13, 13, 13, 14, 14, 14, 14, 14, 14, 15, 15, 15, 15, 15, 15, 16),
        listOf(25, 25, 26, 26, 26, 27, 27, 27, 27, 28, 28, 28, 29, 29, 29, 30),
      ),
      "20+ Atk Defiant Kingambit Double Hit (2 hits) vs. 20 HP  / 20+ Def Gooey Toxapex",
      kingambit(), toxapex("Gooey"), move("Double Hit"), hits = 2,
    )
  }

  @Test
  fun spicySprayBurnsTheAttacker() {
    assertMove(
      listOf(
        listOf(13, 13, 13, 13, 13, 13, 15, 15, 15, 15, 15, 15, 15, 15, 15, 16),
        listOf(6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8),
      ),
      "20+ Atk Garchomp Scale Shot (3 hits) vs. 20 HP  / 20+ Def Spicy Spray Toxapex",
      garchomp(), toxapex("Spicy Spray"), move("Scale Shot"), hits = 3,
    )
  }

  @Test
  fun rawstBerryCuresTheFirstSpicySprayBurn() {
    assertMove(
      listOf(
        listOf(13, 13, 13, 13, 13, 13, 15, 15, 15, 15, 15, 15, 15, 15, 15, 16),
        listOf(13, 13, 13, 13, 13, 13, 15, 15, 15, 15, 15, 15, 15, 15, 15, 16),
        listOf(6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8),
      ),
      "20+ Atk Rawst Berry Garchomp Scale Shot (4 hits) vs. 20 HP  / 20+ Def Spicy Spray Toxapex",
      garchomp(item = "Rawst Berry"), toxapex("Spicy Spray"), move("Scale Shot"), hits = 4,
    )
  }

  @Test
  fun parentalBondSecondHitAfterSuperpowersDrop() {
    assertMove(
      listOf(
        listOf(20, 20, 20, 21, 21, 21, 21, 22, 22, 22, 22, 23, 23, 23, 23, 24),
        listOf(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4),
      ),
      "20+ Atk Parental Bond Garchomp Superpower (2 hits) vs. 20 HP  / 20+ Def Toxapex",
      garchomp("Parental Bond"), toxapex(), move("Superpower"),
    )
  }

  @Test
  fun parentalBondDoublesAssurancesSecondHit() {
    assertMove(
      listOf(
        listOf(21, 21, 21, 22, 22, 22, 22, 23, 23, 23, 23, 24, 24, 24, 24, 25),
        listOf(10, 10, 10, 10, 10, 10, 10, 11, 11, 11, 11, 11, 11, 11, 11, 12),
      ),
      "20+ Atk Parental Bond Garchomp Assurance (2 hits) vs. 20 HP  / 20+ Def Toxapex",
      garchomp("Parental Bond"), toxapex(), move("Assurance"),
    )
  }

  @Test
  fun parentalBondConsumesTheResistBerry() {
    assertMove(
      listOf(
        listOf(51, 51, 51, 52, 52, 54, 54, 54, 55, 55, 57, 57, 57, 58, 58, 60),
        listOf(24, 24, 24, 24, 24, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 30),
      ),
      "20+ Atk Parental Bond Garchomp Earthquake (2 hits) vs. 20 HP  / 20+ Def Shuca Berry Toxapex",
      garchomp("Parental Bond"), toxapex(item = "Shuca Berry"), move("Earthquake"), Battlefield(format = BattleFormat.SINGLES),
    )
  }

  // --- fixed-damage moves ---

  @Test
  fun seismicToss() {
    assertMove(listOf(listOf(50)), "Garchomp Seismic Toss vs. 20 HP  Toxapex", garchomp(), toxapex(), move("Seismic Toss"))
  }

  @Test
  fun parentalBondDoublesNightShadeInOneHit() {
    assertMove(
      listOf(listOf(100)), "Gengar Night Shade vs. 20 HP  Toxapex",
      specialAttacker("Gengar", "Parental Bond", Nature.TIMID), toxapex(), move("Night Shade"),
    )
  }

  @Test
  fun parentalBondSuperFang() {
    assertMove(listOf(listOf(108)), "Garchomp Super Fang vs. 20 HP  Toxapex", garchomp("Parental Bond"), toxapex(), move("Super Fang"))
  }

  @Test
  fun endeavor() {
    assertMove(listOf(listOf(104)), "Garchomp Endeavor vs. 20 HP  Toxapex", garchomp(hpFraction = 0.2), toxapex(), move("Endeavor"))
  }

  @Test
  fun finalGambit() {
    assertMove(
      listOf(listOf(93)), "Gengar Final Gambit vs. 20 HP  Toxapex",
      specialAttacker("Gengar", "Cursed Body", Nature.TIMID, hpFraction = 0.6), toxapex(), move("Final Gambit"),
    )
  }

  @Test
  fun fissureDealsTheDefendersHp() {
    assertMove(listOf(listOf(73)), "Garchomp Fissure vs. 20 HP  Toxapex", garchomp(), toxapex(hpFraction = 0.5), move("Fissure"))
  }

  @Test
  fun sheerColdFailsAgainstIceTypes() {
    val glalie = pokemon("Glalie", "Inner Focus", Nature.IMPISH, 20, 10, 20, 0, 6, 10)
    assertMove(listOf(listOf(0)), "Garchomp Sheer Cold vs. 20 HP  Glalie", garchomp(), glalie, move("Sheer Cold"))
  }

  @Test
  fun counterReturnsAPhysicalMove() {
    assertMove(
      listOf(listOf(84, 84, 84, 86, 86, 86, 90, 90, 90, 92, 92, 92, 96, 96, 96, 98)),
      "2x Counter (20+ Atk Garchomp Dragon Claw vs. 20 HP  / 20+ Def Toxapex) vs. 20 HP  Garchomp",
      toxapex(), garchomp(), move("Counter", countered = "Dragon Claw"),
    )
  }

  @Test
  fun mirrorCoatDoesntReturnAPhysicalMove() {
    assertMove(listOf(listOf(0)), "Toxapex Mirror Coat vs. 20 HP  Garchomp", toxapex(), garchomp(), move("Mirror Coat", countered = "Dragon Claw"))
  }

  @Test
  fun metalBurstReturnsAnyMove() {
    val metagross = pokemon("Metagross", "Clear Body", Nature.ADAMANT, 20, 20, 8, 0, 8, 10)
    assertMove(
      listOf(listOf(210, 216, 216, 219, 219, 225, 225, 228, 234, 234, 237, 237, 243, 243, 246, 252)),
      "1.5x Metal Burst (20+ SpA Charizard Flamethrower vs. 20 HP  / 8 SpD Metagross) vs. 20 HP  Charizard",
      metagross, specialAttacker("Charizard", "Blaze"), move("Metal Burst", countered = "Flamethrower"),
    )
  }

  @Test
  fun counterWithoutADamagingMove() {
    assertMove(listOf(listOf(0)), "Toxapex Counter vs. 20 HP  Garchomp", toxapex(), garchomp(), move("Counter"))
  }

  @Test
  fun painSplit() {
    assertMove(
      listOf(listOf(49)), "Gengar Pain Split vs. Toxapex",
      specialAttacker("Gengar", "Cursed Body", Nature.TIMID, hpFraction = 0.3), toxapex(), move("Pain Split"),
    )
  }
}
