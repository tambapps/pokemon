package com.tambapps.pokemon.champions.data

import com.tambapps.pokemon.PokeStats
import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.PokemonName

data class PokemonSpecies(
  val name: PokemonName,
  val primaryType: PokeType,
  val secondaryType: PokeType?,
  val baseStats: PokeStats,
  val weightKg: Double,
) {
  fun hasType(type: PokeType): Boolean = type == primaryType || type == secondaryType
}
