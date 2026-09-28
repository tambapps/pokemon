package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.AbilityName
import com.tambapps.pokemon.ItemName
import com.tambapps.pokemon.MoveName
import com.tambapps.pokemon.Nature
import com.tambapps.pokemon.PokeStats
import com.tambapps.pokemon.PokemonName
import com.tambapps.pokemon.champions.data.ChampionsDex
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The KO chance of the "ko-" scenarios of tools/scenarios.json, which cover every hazard, end-of-turn effect and
 * restoring berry case: every expected damage, description and KO chance text here is the output of tools/oracle.js
 * for the scenario of the same name, i.e. of the REAL NCP-VGC-Damage-Calculator's getKOChanceText.
 */
class KoChanceCrossValidationTest {

  private fun pokemon(
    species: String,
    ability: String,
    nature: Nature,
    hp: Int, attack: Int, defense: Int, specialAttack: Int, specialDefense: Int, speed: Int,
    item: String? = null,
    status: Status = Status.HEALTHY,
    currentHp: Int? = null,
    toxicCounter: Int = 1,
  ) = BattlePokemon(
    species = ChampionsDex.species(PokemonName(species)),
    ability = AbilityName(ability),
    nature = nature,
    statPoints = PokeStats(hp = hp, attack = attack, defense = defense, specialAttack = specialAttack, specialDefense = specialDefense, speed = speed),
    item = item?.let(::ItemName),
    status = status,
    currentHp = currentHp,
    toxicCounter = toxicCounter,
  )

  // the spreads every scenario shares
  private fun attacker(species: String = "Garchomp", ability: String = "Rough Skin", item: String? = null) =
    pokemon(species, ability, Nature.ADAMANT, 20, 20, 8, 0, 8, 10, item = item)

  private fun defender(
    species: String = "Toxapex",
    ability: String = "Merciless",
    nature: Nature = Nature.BOLD,
    item: String? = null,
    status: Status = Status.HEALTHY,
    currentHp: Int? = null,
    toxicCounter: Int = 1,
  ) = pokemon(species, ability, nature, 20, 0, 20, 0, 16, 6, item, status, currentHp, toxicCounter)

  private fun defenderSide(conditions: SideConditions) = Battlefield(defenderSide = conditions)

  /**
   * [expectedRolls] is the oracle's damage: a single roll list when every hit deals the same, else one per hit.
   * [hits] is the scenario's hit count for a multi-hit move.
   */
  private fun assertKoChance(
    expectedRolls: List<List<Int>>,
    expectedDescription: String,
    expectedKoChance: String,
    move: String,
    attacker: BattlePokemon = attacker(),
    defender: BattlePokemon = defender(),
    field: Battlefield = Battlefield(),
    hits: Int? = null,
  ) {
    val moveUse = MoveUse(ChampionsDex.move(MoveName(move)))
    val result = if (hits == null) {
      DamageCalculator.calculateMove(attacker, defender, moveUse, field)
    } else {
      DamageCalculator.calculateMove(attacker, defender, moveUse, field, hits = hits)
    }
    assertEquals(expectedRolls, result.hits.map { it.rolls }.distinct())
    assertEquals(expectedDescription, result.description)
    assertEquals(expectedKoChance, result.koChance.text)
  }

  // Garchomp's Dragon Claw, Earthquake and Psychic Noise against Toxapex, which most scenarios use
  private val dragonClawRolls = listOf(listOf(42, 42, 42, 43, 43, 43, 45, 45, 45, 46, 46, 46, 48, 48, 48, 49))
  private val dragonClaw = "20+ Atk Garchomp Dragon Claw vs. 20 HP  / 20+ Def Toxapex"
  private val earthquakeRolls = listOf(listOf(74, 74, 78, 78, 78, 80, 80, 80, 80, 84, 84, 84, 86, 86, 86, 90))
  private val earthquake = "20+ Atk Garchomp Earthquake vs. 20 HP  / 20+ Def Toxapex"
  private val psychicNoiseRolls = listOf(listOf(30, 30, 30, 30, 32, 32, 32, 32, 32, 32, 34, 34, 34, 34, 34, 36))
  private val psychicNoise = "0- SpA Garchomp Psychic Noise vs. 20 HP  / 16 SpD Toxapex"
  private val bulletPunchRolls = listOf(listOf(7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8))
  private val bulletPunch = "20+ Atk Garchomp Bullet Punch vs. 20 HP  / 20+ Def Toxapex"
  private val corviknightDragonClawRolls = listOf(listOf(27, 27, 27, 27, 28, 28, 29, 29, 29, 30, 30, 30, 30, 31, 31, 32))
  private val corviknightDragonClaw = "20+ Atk Garchomp Dragon Claw vs. 20 HP  / 20+ Def Corviknight"
  private fun corviknight() = defender("Corviknight", "Pressure", Nature.IMPISH)

  // Toxapex has 145 HP: the oracle's curHpFraction 0.6, 0.7 and 0.45 are Math.round(145 * fraction)
  private val sixtyPercentHp = 87
  private val seventyPercentHp = 102
  private val fortyFivePercentHp = 65

  @Test
  fun stealthRock() = assertKoChance(
    dragonClawRolls, dragonClaw, "99.34% chance to 3HKO after Stealth Rock",
    "Dragon Claw", field = defenderSide(SideConditions(hasStealthRock = true)),
  )

  @Test
  fun spikesOneLayer() = assertKoChance(
    dragonClawRolls, dragonClaw, "99.34% chance to 3HKO after 1 layer of Spikes",
    "Dragon Claw", field = defenderSide(SideConditions(spikesLayers = 1)),
  )

  @Test
  fun spikesTwoLayers() = assertKoChance(
    dragonClawRolls, dragonClaw, "guaranteed 3HKO after 2 layers of Spikes",
    "Dragon Claw", field = defenderSide(SideConditions(spikesLayers = 2)),
  )

  @Test
  fun spikesThreeLayers() = assertKoChance(
    dragonClawRolls, dragonClaw, "guaranteed 3HKO after 3 layers of Spikes",
    "Dragon Claw", field = defenderSide(SideConditions(spikesLayers = 3)),
  )

  @Test
  fun stealthRockAndSpikes() = assertKoChance(
    dragonClawRolls, dragonClaw, "guaranteed 3HKO after Stealth Rock and 1 layer of Spikes",
    "Dragon Claw", field = defenderSide(SideConditions(hasStealthRock = true, spikesLayers = 1)),
  )

  @Test
  fun stealthRockAgainstAFourTimesWeakType() = assertKoChance(
    listOf(listOf(76, 76, 78, 78, 79, 81, 81, 82, 82, 84, 85, 85, 87, 87, 88, 90)),
    "20+ Atk Garchomp Earthquake vs. 20 HP  / 20 Def Volcarona",
    "6.25% chance to OHKO after Stealth Rock",
    "Earthquake", defender = defender("Volcarona", "Flame Body", Nature.TIMID), field = defenderSide(SideConditions(hasStealthRock = true)),
  )

  @Test
  fun stealthRockMakesAnOhko() = assertKoChance(
    earthquakeRolls, earthquake, "guaranteed OHKO after Stealth Rock",
    "Earthquake", defender = defender(currentHp = sixtyPercentHp), field = defenderSide(SideConditions(hasStealthRock = true)),
  )

  @Test
  fun stealthRockGivesAnOhkoChance() = assertKoChance(
    earthquakeRolls, earthquake, "43.75% chance to OHKO after Stealth Rock",
    "Earthquake", defender = defender(currentHp = seventyPercentHp), field = defenderSide(SideConditions(hasStealthRock = true)),
  )

  @Test
  fun spikesDoNotHitAFlyingType() = assertKoChance(
    corviknightDragonClawRolls, corviknightDragonClaw, "possible 6HKO after Stealth Rock",
    "Dragon Claw", defender = corviknight(), field = defenderSide(SideConditions(hasStealthRock = true, spikesLayers = 2)),
  )

  @Test
  fun ingrainGroundsAFlyingTypeForSpikes() = assertKoChance(
    corviknightDragonClawRolls, corviknightDragonClaw, "possible 8HKO after 2 layers of Spikes and Ingrain recovery",
    "Dragon Claw", defender = corviknight(), field = defenderSide(SideConditions(spikesLayers = 2, isIngrained = true)),
  )

  @Test
  fun gravityGroundsAFlyingTypeForSpikes() = assertKoChance(
    corviknightDragonClawRolls, corviknightDragonClaw, "guaranteed 6HKO after 2 layers of Spikes",
    "Dragon Claw", defender = corviknight(), field = Battlefield(isGravity = true, defenderSide = SideConditions(spikesLayers = 2)),
  )

  @Test
  fun spikesDoNotHitAnAirBalloonHolder() = assertKoChance(
    dragonClawRolls, dragonClaw, "0.9% chance to 3HKO",
    "Dragon Claw", defender = defender(item = "Air Balloon"), field = defenderSide(SideConditions(spikesLayers = 1)),
  )

  @Test
  fun magicGuardIgnoresHazardsAndEndOfTurnDamage() = assertKoChance(
    listOf(listOf(64, 64, 66, 66, 67, 67, 69, 69, 70, 70, 72, 72, 73, 73, 75, 76)),
    "20+ Atk Garchomp Earthquake vs. 20 HP  / 20+ Def Clefable",
    "guaranteed 3HKO",
    "Earthquake",
    defender = defender("Clefable", "Magic Guard", status = Status.POISONED),
    field = Battlefield(
      weather = Weather.SAND,
      defenderSide = SideConditions(
        hasStealthRock = true, spikesLayers = 3, isLeechSeeded = true, isCursed = true, isSaltCured = true, isBound = true,
      ),
    ),
  )

  @Test
  fun leftovers() = assertKoChance(
    dragonClawRolls, dragonClaw, "97.65% chance to 4HKO after Leftovers recovery",
    "Dragon Claw", defender = defender(item = "Leftovers"),
  )

  @Test
  fun klutzSuppressesLeftovers() = assertKoChance(
    dragonClawRolls, dragonClaw, "0.9% chance to 3HKO",
    "Dragon Claw", defender = defender(ability = "Klutz", item = "Leftovers"),
  )

  @Test
  fun knockOffRemovesLeftovers() = assertKoChance(
    listOf(listOf(33, 33, 33, 34, 34, 35, 35, 35, 36, 36, 37, 37, 37, 38, 38, 39)),
    "20+ Atk Garchomp Knock Off (97.5 BP) vs. 20 HP  / 20+ Def Toxapex",
    "29.98% chance to 4HKO",
    "Knock Off", defender = defender(item = "Leftovers"),
  )

  @Test
  fun thiefFromAnItemlessAttackerStealsLeftovers() = assertKoChance(
    listOf(listOf(21, 21, 21, 22, 22, 22, 22, 23, 23, 23, 23, 24, 24, 24, 24, 25)),
    "20+ Atk Garchomp Thief vs. 20 HP  / 20+ Def Toxapex",
    "possible 6HKO",
    "Thief", defender = defender(item = "Leftovers"),
  )

  @Test
  fun thiefFromAnAttackerHoldingAnItemLeavesLeftovers() = assertKoChance(
    listOf(listOf(27, 27, 27, 29, 29, 29, 29, 30, 30, 30, 30, 31, 31, 31, 31, 32)),
    "20+ Atk Life Orb Garchomp Thief vs. 20 HP  / 20+ Def Toxapex",
    "possible 6HKO after Leftovers recovery",
    "Thief", attacker = attacker(item = "Life Orb"), defender = defender(item = "Leftovers"),
  )

  @Test
  fun psychicNoiseBlocksLeftovers() = assertKoChance(
    psychicNoiseRolls, psychicNoise, "guaranteed 5HKO",
    "Psychic Noise", defender = defender(item = "Leftovers"),
  )

  @Test
  fun leftoversCancellingSandstorm() = assertKoChance(
    dragonClawRolls, dragonClaw, "0.9% chance to 3HKO after sandstorm damage and Leftovers recovery",
    "Dragon Claw", defender = defender(item = "Leftovers"), field = Battlefield(weather = Weather.SAND),
  )

  @Test
  fun leechSeed() = assertKoChance(
    dragonClawRolls, dragonClaw, "guaranteed 3HKO after Leech Seed damage",
    "Dragon Claw", field = defenderSide(SideConditions(isLeechSeeded = true)),
  )

  @Test
  fun leechSeedFinishesTheOhko() = assertKoChance(
    earthquakeRolls, earthquake, "guaranteed OHKO after Leech Seed damage",
    "Earthquake", defender = defender(currentHp = sixtyPercentHp), field = defenderSide(SideConditions(isLeechSeeded = true)),
  )

  @Test
  fun saltCure() = assertKoChance(
    listOf(listOf(81, 82, 82, 84, 84, 85, 87, 87, 88, 90, 90, 91, 93, 93, 94, 96)),
    "20+ Atk Garchomp Dragon Claw vs. 20 HP  / 20 Def Snorlax",
    "guaranteed 3HKO after Salt Cure damage",
    "Dragon Claw", defender = defender("Snorlax", "Thick Fat", Nature.CAREFUL), field = defenderSide(SideConditions(isSaltCured = true)),
  )

  @Test
  fun saltCureAgainstAWaterType() = assertKoChance(
    dragonClawRolls, dragonClaw, "guaranteed 3HKO after extra Salt Cure damage",
    "Dragon Claw", field = defenderSide(SideConditions(isSaltCured = true)),
  )

  @Test
  fun curse() = assertKoChance(
    dragonClawRolls, dragonClaw, "guaranteed 2HKO after ghost Curse",
    "Dragon Claw", field = defenderSide(SideConditions(isCursed = true)),
  )

  @Test
  fun binding() = assertKoChance(
    dragonClawRolls, dragonClaw, "guaranteed 3HKO after binding damage",
    "Dragon Claw", field = defenderSide(SideConditions(isBound = true)),
  )

  @Test
  fun bindingBand() = assertKoChance(
    dragonClawRolls, dragonClaw, "2.73% chance to 2HKO after binding damage",
    "Dragon Claw", defender = defender(item = "Binding Band"), field = defenderSide(SideConditions(isBound = true)),
  )

  @Test
  fun klutzSuppressesBindingBand() = assertKoChance(
    dragonClawRolls, dragonClaw, "guaranteed 3HKO after binding damage",
    "Dragon Claw", defender = defender(ability = "Klutz", item = "Binding Band"), field = defenderSide(SideConditions(isBound = true)),
  )

  @Test
  fun aquaRing() = assertKoChance(
    dragonClawRolls, dragonClaw, "97.65% chance to 4HKO after Aqua Ring recovery",
    "Dragon Claw", field = defenderSide(SideConditions(hasAquaRing = true)),
  )

  @Test
  fun aquaRingWithBigRoot() = assertKoChance(
    dragonClawRolls, dragonClaw, "71.9% chance to 4HKO after Aqua Ring recovery",
    "Dragon Claw", defender = defender(item = "Big Root"), field = defenderSide(SideConditions(hasAquaRing = true)),
  )

  @Test
  fun ingrain() = assertKoChance(
    dragonClawRolls, dragonClaw, "97.65% chance to 4HKO after Ingrain recovery",
    "Dragon Claw", field = defenderSide(SideConditions(isIngrained = true)),
  )

  @Test
  fun ingrainWithBigRoot() = assertKoChance(
    dragonClawRolls, dragonClaw, "71.9% chance to 4HKO after Ingrain recovery",
    "Dragon Claw", defender = defender(item = "Big Root"), field = defenderSide(SideConditions(isIngrained = true)),
  )

  @Test
  fun grassyTerrain() = assertKoChance(
    dragonClawRolls, dragonClaw, "97.65% chance to 4HKO after Grassy Terrain recovery",
    "Dragon Claw", field = Battlefield(terrain = Terrain.GRASSY),
  )

  @Test
  fun grassyTerrainDoesNotHealAFlyingType() = assertKoChance(
    corviknightDragonClawRolls, corviknightDragonClaw, "possible 7HKO",
    "Dragon Claw", defender = corviknight(), field = Battlefield(terrain = Terrain.GRASSY),
  )

  @Test
  fun grassyTerrainDoesNotHealALevitatingPokemon() = assertKoChance(
    dragonClawRolls, dragonClaw, "0.9% chance to 3HKO",
    "Dragon Claw", defender = defender(ability = "Levitate"), field = Battlefield(terrain = Terrain.GRASSY),
  )

  @Test
  fun psychicNoiseBlocksHealing() = assertKoChance(
    psychicNoiseRolls, psychicNoise, "guaranteed 5HKO",
    "Psychic Noise", field = Battlefield(terrain = Terrain.GRASSY, defenderSide = SideConditions(hasAquaRing = true)),
  )

  @Test
  fun poison() = assertKoChance(
    dragonClawRolls, dragonClaw, "guaranteed 3HKO after poison damage",
    "Dragon Claw", defender = defender(status = Status.POISONED),
  )

  @Test
  fun toxic() = assertKoChance(
    dragonClawRolls, dragonClaw, "guaranteed 3HKO after toxic damage",
    "Dragon Claw", defender = defender(status = Status.BADLY_POISONED),
  )

  @Test
  fun toxicCounter() = assertKoChance(
    dragonClawRolls, dragonClaw, "guaranteed 2HKO after toxic damage",
    "Dragon Claw", defender = defender(status = Status.BADLY_POISONED, toxicCounter = 5),
  )

  @Test
  fun toxicAfterFourUses() = assertKoChance(
    bulletPunchRolls, bulletPunch, "guaranteed 5HKO after toxic damage",
    "Bullet Punch", defender = defender(status = Status.BADLY_POISONED),
  )

  @Test
  fun toxicCounterAtItsMaximum() = assertKoChance(
    bulletPunchRolls, bulletPunch, "guaranteed 2HKO after toxic damage",
    "Bullet Punch", defender = defender(status = Status.BADLY_POISONED, toxicCounter = 15),
  )

  @Test
  fun poisonHeal() = assertKoChance(
    dragonClawRolls, dragonClaw, "possible 5HKO after Poison Heal",
    "Dragon Claw", defender = defender(ability = "Poison Heal", status = Status.POISONED),
  )

  @Test
  fun poisonHealWhileBadlyPoisoned() = assertKoChance(
    dragonClawRolls, dragonClaw, "possible 5HKO after Poison Heal",
    "Dragon Claw", defender = defender(ability = "Poison Heal", status = Status.BADLY_POISONED, toxicCounter = 4),
  )

  @Test
  fun psychicNoiseTurnsPoisonHealIntoPoisonDamage() = assertKoChance(
    psychicNoiseRolls, psychicNoise, "98.44% chance to 3HKO after poison damage",
    "Psychic Noise", defender = defender(ability = "Poison Heal", status = Status.POISONED),
  )

  @Test
  fun burn() = assertKoChance(
    dragonClawRolls, dragonClaw, "guaranteed 3HKO after burn damage",
    "Dragon Claw", defender = defender(status = Status.BURNED),
  )

  @Test
  fun heatproofHalvesBurnDamage() = assertKoChance(
    dragonClawRolls, dragonClaw, "74.95% chance to 3HKO after reduced burn damage",
    "Dragon Claw", defender = defender(ability = "Heatproof", status = Status.BURNED),
  )

  @Test
  fun sandstorm() = assertKoChance(
    dragonClawRolls, dragonClaw, "guaranteed 3HKO after sandstorm damage",
    "Dragon Claw", field = Battlefield(weather = Weather.SAND),
  )

  @Test
  fun sandstormDoesNotHurtASteelType() = assertKoChance(
    listOf(listOf(38, 39, 39, 39, 40, 40, 41, 42, 42, 42, 42, 43, 44, 44, 45, 45)),
    "20+ Atk Garchomp Dragon Claw vs. 20 HP  / 20+ Def Excadrill",
    "possible 5HKO",
    "Dragon Claw", defender = defender("Excadrill", "Sand Rush"), field = Battlefield(weather = Weather.SAND),
  )

  @Test
  fun sandstormDoesNotHurtOvercoat() = assertKoChance(
    dragonClawRolls, dragonClaw, "0.9% chance to 3HKO",
    "Dragon Claw", defender = defender(ability = "Overcoat"), field = Battlefield(weather = Weather.SAND),
  )

  @Test
  fun hail() = assertKoChance(
    dragonClawRolls, dragonClaw, "guaranteed 3HKO after hail damage",
    "Dragon Claw", field = Battlefield(weather = Weather.HAIL),
  )

  @Test
  fun hailDoesNotHurtAnIceType() = assertKoChance(
    listOf(listOf(76, 76, 78, 78, 79, 81, 81, 82, 82, 84, 85, 85, 87, 87, 88, 90)),
    "20+ Atk Garchomp Earthquake vs. 20 HP  / 20 Def Weavile",
    "51.95% chance to 2HKO",
    "Earthquake", defender = defender("Weavile", "Pressure", Nature.JOLLY), field = Battlefield(weather = Weather.HAIL),
  )

  @Test
  fun iceBodyInHail() = assertKoChance(
    dragonClawRolls, dragonClaw, "97.65% chance to 4HKO after Ice Body recovery",
    "Dragon Claw", defender = defender(ability = "Ice Body"), field = Battlefield(weather = Weather.HAIL),
  )

  @Test
  fun psychicNoiseTurnsIceBodyIntoHailDamage() = assertKoChance(
    psychicNoiseRolls, psychicNoise, "guaranteed 4HKO after hail damage",
    "Psychic Noise", defender = defender(ability = "Ice Body"), field = Battlefield(weather = Weather.HAIL),
  )

  @Test
  fun iceBodyInSnow() = assertKoChance(
    dragonClawRolls, dragonClaw, "97.65% chance to 4HKO after Ice Body recovery",
    "Dragon Claw", defender = defender(ability = "Ice Body"), field = Battlefield(weather = Weather.SNOW),
  )

  @Test
  fun drySkinInRain() = assertKoChance(
    dragonClawRolls, dragonClaw, "possible 5HKO after Dry Skin recovery",
    "Dragon Claw", defender = defender(ability = "Dry Skin"), field = Battlefield(weather = Weather.RAIN),
  )

  @Test
  fun rainDish() = assertKoChance(
    dragonClawRolls, dragonClaw, "97.65% chance to 4HKO after Rain Dish recovery",
    "Dragon Claw", defender = defender(ability = "Rain Dish"), field = Battlefield(weather = Weather.RAIN),
  )

  @Test
  fun psychicNoiseBlocksRainDish() = assertKoChance(
    psychicNoiseRolls, psychicNoise, "guaranteed 5HKO",
    "Psychic Noise", defender = defender(ability = "Rain Dish"), field = Battlefield(weather = Weather.RAIN),
  )

  @Test
  fun drySkinInSun() = assertKoChance(
    dragonClawRolls, dragonClaw, "guaranteed 3HKO after Dry Skin damage",
    "Dragon Claw", defender = defender(ability = "Dry Skin"), field = Battlefield(weather = Weather.SUN),
  )

  @Test
  fun solarPowerInSun() = assertKoChance(
    dragonClawRolls, dragonClaw, "guaranteed 3HKO after Solar Power damage",
    "Dragon Claw", defender = defender(ability = "Solar Power"), field = Battlefield(weather = Weather.SUN),
  )

  @Test
  fun manyEffects() = assertKoChance(
    dragonClawRolls, dragonClaw,
    "guaranteed 3HKO after Stealth Rock, Grassy Terrain recovery, Leech Seed damage, burn damage, and Sitrus Berry recovery",
    "Dragon Claw",
    defender = defender(item = "Sitrus Berry", status = Status.BURNED),
    field = Battlefield(terrain = Terrain.GRASSY, defenderSide = SideConditions(hasStealthRock = true, isLeechSeeded = true)),
  )

  @Test
  fun manyEffectsWithAnOranBerry() = assertKoChance(
    dragonClawRolls, dragonClaw,
    "guaranteed 2HKO after 1 layer of Spikes, toxic damage, extra Salt Cure damage, binding damage, and Oran Berry recovery",
    "Dragon Claw",
    defender = defender(item = "Oran Berry", status = Status.BADLY_POISONED, toxicCounter = 2),
    field = defenderSide(SideConditions(spikesLayers = 1, isSaltCured = true, isBound = true)),
  )

  @Test
  fun sitrusBerry() = assertKoChance(
    dragonClawRolls, dragonClaw, "47.88% chance to 4HKO after Sitrus Berry recovery",
    "Dragon Claw", defender = defender(item = "Sitrus Berry"),
  )

  @Test
  fun sitrusBerryGuaranteedKo() = assertKoChance(
    earthquakeRolls, earthquake, "guaranteed 3HKO after Sitrus Berry recovery",
    "Earthquake", defender = defender(item = "Sitrus Berry"),
  )

  @Test
  fun oranBerry() = assertKoChance(
    dragonClawRolls, dragonClaw, "guaranteed 4HKO after Oran Berry recovery",
    "Dragon Claw", defender = defender(item = "Oran Berry"),
  )

  @Test
  fun ripenDoublesTheSitrusBerry() = assertKoChance(
    dragonClawRolls, dragonClaw, "possible 5HKO after Ripen Sitrus Berry recovery",
    "Dragon Claw", defender = defender(ability = "Ripen", item = "Sitrus Berry"),
  )

  @Test
  fun knockOffRemovesTheSitrusBerry() = assertKoChance(
    listOf(listOf(33, 33, 33, 34, 34, 35, 35, 35, 36, 36, 37, 37, 37, 38, 38, 39)),
    "20+ Atk Garchomp Knock Off (97.5 BP) vs. 20 HP  / 20+ Def Toxapex",
    "guaranteed 4HKO after Stealth Rock",
    "Knock Off", defender = defender(item = "Sitrus Berry"), field = defenderSide(SideConditions(hasStealthRock = true)),
  )

  @Test
  fun bugBiteEatsTheSitrusBerry() = assertKoChance(
    listOf(listOf(23, 23, 24, 24, 24, 24, 24, 25, 25, 25, 26, 26, 26, 27, 27, 27)),
    "20+ Atk Technician Scizor Bug Bite vs. 20 HP  / 20+ Def Toxapex",
    "possible 6HKO",
    "Bug Bite", attacker = attacker("Scizor", "Technician"), defender = defender(item = "Sitrus Berry"),
  )

  @Test
  fun pluckEatsTheSitrusBerry() = assertKoChance(
    listOf(listOf(31, 31, 31, 33, 33, 33, 33, 34, 34, 34, 34, 36, 36, 36, 36, 37)),
    "20+ Atk Dragonite Pluck vs. 20 HP  / 20+ Def Toxapex",
    "0.59% chance to 4HKO",
    "Pluck", attacker = attacker("Dragonite", "Inner Focus"), defender = defender(item = "Sitrus Berry"),
  )

  @Test
  fun klutzSuppressesTheSitrusBerry() = assertKoChance(
    dragonClawRolls, dragonClaw, "0.9% chance to 3HKO",
    "Dragon Claw", defender = defender(ability = "Klutz", item = "Sitrus Berry"),
  )

  @Test
  fun sitrusBerryBelowHalfHp() = assertKoChance(
    dragonClawRolls, dragonClaw, "guaranteed 3HKO after Sitrus Berry recovery",
    "Dragon Claw", defender = defender(item = "Sitrus Berry", currentHp = fortyFivePercentHp),
  )

  @Test
  fun sitrusBerryDoesNotChangeAnOhkoChance() = assertKoChance(
    earthquakeRolls, earthquake, "6.25% chance to OHKO",
    "Earthquake", defender = defender(item = "Sitrus Berry", currentHp = sixtyPercentHp),
  )

  @Test
  fun sitrusBerryWithLeechSeed() = assertKoChance(
    dragonClawRolls, dragonClaw, "99.34% chance to 3HKO after Leech Seed damage and Sitrus Berry recovery",
    "Dragon Claw", defender = defender(item = "Sitrus Berry"), field = defenderSide(SideConditions(isLeechSeeded = true)),
  )

  @Test
  fun sitrusBerryAgainstAMultiHitMove() = assertKoChance(
    listOf(listOf(9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 11)),
    "20+ Atk Garchomp Bullet Seed (3 hits) vs. 20 HP  / 20+ Def Toxapex",
    "possible 6HKO after Sitrus Berry recovery",
    "Bullet Seed", defender = defender(item = "Sitrus Berry"), hits = 3,
  )

  @Test
  fun sitrusBerryAgainstAFiveHitMove() = assertKoChance(
    listOf(listOf(9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 11)),
    "20+ Atk Garchomp Bullet Seed (5 hits) vs. 20 HP  / 20+ Def Toxapex",
    ">99.99% chance to 4HKO after Sitrus Berry recovery",
    "Bullet Seed", defender = defender(item = "Sitrus Berry"), hits = 5,
  )

  @Test
  fun sitrusBerryAgainstAMultiHitMoveInGrassyTerrain() = assertKoChance(
    listOf(listOf(11, 12, 12, 12, 12, 12, 12, 12, 13, 13, 13, 13, 13, 13, 13, 14)),
    "20+ Atk Garchomp Bullet Seed (3 hits) vs. 20 HP  / 20+ Def Toxapex in Grassy Terrain",
    "possible 6HKO after Grassy Terrain recovery and Sitrus Berry recovery",
    "Bullet Seed", defender = defender(item = "Sitrus Berry"), field = Battlefield(terrain = Terrain.GRASSY), hits = 3,
  )

  @Test
  fun sitrusBerryAgainstParentalBond() = assertKoChance(
    listOf(
      listOf(42, 42, 42, 43, 43, 43, 45, 45, 45, 46, 46, 46, 48, 48, 48, 49),
      listOf(9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 12),
    ),
    "20+ Atk Parental Bond Garchomp Dragon Claw (2 hits) vs. 20 HP  / 20+ Def Toxapex",
    "<0.01% chance to 3HKO after Sitrus Berry recovery",
    "Dragon Claw", attacker = attacker(ability = "Parental Bond"), defender = defender(item = "Sitrus Berry"),
  )

  @Test
  fun sitrusBerryAgainstTripleAxel() = assertKoChance(
    listOf(
      listOf(3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4),
      listOf(7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8),
      listOf(10, 10, 10, 11, 11, 11, 11, 11, 11, 11, 11, 12, 12, 12, 12, 12),
    ),
    "20+ Atk Garchomp Triple Axel (3 hits) vs. 20 HP  / 20+ Def Toxapex",
    "possible 8HKO after Sitrus Berry recovery",
    "Triple Axel", defender = defender(item = "Sitrus Berry"), hits = 3,
  )

  @Test
  fun painSplit() = assertKoChance(
    listOf(listOf(-29)), "Garchomp Pain Split vs. Toxapex", "The battlers shared their pain!",
    "Pain Split",
  )

  @Test
  fun ohkoMove() = assertKoChance(
    listOf(listOf(145)), "Garchomp Sheer Cold vs. 20 HP  Toxapex", "is it a one-hit KO?!",
    "Sheer Cold",
  )
}
