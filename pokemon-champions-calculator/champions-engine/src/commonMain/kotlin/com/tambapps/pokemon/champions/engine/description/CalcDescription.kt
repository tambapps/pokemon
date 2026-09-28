package com.tambapps.pokemon.champions.engine.description

import com.tambapps.pokemon.MoveName
import com.tambapps.pokemon.Nature
import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.PokemonName
import com.tambapps.pokemon.Stat
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Item
import com.tambapps.pokemon.champions.engine.BattlePokemon
import com.tambapps.pokemon.champions.engine.Terrain
import com.tambapps.pokemon.champions.engine.Weather

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

/** The screen a hit went through. */
enum class Screen(val displayName: String) {
  REFLECT("Reflect"),
  LIGHT_SCREEN("Light Screen"),
  AURORA_VEIL("Aurora Veil"),
}

/** How Rivalry changed a hit's power. */
enum class RivalryEffect(val multiplier: String) {
  SAME_GENDER("1.25x"),
  OPPOSITE_GENDER("0.75x"),
}

/** What a Pokemon invested in one stat, enough to write it in any [StatDisplay]. */
data class StatInvestment(val stat: Stat, val statPoints: Int, val value: Int, val nature: Nature) {

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
 * The facts the source calculator's description of a calc mentions, each one recorded by the modifier chain only
 * where the matching modifier applied, like the `description` object damage_MASTER.js/damage_SV.js fill in.
 * A null/false/0 fact isn't mentioned. [format] writes them exactly like the source's `buildDescription`, trimmed to
 * what exists in Champions (no levels, Tera, Dynamax, Ruin abilities...).
 *
 * @property attackStat the attack stat investment, the defender's for Foul Play ([usesDefenderAttackStat]).
 * Null when no damage was calculated (status move, immunity), like [hp] and [defenseStat]
 * @property attackerAbility written with [rivalryEffect] for Rivalry and [faintedAllies] for Supreme Overlord
 * @property moveBasePower the move's power when it changed, e.g. 97.5 for Knock Off against an item holder
 * @property moveType the move's type when it changed, e.g. Fire for Weather Ball in the sun
 * @property hits the number of hits of a multi-hit move (or Parental Bond's 2), only in a whole-move description
 */
data class CalcDescription(
  val attackerName: PokemonName,
  val moveName: MoveName,
  val defenderName: PokemonName,
  val attackBoost: Int = 0,
  val attackStat: StatInvestment? = null,
  val usesDefenderAttackStat: Boolean = false,
  val attackerItem: Item? = null,
  val attackerAbility: Ability? = null,
  val rivalryEffect: RivalryEffect? = null,
  val faintedAllies: Int = 0,
  val isBurned: Boolean = false,
  val isHelpingHand: Boolean = false,
  val isPowerSpot: Boolean = false,
  val isBattery: Boolean = false,
  val isAllySteelySpirit: Boolean = false,
  val isCharged: Boolean = false,
  val moveBasePower: Double? = null,
  val moveType: PokeType? = null,
  val hits: Int? = null,
  val defenseBoost: Int = 0,
  val hp: StatInvestment? = null,
  val defenseStat: StatInvestment? = null,
  val defenderItem: Item? = null,
  val defenderAbility: Ability? = null,
  val weather: Weather? = null,
  val terrain: Terrain? = null,
  val screen: Screen? = null,
  val isCritical: Boolean = false,
  val isGravity: Boolean = false,
  val isGlaiveRushVulnerable: Boolean = false,
  val isFriendGuard: Boolean = false,
  val isQuarteredByProtect: Boolean = false,
) {

  /** The text of the calc as the source calculator writes it, before its damage numbers. */
  fun format(display: StatDisplay = StatDisplay.STAT_POINTS): String = buildString {
    appendBoost(attackBoost)
    if (!usesDefenderAttackStat) appendIfSet(attackStat?.text(display))
    appendIfSet(attackerItem?.displayName)
    appendIfSet(attackerAbilityText())
    if (isBurned) append("burned ")
    append(attackerName.value).append(' ')
    if (isHelpingHand) append("Helping Hand ")
    if (isPowerSpot) append("Power Spot ")
    if (isBattery) append("Battery ")
    if (isAllySteelySpirit) append("Ally Steely Spirit ")
    if (isCharged) append("Charged ")
    append(moveName.value).append(' ')
    val bp = moveBasePower?.let(::formatNumber)
    val type = moveType?.displayName
    when {
      bp != null && type != null -> append("($bp BP $type) ")
      bp != null -> append("($bp BP) ")
      type != null -> append("($type) ")
    }
    hits?.let { append("($it hits) ") }
    append("vs. ")
    appendBoost(defenseBoost)
    appendIfSet(hp?.text(display))
    if (usesDefenderAttackStat) attackStat?.let { append("/ ").append(it.text(display)).append(' ') }
    defenseStat?.let { append("/ ").append(it.text(display)).append(' ') }
    appendIfSet(defenderItem?.displayName)
    appendIfSet(defenderAbility?.displayName)
    append(defenderName.value)
    val weatherName = weather?.displayName
    val terrainName = terrain?.displayName
    when {
      weatherName != null && terrainName != null -> append(" in $weatherName and $terrainName Terrain")
      weatherName != null -> append(" in $weatherName")
      terrainName != null -> append(" in $terrainName Terrain")
    }
    screen?.let { append(" through ${it.displayName}") }
    if (isCritical) append(" on a critical hit")
    if (isGravity) append(" under Gravity")
    if (isGlaiveRushVulnerable) append(" after using Glaive Rush")
    if (isFriendGuard) append(" with Friend Guard")
    if (isQuarteredByProtect) append(" through Protect")
  }

  private fun attackerAbilityText(): String? {
    val ability = attackerAbility ?: return null
    return when {
      ability == Ability.RIVALRY && rivalryEffect != null -> "${ability.displayName} (${rivalryEffect.multiplier})"
      ability == Ability.SUPREME_OVERLORD && faintedAllies > 0 ->
        "${ability.displayName} ($faintedAllies ${if (faintedAllies > 1) "allies" else "ally"} down)"
      else -> ability.displayName
    }
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

private val PokeType.displayName: String get() = name.lowercase().replaceFirstChar { it.uppercase() }

private val Weather.displayName: String? get() = when (this) {
  Weather.NONE -> null
  Weather.SUN -> "Sun"
  Weather.RAIN -> "Rain"
  Weather.SAND -> "Sand"
  Weather.HAIL -> "Hail"
  Weather.SNOW -> "Snow"
}

private val Terrain.displayName: String? get() = when (this) {
  Terrain.NONE -> null
  Terrain.ELECTRIC -> "Electric"
  Terrain.GRASSY -> "Grassy"
  Terrain.MISTY -> "Misty"
  Terrain.PSYCHIC -> "Psychic"
}
