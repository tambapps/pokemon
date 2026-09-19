package com.tambapps.pokemon.champions.data

import com.tambapps.pokemon.MoveName
import com.tambapps.pokemon.PokemonName
import com.tambapps.pokemon.PokemonNormalizer
import com.tambapps.pokemon.champions.data.generated.ALL_MOVES
import com.tambapps.pokemon.champions.data.generated.ALL_SPECIES
import com.tambapps.pokemon.champions.data.generated.CHAMPIONS_TYPE_CHART

/**
 * Entry point for every Pokemon/move/type fact the Champions format defines. Species and move
 * data are keyed by [PokemonNormalizer]-normalized name, so lookups are case/spacing-insensitive
 * without every call site having to normalize by hand.
 */
object ChampionsDex {
  val typeChart: TypeChart get() = CHAMPIONS_TYPE_CHART

  val allSpecies: Collection<PokemonSpecies> get() = ALL_SPECIES.values
  val allMoves: Collection<Move> get() = ALL_MOVES.values

  fun species(name: PokemonName): PokemonSpecies =
    ALL_SPECIES[PokemonNormalizer.normalize(name.value)] ?: throw NoSuchElementException("Unknown Champions species: ${name.value}")

  fun move(name: MoveName): Move =
    ALL_MOVES[PokemonNormalizer.normalize(name.value)] ?: throw NoSuchElementException("Unknown Champions move: ${name.value}")
}
