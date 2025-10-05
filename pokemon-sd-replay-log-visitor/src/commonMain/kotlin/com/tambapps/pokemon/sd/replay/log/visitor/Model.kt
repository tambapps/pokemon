package com.tambapps.pokemon.sd.replay.log.visitor

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.PokemonName


data class OtsPokemon(
  val name: PokemonName,
  val item: String,
  val ability: String,
  val moves: List<String>,
  val level: Int,
  val teraType: PokeType?
)
