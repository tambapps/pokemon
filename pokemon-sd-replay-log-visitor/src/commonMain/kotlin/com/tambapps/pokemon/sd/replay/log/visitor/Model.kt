package com.tambapps.pokemon.sd.replay.log.visitor

import com.tambapps.pokemon.PokemonName
import com.tambapps.pokemon.TeraType


data class OtsPokemon(
  val name: PokemonName,
  val item: String,
  val ability: String,
  val moves: List<String>,
  val level: Int,
  val teraType: TeraType?
)
