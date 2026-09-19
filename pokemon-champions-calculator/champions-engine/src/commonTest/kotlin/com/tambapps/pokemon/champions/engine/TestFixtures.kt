package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.AbilityName
import com.tambapps.pokemon.Gender
import com.tambapps.pokemon.ItemName
import com.tambapps.pokemon.MoveName
import com.tambapps.pokemon.Nature
import com.tambapps.pokemon.PokeStats
import com.tambapps.pokemon.PokemonName
import com.tambapps.pokemon.champions.data.ChampionsDex

/** Shared BattlePokemon builder for tests that don't need every field spelled out. Unset stats default to 0, not a realistic spread -- pass only what the test cares about. */
internal fun testPokemon(
  species: String,
  ability: String = "",
  nature: Nature = Nature.HARDY,
  hp: Int = 0, attack: Int = 0, defense: Int = 0, specialAttack: Int = 0, specialDefense: Int = 0, speed: Int = 0,
  item: String? = null,
  status: Status = Status.HEALTHY,
  gender: Gender = Gender.ASEXUAL,
  boosts: StatBoosts = StatBoosts.NONE,
  currentHp: Int? = null,
  abilityIsActive: Boolean = false,
  isVulnerableFromGlaiveRush: Boolean = false,
) = BattlePokemon(
  species = ChampionsDex.species(PokemonName(species)),
  ability = AbilityName(ability),
  nature = nature,
  statPoints = PokeStats(hp = hp, attack = attack, defense = defense, specialAttack = specialAttack, specialDefense = specialDefense, speed = speed),
  item = item?.let(::ItemName),
  status = status,
  gender = gender,
  boosts = boosts,
  currentHp = currentHp,
  abilityIsActive = abilityIsActive,
  isVulnerableFromGlaiveRush = isVulnerableFromGlaiveRush,
)

internal fun testMove(name: String) = ChampionsDex.move(MoveName(name))
