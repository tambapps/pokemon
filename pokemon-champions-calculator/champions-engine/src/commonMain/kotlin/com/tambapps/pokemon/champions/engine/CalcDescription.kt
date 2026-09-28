package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.Nature
import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.Stat
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Item

/**
 * How the stats invested in the attacking and defending stats are written in a calc description, the source
 * calculator's "Display results with" setting.
 */
enum class StatDisplay {
  /** Champions' stat points with the nature's effect, e.g. "20+ Atk". The source calculator's default. */
  STAT_POINTS,
  /** The stat points converted to EVs, e.g. "156+ Atk". */
  EVS,
  /** The stat's actual value, before boosts, e.g. "187 Atk". */
  STATS,
}

/** What a Pokemon invested in one stat, enough to write it in any [StatDisplay]. */
internal data class StatInvestment(val stat: Stat, val statPoints: Int, val value: Int, val nature: Nature) {

  fun text(display: StatDisplay): String {
    val amount = when (display) {
      StatDisplay.STAT_POINTS -> statPoints
      StatDisplay.EVS -> maxOf(0, statPoints * 8 - 4)
      StatDisplay.STATS -> value
    }
    if (stat == Stat.HP) return "$amount HP "
    val natureSign = when {
      display == StatDisplay.STATS -> ""
      nature.bonusStat == stat -> "+"
      nature.malusStat == stat -> "-"
      else -> ""
    }
    return "$amount$natureSign ${stat.smogonName}"
  }

  private val Stat.smogonName get() = when (this) {
    Stat.ATTACK -> "Atk"
    Stat.DEFENSE -> "Def"
    Stat.SPECIAL_ATTACK -> "SpA"
    Stat.SPECIAL_DEFENSE -> "SpD"
    Stat.SPEED -> "Spe"
    Stat.HP -> "HP"
  }

  companion object {
    fun of(pokemon: BattlePokemon, stat: Stat) = StatInvestment(stat, pokemon.statPoints[stat], pokemon.stats[stat], pokemon.nature)
  }
}

/**
 * The facts a calc description mentions, recorded by the modifier chain wherever the matching modifier applies,
 * like the `description` object damage_MASTER.js/damage_SV.js fill in. [build] is a port of their
 * `buildDescription`, trimmed to what exists in Champions (no levels, Tera, Dynamax, Ruin abilities...).
 */
internal class DescriptionBuilder(
  var attackerName: String = "",
  var moveName: String = "",
  var defenderName: String = "",
) {
  var attackBoost: Int = 0
  var attackStat: StatInvestment? = null
  var usesOppAtkStat: Boolean = false
  var attackerItem: String? = null
  var attackerAbility: String? = null
  var isBurned: Boolean = false
  var isHelpingHand: Boolean = false
  var isPowerSpot: Boolean = false
  var isBattery: Boolean = false
  var isSteelySpirit: Boolean = false
  var charged: Boolean = false
  var moveBP: Double? = null
  var moveType: String? = null
  var hits: Int? = null
  var defenseBoost: Int = 0
  var hp: StatInvestment? = null
  var defenseStat: StatInvestment? = null
  var defenderItem: String? = null
  var defenderAbility: String? = null
  var weather: String? = null
  var terrain: String? = null
  var isAuroraVeil: Boolean = false
  var isReflect: Boolean = false
  var isLightScreen: Boolean = false
  var isCritical: Boolean = false
  var isGravity: Boolean = false
  var isGlaiveMod: Boolean = false
  var isFriendGuard: Boolean = false
  var isQuarteredByProtect: Boolean = false

  fun attackerAbility(ability: Ability) {
    attackerAbility = ability.displayName
  }

  fun defenderAbility(ability: Ability) {
    defenderAbility = ability.displayName
  }

  fun attackerItem(item: Item?) {
    attackerItem = item?.displayName
  }

  fun defenderItem(item: Item?) {
    defenderItem = item?.displayName
  }

  fun weather(weather: Weather) {
    this.weather = weather.displayName
  }

  fun terrain(terrain: Terrain) {
    this.terrain = terrain.displayName
  }

  fun moveType(type: PokeType) {
    moveType = type.displayName
  }

  fun build(display: StatDisplay): String = buildString {
    appendBoost(attackBoost)
    if (!usesOppAtkStat) appendIfSet(attackStat?.text(display))
    appendIfSet(attackerItem)
    appendIfSet(attackerAbility)
    if (isBurned) append("burned ")
    append(attackerName).append(' ')
    if (isHelpingHand) append("Helping Hand ")
    if (isPowerSpot) append("Power Spot ")
    if (isBattery) append("Battery ")
    if (isSteelySpirit) append("Ally Steely Spirit ")
    if (charged) append("Charged ")
    append(moveName).append(' ')
    val bp = moveBP?.let(::formatNumber)
    when {
      bp != null && moveType != null -> append("($bp BP $moveType) ")
      bp != null -> append("($bp BP) ")
      moveType != null -> append("($moveType) ")
    }
    hits?.let { append("($it hits) ") }
    append("vs. ")
    appendBoost(defenseBoost)
    appendIfSet(hp?.text(display))
    if (usesOppAtkStat) attackStat?.let { append("/ ").append(it.text(display)).append(' ') }
    defenseStat?.let { append("/ ").append(it.text(display)).append(' ') }
    appendIfSet(defenderItem)
    appendIfSet(defenderAbility)
    append(defenderName)
    when {
      weather != null && terrain != null -> append(" in $weather and $terrain Terrain")
      weather != null -> append(" in $weather")
      terrain != null -> append(" in $terrain Terrain")
    }
    when {
      isAuroraVeil -> append(" through Aurora Veil")
      isReflect -> append(" through Reflect")
      isLightScreen -> append(" through Light Screen")
    }
    if (isCritical) append(" on a critical hit")
    if (isGravity) append(" under Gravity")
    if (isGlaiveMod) append(" after using Glaive Rush")
    if (isFriendGuard) append(" with Friend Guard")
    if (isQuarteredByProtect) append(" through Protect")
  }

  private fun StringBuilder.appendBoost(boost: Int) {
    if (boost != 0) append(if (boost > 0) "+$boost " else "$boost ")
  }

  // the source's appendIfSet: an empty string is falsy in JS
  private fun StringBuilder.appendIfSet(text: String?) {
    if (!text.isNullOrEmpty()) append(text).append(' ')
  }
}

// how JS prints a number: 140.0 -> "140", 97.5 -> "97.5"
private fun formatNumber(value: Double): String =
  if (value == kotlin.math.floor(value)) value.toLong().toString() else value.toString()

internal val PokeType.displayName: String get() = name.lowercase().replaceFirstChar { it.uppercase() }

internal val Weather.displayName: String get() = when (this) {
  Weather.NONE -> ""
  Weather.SUN -> "Sun"
  Weather.RAIN -> "Rain"
  Weather.SAND -> "Sand"
  Weather.HAIL -> "Hail"
  Weather.SNOW -> "Snow"
}

internal val Terrain.displayName: String get() = when (this) {
  Terrain.NONE -> ""
  Terrain.ELECTRIC -> "Electric"
  Terrain.GRASSY -> "Grassy"
  Terrain.MISTY -> "Misty"
  Terrain.PSYCHIC -> "Psychic"
}
