package com.tambapps.pokemon.champions.engine.description

import com.tambapps.pokemon.MoveName
import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.PokemonName
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Item
import com.tambapps.pokemon.champions.engine.Terrain
import com.tambapps.pokemon.champions.engine.Weather

/**
 * Collects the [CalcFacts] of a calc while the modifier chain runs: each resolver records what it applied,
 * later records overwriting earlier ones like in the source calculator.
 */
internal class CalcFactsBuilder(
  private val attackerName: PokemonName = PokemonName(""),
  private val moveName: MoveName = MoveName(""),
  private val defenderName: PokemonName = PokemonName(""),
) {
  var attackBoost: Int = 0
  var attackStat: StatInvestment? = null
  var usesOppAtkStat: Boolean = false
  var attackerItem: Item? = null
  var attackerAbility: Ability? = null
  var rivalryEffect: RivalryEffect? = null
  var faintedAllies: Int = 0
  var isBurned: Boolean = false
  var isHelpingHand: Boolean = false
  var isPowerSpot: Boolean = false
  var isBattery: Boolean = false
  var isSteelySpirit: Boolean = false
  var charged: Boolean = false
  var moveBP: Double? = null
  var moveType: PokeType? = null
  var hits: Int? = null
  var defenseBoost: Int = 0
  var hp: StatInvestment? = null
  var defenseStat: StatInvestment? = null
  var defenderItem: Item? = null
  var defenderAbility: Ability? = null
  var weather: Weather? = null
  var terrain: Terrain? = null
  var screen: Screen? = null
  var isCritical: Boolean = false
  var isGravity: Boolean = false
  var isGlaiveMod: Boolean = false
  var isFriendGuard: Boolean = false
  var isQuarteredByProtect: Boolean = false
  var countered: CounteredMove? = null

  // not a fact: the defender's resist berry weakened this hit, so it's consumed for the next hits of the move
  var consumedResistBerry: Boolean = false

  fun attackerAbility(ability: Ability) {
    attackerAbility = ability
  }

  fun defenderAbility(ability: Ability) {
    defenderAbility = ability
  }

  fun attackerItem(item: Item?) {
    attackerItem = item
  }

  fun defenderItem(item: Item?) {
    defenderItem = item
  }

  fun weather(weather: Weather) {
    this.weather = weather
  }

  fun terrain(terrain: Terrain) {
    this.terrain = terrain
  }

  fun moveType(type: PokeType) {
    moveType = type
  }

  fun build() = CalcFacts(
    attackerName = attackerName,
    moveName = moveName,
    defenderName = defenderName,
    attackBoost = attackBoost,
    attackStat = attackStat,
    usesDefenderAttackStat = usesOppAtkStat,
    attackerItem = attackerItem,
    attackerAbility = attackerAbility,
    rivalryEffect = rivalryEffect,
    faintedAllies = faintedAllies,
    isBurned = isBurned,
    isHelpingHand = isHelpingHand,
    isPowerSpot = isPowerSpot,
    isBattery = isBattery,
    isAllySteelySpirit = isSteelySpirit,
    isCharged = charged,
    moveBasePower = moveBP,
    moveType = moveType,
    hits = hits,
    defenseBoost = defenseBoost,
    hp = hp,
    defenseStat = defenseStat,
    defenderItem = defenderItem,
    defenderAbility = defenderAbility,
    weather = weather,
    terrain = terrain,
    screen = screen,
    isCritical = isCritical,
    isGravity = isGravity,
    isGlaiveRushVulnerable = isGlaiveMod,
    isFriendGuard = isFriendGuard,
    isQuarteredByProtect = isQuarteredByProtect,
    countered = countered,
  )
}
