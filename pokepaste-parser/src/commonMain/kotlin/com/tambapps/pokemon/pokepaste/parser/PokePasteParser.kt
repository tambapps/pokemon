package com.tambapps.pokemon.pokepaste.parser

import com.tambapps.pokemon.AbilityName
import com.tambapps.pokemon.Gender
import com.tambapps.pokemon.ItemName
import com.tambapps.pokemon.MoveName
import com.tambapps.pokemon.Nature
import com.tambapps.pokemon.PokeStats
import com.tambapps.pokemon.Pokemon
import com.tambapps.pokemon.PokemonName
import com.tambapps.pokemon.TeraType
import com.tambapps.pokemon.buildStats

class PokepasteParser(
  private val defaultLevel: Int = 100,
  val formatPokemonName: (String) -> String = { it },
  val formatPokemonTrait: (String) -> String = { it },
) {

  companion object {
    const val DEFAULT_HAPPINESS = 255
    const val DEFAULT_IV_VALUE = 31
    const val DEFAULT_EV_VALUE = 0

    const val GENDER_MALE = "M"
    const val GENDER_FEMALE = "F"

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

  fun tryParse(input: String) = try {
    parse(input)
  } catch (_: PokePasteParseException) {
    null
  }

  fun parse(input: String): PokePaste {
    val pokemons = mutableListOf<Pokemon>()
    val lineReader = LineReader(input)

    var isOts = true
    while (lineReader.hasNext()) {
      val (name, item, surname, gender) = parseHeader(lineReader.readLine())

      var ability: String? = null
      var teraType: TeraType? = null
      var level: Int = defaultLevel
      val moves = mutableListOf<MoveName>()
      var ivs: PokeStats? = null
      var evs: PokeStats? = null
      var nature: Nature? = null
      var shiny = false
      var happiness = DEFAULT_HAPPINESS

      var line: String
      while (lineReader.hasNext()) {
        line = lineReader.readLine()
        if (line.isEmpty()) break // parsing of current Pokemon is finished. Moving on
        if (line.startsWith(ABILITY_PREFIX)) {
          ability = formatPokemonTrait(extractRight(line))
        } else if (line.startsWith(TERA_TYPE_PREFIX)) {
          teraType = parseTeraType(line, extractRight(line))
        } else if (line.startsWith(LEVEL_PREFIX)) {
          level = extractRightInt(line);
        } else if (line.startsWith(EVS_PREFIX)) {
          isOts = false
          evs = parseStats(line, DEFAULT_EV_VALUE)
        } else if (line.startsWith(IVS_PREFIX)) {
          isOts = false
          ivs = parseStats(line, DEFAULT_IV_VALUE)
        } else if (line.endsWith(NATURE_SUFFIX)) {
          isOts = false
          nature = parseNature(line, line.substring(0, line.indexOf(" ")).uppercase())
        } else if (line.startsWith(MOVE_PREFIX)) {
          moves.add(MoveName(formatPokemonTrait(line.substring(MOVE_PREFIX.length))))
        } else if (line.startsWith(SHINY_PREFIX)) {
          shiny = extractRight(line) == YES
        } else if (line.startsWith(HAPPINESS_PREFIX)) {
          happiness = extractRightInt(line)
        }
      }
      if (moves.isEmpty()) throw PokePasteParseException("Pokemon $name has no moves")
      pokemons.add(
        Pokemon(
          name = PokemonName(name),
          surname = surname,
          gender = gender,
          nature = nature,
          item = item?.let(::ItemName),
          shiny = shiny,
          happiness = happiness,
          ability = ability?.let(::AbilityName),
          teraType = teraType,
          level = level,
          moves = moves.toList(),
          ivs = ivs ?: PokeStats.default(DEFAULT_IV_VALUE),
          evs = evs ?: PokeStats.default(DEFAULT_EV_VALUE)
        )
      )
    }
    if (pokemons.isEmpty()) throw PokePasteParseException("Empty pokepaste")
    return PokePaste(pokemons.toList(), isOts)
  }

  private fun parseNature(line: String, str: String) = try {
    Nature.valueOf(str)
  } catch (_: IllegalArgumentException) {
    throw PokePasteParseException(line, "Unknown nature $str")
  }

  private fun parseTeraType(line: String, str: String) = try {
    TeraType.valueOf(str.trim().uppercase())
  } catch (_: IllegalArgumentException) {
    throw PokePasteParseException(line, "Unknown tera type $str")
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

  private data class PokemonHeader(
    var name: String = "",
    var item: String? = null,
    var surname: String? = null,
    var gender: Gender? = null
  )

  private fun parseHeader(headerLine: String) = PokemonHeader().apply {
    var line = headerLine
    if (headerLine.contains(ITEM_SEPARATOR)) {
      val parts = headerLine.split(ITEM_SEPARATOR)
      if (parts.size != 2) throw PokePasteParseException(headerLine, "Missing item")
      item = formatPokemonTrait(parts.last().trim())
      line = parts.first().trim()
    }
    val nbParenthesis = line.count { it == '(' }
    if (line.count { it == ')' } != nbParenthesis) throw PokePasteParseException(headerLine)
    when (nbParenthesis) {
      // no surname and no specific gender
      0 -> name = formatPokemonName(line.trim())
      1 -> { // surname or gender
        val content = parenthesisContent(line)
        if (content == GENDER_MALE || content == GENDER_FEMALE) {
          name = formatPokemonName(beforeParenthesisContent(line))
          gender = if (content == GENDER_MALE) Gender.MALE else Gender.FEMALE
        } else {
          name = formatPokemonName(content)
          surname = beforeParenthesisContent(line)
        }
      }

      2 -> { // both surname and gender
        surname = beforeParenthesisContent(line)
        name = formatPokemonName(parenthesisContent(line))
        gender = if (lastParenthesisContent(line) == GENDER_MALE) Gender.MALE else Gender.FEMALE
      }

      else -> throw PokePasteParseException(headerLine)
    }
    if (name.isEmpty()) throw PokePasteParseException(headerLine, "Missing name")
  }

  private fun beforeParenthesisContent(line: String) = line.substring(0, line.indexOf('(')).trim()

  private fun parenthesisContent(line: String) = parenthesisContent(line) { line.indexOf('(') }

  private fun lastParenthesisContent(line: String) = parenthesisContent(line) { line.lastIndexOf('(') }

  private inline fun parenthesisContent(line: String, startSupplier: () -> Int): String {
    val start = startSupplier.invoke()
    val end = line.indexOf(')', startIndex = start + 1)
    if (start == -1 || end == -1) throw PokePasteParseException(line)
    return line.substring(start + 1, end).trim()
  }
}

class PokePasteParseException(message: String) : Exception(message) {
  constructor(line: String, reason: String = "Invalid line"): this("Invalid pokepaste line $line: $reason")
}

private class LineReader(input: String) {
  private val lines = input.trim().lines()
  private var i = 0

  fun readLine() = lines[i++].trim()

  fun hasNext() = i < lines.size
}