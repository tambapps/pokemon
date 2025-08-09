package com.tambapps.pokemon.pokepaste.parser

import com.tambapps.pokemon.Pokemon
import kotlinx.serialization.Serializable

// TODO add metadata, or plain fields (for format, and team name)
@Serializable
data class PokePaste(
  val pokemons: List<Pokemon>,
)
