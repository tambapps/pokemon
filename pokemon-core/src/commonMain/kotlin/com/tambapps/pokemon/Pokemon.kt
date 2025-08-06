package com.tambapps.pokemon

data class Pokemon(
  val name: String,
  val surname: String?,
  val gender: String?,
  val nature: String,
  val item: String?,
  val shiny: Boolean,
  val happiness: Int,
  val ability: String?,
  val teraType: PokeType?,
  val level: Int,
  val moves: List<String>,
  val ivs: PokeStats,
  val evs: PokeStats,
)

object Gender {
  const val MALE = "M"
  const val FEMALE = "F"
}

enum class PokeType {
  STEEL,
  FIGHTING,
  DRAGON,
  FIRE,
  ICE,
  NORMAL,
  WATER,
  GRASS,
  ELECTRIC,
  FAIRY,
  POISON,
  PSY,
  ROCK,
  GHOST,
  DARK,
  GROUND,
  FLYING,
  PSYCHIC,
  BUG,
  // not really types, but meh
  STELLAR,
  UNKNOWN
}