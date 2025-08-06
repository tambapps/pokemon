package com.tambapps.pokemon.pokepaste.parser

import com.tambapps.pokemon.Pokemon
import kotlinx.serialization.Serializable

@Serializable
data class PokePaste(
  val pokemons: List<Pokemon>,
)
