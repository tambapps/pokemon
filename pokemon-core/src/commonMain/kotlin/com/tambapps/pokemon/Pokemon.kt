package com.tambapps.pokemon

import kotlinx.serialization.Serializable

@Serializable
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

enum class Nature(
  val bonusStat: Stat?,
  val malusStat: Stat?
) {
  ADAMANT(bonusStat = Stat.ATTACK, malusStat = Stat.SPECIAL_ATTACK),
  BOLD(bonusStat = Stat.DEFENSE, malusStat = Stat.ATTACK),
  BRAVE(bonusStat = Stat.ATTACK, malusStat = Stat.SPEED),
  CALM(bonusStat = Stat.SPECIAL_DEFENSE, malusStat = Stat.ATTACK),
  CAREFUL(bonusStat = Stat.SPECIAL_DEFENSE, malusStat = Stat.SPECIAL_ATTACK),
  DOCILE(bonusStat = null, malusStat = null),
  GENTLE(bonusStat = Stat.SPECIAL_DEFENSE, malusStat = Stat.DEFENSE),
  HARDY(bonusStat = null, malusStat = null),
  HASTY(bonusStat = Stat.SPEED, malusStat = Stat.DEFENSE),
  IMPISH(bonusStat = Stat.DEFENSE, malusStat = Stat.SPECIAL_ATTACK),
  JOLLY(bonusStat = Stat.SPEED, malusStat = Stat.SPECIAL_ATTACK),
  LAX(bonusStat = Stat.DEFENSE, malusStat = Stat.SPECIAL_DEFENSE),
  LONELY(bonusStat = Stat.ATTACK, malusStat = Stat.DEFENSE),
  MILD(bonusStat = Stat.SPECIAL_ATTACK, malusStat = Stat.DEFENSE),
  MODEST(bonusStat = Stat.SPECIAL_ATTACK, malusStat = Stat.ATTACK),
  NAIVE(bonusStat = Stat.SPEED, malusStat = Stat.SPECIAL_DEFENSE),
  NAUGHTY(bonusStat = Stat.ATTACK, malusStat = Stat.SPECIAL_DEFENSE),
  QUIET(bonusStat = Stat.SPECIAL_ATTACK, malusStat = Stat.SPEED),
  QUIRKY(bonusStat = null, malusStat = null),
  RASH(bonusStat = Stat.SPECIAL_ATTACK, malusStat = Stat.SPECIAL_DEFENSE),
  RELAXED(bonusStat = Stat.DEFENSE, malusStat = Stat.SPEED),
  SASSY(bonusStat = Stat.SPECIAL_DEFENSE, malusStat = Stat.SPEED),
  SERIOUS(bonusStat = null, malusStat = null),
  TIMID(bonusStat = Stat.SPEED, malusStat = Stat.ATTACK),
  BASHFUL(bonusStat = null, malusStat = null);

  val isNeutral: Boolean get() = bonusStat == null && malusStat == null
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