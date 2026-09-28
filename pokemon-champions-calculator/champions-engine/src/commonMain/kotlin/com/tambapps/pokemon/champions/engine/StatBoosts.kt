package com.tambapps.pokemon.champions.engine

/** Stat stage boosts, each clamped to the game's [-6, 6] range. */
data class StatBoosts(
  val attack: Int = 0,
  val defense: Int = 0,
  val specialAttack: Int = 0,
  val specialDefense: Int = 0,
  val speed: Int = 0,
) {
  init {
    for (stage in listOf(attack, defense, specialAttack, specialDefense, speed)) {
      requireValid(stage in -6..6) { "Stat boost stages must be within -6..6, got $stage" }
    }
  }

  operator fun get(stat: BoostableStat): Int = when (stat) {
    BoostableStat.ATTACK -> attack
    BoostableStat.DEFENSE -> defense
    BoostableStat.SPECIAL_ATTACK -> specialAttack
    BoostableStat.SPECIAL_DEFENSE -> specialDefense
    BoostableStat.SPEED -> speed
  }

  /** These boosts with [stat] set to [stage], clamped to [-6, 6]. */
  fun with(stat: BoostableStat, stage: Int): StatBoosts {
    val clamped = stage.coerceIn(-6, 6)
    return when (stat) {
      BoostableStat.ATTACK -> copy(attack = clamped)
      BoostableStat.DEFENSE -> copy(defense = clamped)
      BoostableStat.SPECIAL_ATTACK -> copy(specialAttack = clamped)
      BoostableStat.SPECIAL_DEFENSE -> copy(specialDefense = clamped)
      BoostableStat.SPEED -> copy(speed = clamped)
    }
  }

  companion object {
    val NONE = StatBoosts()
  }
}

internal fun BattlePokemon.withBoost(stat: BoostableStat, stage: Int) = copy(boosts = boosts.with(stat, stage))

/**
 * Applies a stat stage to a raw stat value, using the games' integer-truncated ratio table
 * (+1 = 3/2, +2 = 2/1, ... -1 = 2/3, -2 = 1/2, ...) rather than a flat 50%-per-stage approximation.
 */
fun applyBoostStage(rawStat: Int, stage: Int): Int = when {
  stage > 0 -> (rawStat * (2 + stage)) / 2
  stage < 0 -> (rawStat * 2) / (2 - stage)
  else -> rawStat
}
