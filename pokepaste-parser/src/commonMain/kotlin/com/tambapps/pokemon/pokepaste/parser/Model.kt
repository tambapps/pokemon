package com.tambapps.pokemon.pokepaste.parser

import com.tambapps.pokemon.Pokemon
import com.tambapps.pokemon.TeraType

// TODO add metadata, or plain fields (for format, and team name)
data class PokePaste(
  val pokemons: List<Pokemon>,
  val isOts: Boolean = false,
) {
  fun toPokePasteString(): String = buildString {
    for (pokemon in pokemons) {
      if (pokemon.surname != null) {
        append("${pokemon.surname} (${pokemon.name.value})")
      } else {
        append(pokemon.name.value)
      }
      
      pokemon.gender?.let { gender ->
        append(" (${gender.name.first()})")
      }
      
      if (pokemon.item != null) {
        append(" @ ${pokemon.item}")
      }
      
      appendLine()
      
      if (pokemon.ability != null) {
        appendLine("Ability: ${pokemon.ability}")
      }
      
      if (pokemon.level != 100) {
        appendLine("Level: ${pokemon.level}")
      }
      
      if (pokemon.shiny) {
        appendLine("Shiny: Yes")
      }
      
      if (pokemon.happiness != 255) {
        appendLine("Happiness: ${pokemon.happiness}")
      }

      pokemon.teraType?.let { teraType ->
        appendLine("Tera Type: ${teraType.pascalCase}")
      }

      val evs = pokemon.evs
      if (evs != com.tambapps.pokemon.PokeStats.default(0)) {
        appendLine("EVs: ${evsToString(evs)}")
      }

      pokemon.nature?.let { nature ->
        appendLine("${nature.name.lowercase().replaceFirstChar { it.uppercase() }} Nature")
      }
      
      val ivs = pokemon.ivs
      if (ivs != com.tambapps.pokemon.PokeStats.default(31)) {
        appendLine("IVs: ${ivsToString(ivs)}")
      }
      
      for (move in pokemon.moves) {
        appendLine("- $move")
      }
      
      appendLine()
    }
  }

  private fun evsToString(stats: com.tambapps.pokemon.PokeStats): String {
    val parts = mutableListOf<String>()
    if (stats.hp > 0) parts.add("${stats.hp} HP")
    if (stats.attack > 0) parts.add("${stats.attack} Atk")
    if (stats.defense > 0) parts.add("${stats.defense} Def")
    if (stats.specialAttack > 0) parts.add("${stats.specialAttack} SpA")
    if (stats.specialDefense > 0) parts.add("${stats.specialDefense} SpD")
    if (stats.speed > 0) parts.add("${stats.speed} Spe")
    return parts.joinToString(" / ")
  }
  
  private fun ivsToString(stats: com.tambapps.pokemon.PokeStats): String {
    val parts = mutableListOf<String>()
    if (stats.hp < 31) parts.add("${stats.hp} HP")
    if (stats.attack < 31) parts.add("${stats.attack} Atk")
    if (stats.defense < 31) parts.add("${stats.defense} Def")
    if (stats.specialAttack < 31) parts.add("${stats.specialAttack} SpA")
    if (stats.specialDefense < 31) parts.add("${stats.specialDefense} SpD")
    if (stats.speed < 31) parts.add("${stats.speed} Spe")
    return parts.joinToString(" / ")
  }
}

private val TeraType.pascalCase get() = name[0].uppercase() + name.substring(1).lowercase()