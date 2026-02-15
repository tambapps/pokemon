package com.tambapps.pokemon

import com.tambapps.pokemon.util.PrefixTrie

object PokemonNormalizer {

  // Initialize the prefix trie with Pokemon forms that need prefix matching
  private val prefixTrie = PrefixTrie().apply {
    insert("urshifu")
    insert("indeedee")
    insert("oinkologne")
    insert("meowstic")
    insert("ogerpon")
    insert("oricorio")
    insert("ursaluna")
    insert("rotom")
    insert("terapagos")
    insert("zamazenta")
    insert("zacian")
    insert("necrozma")
    insert("calyrex")
    insert("kyurem")
    insert("tatsugiri")
    insert("pyroar")
    insert("basculegion")
  }

  fun pretty(s: String) = buildString {
    val first = s.first()
    append(if (first.isLetter()) first.uppercase() else first)
    for (i in 1 until s.length) {
      val last = s[i - 1]
      val current = s[i]
      if (current == '-') {
        continue
      }
      append(if (last == '-') " ${current.uppercase()}" else current.toString())
    }
  }

  fun matches(p1: String, p2: String) = normalize(p1) == normalize(p2)

  fun baseMatches(p1: String, p2: String) = normalizeToBase(p1) == normalizeToBase(p2)

  fun normalize(s: String): String = s.lowercase().replace(' ', '-')

  fun normalizeToBase(input: String): String {
    val s = normalize(input)

    // First, try prefix matching using the trie (O(m) where m is input length)
    val prefixMatch = prefixTrie.findLongestPrefixMatch(s)
    if (prefixMatch != null) {
      return prefixMatch
    }

    // Then handle suffix patterns (still O(1) constant time checks)
    return when {
      !s.contains("-") -> s
      s.endsWith("-hisui")
          || s.endsWith("-galar")
          || s.endsWith("-alola") -> s.dropLast(6)
      s.endsWith("-incarnate") -> s.dropLast(10)
      s.contains("-paldea") -> {
        // because of Tauros-Paldea-Aqua
        s.substringBefore("-paldea")
      }
      else -> s
    }
  }
}