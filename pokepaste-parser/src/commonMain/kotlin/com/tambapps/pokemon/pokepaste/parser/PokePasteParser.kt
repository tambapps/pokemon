package com.tambapps.pokemon.pokepaste.parser

import com.tambapps.pokemon.*

class PokepasteParser(
  private val defaultLevel: Int = 100
) {

  companion object {
    const val DEFAULT_NATURE = "Hardy" // neutral nature
    const val DEFAULT_HAPPINESS = 255
    const val DEFAULT_IV_VALUE = 31
    const val DEFAULT_EV_VALUE = 0

    private const val ABILITY_PREFIX = "Ability:"
    private const val TERA_TYPE_PREFIX = "Tera Type:"
    private const val LEVEL_PREFIX = "Level:"
    private const val EVS_PREFIX = "EVs:"
    private const val IVS_PREFIX = "IVs:"
    private const val NATURE_SUFFIX = "Nature"
    private const val MOVE_PREFIX = "- "
    private const val SHINY_PREFIX = "Shiny:"
    private const val HAPPINESS_PREFIX = "Happiness:"

    private const val YES = "Yes"
    private const val STAT_SEPARATOR = " / "
    private const val ITEM_SEPARATOR = "@"
    
    private const val STAT_ATTACK = "Atk"
    private const val STAT_DEFENSE = "Def"
    private const val STAT_SPECIAL_ATTACK = "SpA"
    private const val STAT_SPECIAL_DEFENSE = "SpD"
    private const val STAT_HP = "HP"
    private const val STAT_SPEED = "Spe"
  }

  fun parse(input: String): PokePaste {
    val pokemons = mutableListOf<Pokemon>()
    val lineReader = LineReader(input)

    while (lineReader.hasNext()) {
      val (name, item, surname, gender) = parseHeader(lineReader.readLine())

      var ability: String? = null
      var teraType: PokeType? = null
      var level: Int = defaultLevel
      val moves = mutableListOf<String>()
      var ivs: PokeStats? = null
      var evs: PokeStats? = null
      var nature = DEFAULT_NATURE // neutral nature
      var shiny = false
      var happiness = DEFAULT_HAPPINESS

      var line: String
      while (lineReader.hasNext()) {
        line = lineReader.readLine()
        if (line.isEmpty()) break // parsing of current Pokemon is finished. Moving on
        if (line.startsWith(ABILITY_PREFIX)) {
          ability = extractRight(line)
        } else if (line.startsWith(TERA_TYPE_PREFIX)) {
          teraType = parseTeraType(extractRight(line))
        } else if (line.startsWith(LEVEL_PREFIX)) {
          level = extractRightInt(line);
        } else if (line.startsWith(EVS_PREFIX)) {
          evs = parseStats(line, DEFAULT_EV_VALUE)
        } else if (line.startsWith(IVS_PREFIX)) {
          ivs = parseStats(line, DEFAULT_IV_VALUE)
        } else if (line.endsWith(NATURE_SUFFIX)) {
          nature = line.substring(0, line.indexOf(" "));
        } else if (line.startsWith(MOVE_PREFIX)) {
          moves.add(line.substring(MOVE_PREFIX.length))
        } else if (line.startsWith(SHINY_PREFIX)) {
          shiny = extractRight(line) == YES
        } else if (line.startsWith(HAPPINESS_PREFIX)) {
          happiness = extractRightInt(line)
        }
      }
      pokemons.add(Pokemon(
        name = name,
        surname = surname,
        gender = gender,
        nature = nature,
        item = item,
        shiny = shiny,
        happiness = happiness,
        ability = ability,
        teraType = teraType,
        level = level,
        moves = moves.toList(),
        ivs = ivs ?: PokeStats.default(DEFAULT_IV_VALUE),
        evs = evs ?: PokeStats.default(DEFAULT_EV_VALUE)
      ))
    }
    return PokePaste(pokemons.toList())
  }

  private fun parseTeraType(str: String) = try {
    PokeType.valueOf(str.trim().uppercase())
  } catch (_: IllegalArgumentException) {
    throw PokePasteParseException(str, "Unknown tera type $str")
  }

  private fun parseStats(line: String, defaultValue: Int) = buildStats(defaultValue) {
    val statsStr = extractRight(line)
    val stats = statsStr.split(STAT_SEPARATOR)
    for (stat in stats) {
      val parts = stat.split(" ")
      if (parts.size != 2) throw PokePasteParseException(line)
      val value = parts[0].toIntOrNull() ?: throw PokePasteParseException(line)
      val statName = parts[1]
      when (statName) {
        STAT_ATTACK -> attack = value
        STAT_DEFENSE -> defense = value
        STAT_SPECIAL_ATTACK -> specialAttack = value
        STAT_SPECIAL_DEFENSE -> specialDefense = value
        STAT_HP -> hp = value
        STAT_SPEED -> speed = value
        else -> throw PokePasteParseException(line, "Unknown stat $statName")
      }
    }
  }

  private fun extractRight(line: String) = line.substring(line.indexOf(":") + 1).trim()

  private fun extractRightInt(line: String) = try {
    extractRight(line).toInt()
  } catch (_: NumberFormatException) {
    throw PokePasteParseException(line, "Invalid number")
  }

  private data class PokemonHeader(var name: String = "", var item: String? = null, var surname: String? = null, var gender: String? = null)

  private fun parseHeader(headerLine: String) = PokemonHeader().apply {
    var line = headerLine
    if (headerLine.contains(ITEM_SEPARATOR)) {
      val parts = headerLine.split(ITEM_SEPARATOR)
      if (parts.size != 2) throw PokePasteParseException(headerLine, "Missing item")
      item = parts.last().trim()
      line = parts.first().trim()
    }
    val nbParenthesis = line.count { it == '(' }
    if (line.count { it == ')' } != nbParenthesis) throw PokePasteParseException(headerLine)
    when (nbParenthesis) {
      // no surname and no specific gender
      0 -> name = line.trim()
      // surname or gender
      1 -> {
        val content = line.substring(line.indexOf('(') + 1, line.indexOf(')')).trim()
        if (content == Gender.MALE || content == Gender.FEMALE) {
          name = line.substring(0, line.indexOf('(')).trim()
          gender = content
        } else {
          name = content
          surname = line.substring(0, line.indexOf('(')).trim()
        }
      }
      // both surname and gender
      2 -> {
        surname = line.substring(0, line.indexOf('(')).trim()
        name = line.substring(line.indexOf('(') + 1, line.indexOf(')')).trim()
        gender = line.substring(line.lastIndexOf('(') + 1, line.lastIndexOf(')')).trim()
      }
      else -> throw PokePasteParseException(headerLine)
    }
  }
}

private class LineReader(input: String) {
  private val lines = input.trim().lines()
  private var i = 0

  fun readLine() = lines[i++].trim()

  fun hasNext() = i < lines.size
}