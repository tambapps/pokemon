package com.tambapps.pokemon.champions.data

import com.tambapps.pokemon.AbilityName
import com.tambapps.pokemon.PokeStats
import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.PokemonName

data class PokemonSpecies(
  val name: PokemonName,
  val primaryType: PokeType,
  val secondaryType: PokeType?,
  val baseStats: PokeStats,
  val weightKg: Double,
  /**
   * The ability the source calculator pre-selects for this species. Not an exhaustive list of
   * the abilities it can legally have: ability legality isn't modeled.
   */
  val defaultAbility: AbilityName,
) {
  fun hasType(type: PokeType): Boolean = type == primaryType || type == secondaryType
}
