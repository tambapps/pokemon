package com.tambapps.pokemon.champions.data

import com.tambapps.pokemon.AbilityName
import com.tambapps.pokemon.ItemName
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
  /**
   * The forms this species switches between in battle (e.g. Charizard -> Charizard, Mega Charizard X,
   * Mega Charizard Y; Aegislash -> Aegislash-Shield, Aegislash-Blade). Empty if it has a single form.
   * Regional forms, Rotom appliances etc. are separate species, not forms.
   */
  val forms: List<PokemonName>,
  /** Whether this species only exists as another species' form (e.g. Mega Charizard X, Aegislash-Shield). */
  val isAlternateForm: Boolean,
  /** The mega stone to hold to mega evolve into this species, if it is a mega. */
  val megaStone: ItemName?,
) {
  fun hasType(type: PokeType): Boolean = type == primaryType || type == secondaryType
}
