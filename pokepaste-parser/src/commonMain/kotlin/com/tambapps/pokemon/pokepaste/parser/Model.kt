package com.tambapps.pokemon.pokepaste.parser

import com.tambapps.pokemon.Pokemon
import kotlinx.serialization.Serializable

// TODO add metadata, or plain fields (for format, and team name)
@Serializable
data class PokePaste(
  val pokemons: List<Pokemon>,
) {
  fun toPokePasteString(): String = buildString {
    for (pokemon in pokemons) {
      if (pokemon.surname != null) {
        append("${pokemon.surname} (${pokemon.name})")
      } else {
        append(pokemon.name)
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
      
      if (pokemon.teraType != null) {
        appendLine("Tera Type: ${pokemon.teraType}")
      }
      
      val evs = pokemon.evs
      if (evs != com.tambapps.pokemon.PokeStats.default(0)) {
        appendLine("EVs: ${evsToString(evs)}")
      }
      
      if (pokemon.nature != com.tambapps.pokemon.Nature.HARDY) {
        appendLine("${pokemon.nature.name.lowercase().replaceFirstChar { it.uppercase() }} Nature")
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

/*
  @override
  String toString() {
    StringBuffer buffer = StringBuffer();
    for (Pokemon pokemon in pokemons) {
      buffer.write(pokemon.name);
      if (pokemon.gender != null) {
        buffer.write(" (${pokemon.gender})");
      }
      if (pokemon.item != null) {
        buffer.write(" @ ${pokemon.item}");
      }
      buffer.writeln();
      buffer.writeln("Ability: ${pokemon.ability}");
      if (pokemon.level != null) {
        buffer.writeln("Level: ${pokemon.level}");
      }
      buffer.writeln("Tera Type: ${pokemon.teraType}");
      Stats evs = pokemon.evs ?? Stats.withDefault(0);
      buffer.writeln("EVs: ${_statsToString(evs)}");
      if (pokemon.nature != null) {
        buffer.writeln("${pokemon.nature} Nature");
      }
      if (pokemon.ivs != null && pokemon.ivs != Stats.withDefault(31)) {
        buffer.writeln("IVs: ${_statsToString(pokemon.ivs!)}");
      }
      for (String move in pokemon.moves) {
        buffer.writeln("- $move");
      }
      buffer.writeln();
    }
    return buffer.toString();
  }

 */