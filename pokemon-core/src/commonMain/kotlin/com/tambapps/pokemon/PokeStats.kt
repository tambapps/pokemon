package com.tambapps.pokemon

data class PokeStats(
  val hp: Int,
  val speed: Int,
  val attack: Int,
  val specialAttack: Int,
  val defense: Int,
  val specialDefense: Int,
) {
  companion object {
    fun default(defaultValue: Int) = PokeStats(defaultValue, defaultValue, defaultValue, defaultValue, defaultValue, defaultValue)
  }
}

fun buildStats(defaultValue: Int, builder: PokeStatsBuilder.() -> Unit): PokeStats = PokeStatsBuilder(defaultValue).apply(builder).run {
  PokeStats(attack = attack, speed = speed, specialAttack = specialAttack, defense = defense, specialDefense = specialDefense, hp = hp)
}

class PokeStatsBuilder(defaultValue: Int) {
  var hp = defaultValue
  var speed = defaultValue
  var attack = defaultValue
  var specialAttack = defaultValue
  var defense = defaultValue
  var specialDefense = defaultValue
}