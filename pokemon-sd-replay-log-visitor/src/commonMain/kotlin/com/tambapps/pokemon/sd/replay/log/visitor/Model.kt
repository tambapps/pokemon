package com.tambapps.pokemon.sd.replay.log.visitor

import com.tambapps.pokemon.PokeType


data class OtsPokemon(
  val name: String,
  val item: String,
  val ability: String,
  val moves: List<String>,
  val level: Int,
  val teraType: PokeType?
)
