package com.tambapps.pokemon.pokepaste.parser

import com.tambapps.pokemon.Pokemon
import kotlinx.serialization.Serializable

@Serializable
data class PokePaste(
  val pokemons: List<Pokemon>,
)

class PokePasteParseException(line: String, reason: String = "Invalid line"): Exception(
  "Invalid pokepaste line $line: $reason"
)