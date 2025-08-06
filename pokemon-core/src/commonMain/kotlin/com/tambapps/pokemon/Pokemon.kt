package com.tambapps.pokemon

data class Pokemon(
  val name: String,
  val surname: String?,
  val gender: Gender?,
  val nature: Nature,
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

// TODO make boosts information retrievable
enum class Nature {
  ADAMANT, // +Atk -SpA
  BOLD, // +Def -Atk
  BRAVE, // +Atk -Spe
  CALM, // +SpD -Atk
  CAREFUL, // +SpD -SpA
  DOCILE, // Neutral
  GENTLE, // +SpD -Def
  HARDY, // Neutral
  HASTY, // +Spe -Def
  IMPISH, // +Def -SpA
  JOLLY, // +Spe -SpA
  LAX, // +Def -SpD
  LONELY, // +Atk -Def
  MILD, // +SpA -Def
  MODEST, // +SpA -Atk
  NAIVE, // +Spe -SpD
  NAUGHTY, // +Atk -SpD
  QUIET, // +SpA -Spe
  QUIRKY, // Neutral
  RASH, // +SpA -SpD
  RELAXED, // +Def -Spe
  SASSY, // +SpD -Spe
  SERIOUS, // Neutral
  TIMID, // +Spe -Atk
  BASHFUL, // Neutral
}

enum class Gender {
  MALE,
  FEMALE,
  ASEXUAL
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