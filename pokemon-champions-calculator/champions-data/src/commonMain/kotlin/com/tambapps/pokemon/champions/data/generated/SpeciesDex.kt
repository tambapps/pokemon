package com.tambapps.pokemon.champions.data.generated

import com.tambapps.pokemon.AbilityName
import com.tambapps.pokemon.ItemName
import com.tambapps.pokemon.PokeStats
import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.PokemonName
import com.tambapps.pokemon.PokemonNormalizer
import com.tambapps.pokemon.champions.data.PokemonSpecies
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class BaseStatsDto(val hp: Int, val atk: Int, val def: Int, val spa: Int, val spd: Int, val spe: Int) {
  fun toDomain() = PokeStats(hp = hp, attack = atk, defense = def, specialAttack = spa, specialDefense = spd, speed = spe)
}

@Serializable
private data class SpeciesDto(
  val types: List<String>,
  val baseStats: BaseStatsDto,
  val weightKg: Double,
  val defaultAbility: String,
  // only generated for the species they apply to
  val forms: List<String> = emptyList(),
  val isAlternateForm: Boolean = false,
  val megaStone: String? = null,
)

/** Every Pokemon species/form legal in the Champions format, keyed by normalized name, parsed once on first access. */
internal val ALL_SPECIES: Map<String, PokemonSpecies> by lazy {
  Json.decodeFromString<Map<String, SpeciesDto>>(SPECIES_JSON).entries.associate { (name, dto) ->
    PokemonNormalizer.normalize(name) to PokemonSpecies(
      name = PokemonName(name),
      primaryType = PokeType.valueOf(dto.types[0].uppercase()),
      secondaryType = dto.types.getOrNull(1)?.let { PokeType.valueOf(it.uppercase()) },
      baseStats = dto.baseStats.toDomain(),
      weightKg = dto.weightKg,
      defaultAbility = AbilityName(dto.defaultAbility),
      forms = dto.forms.map(::PokemonName),
      isAlternateForm = dto.isAlternateForm,
      megaStone = dto.megaStone?.let(::ItemName),
    )
  }
}
