package com.tambapps.pokemon

data class Pokemon(
  val name: String,
  val surname: String?,
  val gender: Gender?,
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

enum class Gender {
  MALE,
  FEMALE,
  ASEXUAL
}

// TODO add enum for nature
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