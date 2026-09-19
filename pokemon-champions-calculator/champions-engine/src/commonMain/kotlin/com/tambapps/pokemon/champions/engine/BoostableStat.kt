package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.Stat

/** A [Stat] other than HP: the only stats that can have boost stages in battle. */
enum class BoostableStat {
  ATTACK, DEFENSE, SPECIAL_ATTACK, SPECIAL_DEFENSE, SPEED;

  fun toStat(): Stat = when (this) {
    ATTACK -> Stat.ATTACK
    DEFENSE -> Stat.DEFENSE
    SPECIAL_ATTACK -> Stat.SPECIAL_ATTACK
    SPECIAL_DEFENSE -> Stat.SPECIAL_DEFENSE
    SPEED -> Stat.SPEED
  }
}
