package com.tambapps.pokemon

import kotlinx.serialization.Serializable

@Serializable
enum class Stat {
  ATTACK,
  DEFENSE,
  SPECIAL_ATTACK,
  SPECIAL_DEFENSE,
  SPEED,
  HP
}

@Serializable
data class PokeStats(
  val hp: Int,
  val speed: Int,
  val attack: Int,
  val specialAttack: Int,
  val defense: Int,
  val specialDefense: Int,
) {
  companion object {
    fun default(defaultValue: Int) =
      PokeStats(defaultValue, defaultValue, defaultValue, defaultValue, defaultValue, defaultValue)
  }

  operator fun get(stat: Stat) = when (stat) {
    Stat.ATTACK -> attack
    Stat.DEFENSE -> defense
    Stat.SPECIAL_ATTACK -> specialAttack
    Stat.SPECIAL_DEFENSE -> specialDefense
    Stat.SPEED -> speed
    Stat.HP -> hp
  }
}

fun buildStats(defaultValue: Int, builder: PokeStatsBuilder.() -> Unit): PokeStats =
  PokeStatsBuilder(defaultValue).apply(builder).run {
    PokeStats(
      attack = attack,
      speed = speed,
      specialAttack = specialAttack,
      defense = defense,
      specialDefense = specialDefense,
      hp = hp
    )
  }

class PokeStatsBuilder(defaultValue: Int) {
  var hp = defaultValue
  var speed = defaultValue
  var attack = defaultValue
  var specialAttack = defaultValue
  var defense = defaultValue
  var specialDefense = defaultValue
}