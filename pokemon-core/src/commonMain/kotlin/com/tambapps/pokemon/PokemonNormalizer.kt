package com.tambapps.pokemon

import kotlin.text.iterator

object PokemonNormalizer {

  private class TrieNode {
    val children = mutableMapOf<Char, TrieNode>()
    var value: String? = null
  }

  private class PrefixTrie {
    private val root = TrieNode()

    fun insert(prefix: String, value: String) {
      var current = root
      for (char in prefix) {
        current = current.children.getOrPut(char) { TrieNode() }
      }
      current.value = value
    }

    fun findLongestPrefixMatch(input: String): String? {
      var current = root
      var lastMatch: String? = null

      for (char in input) {
        current = current.children[char] ?: break
        if (current.value != null) {
          lastMatch = current.value
        }
      }

      return lastMatch
    }
  }

  // Initialize the prefix trie with Pokemon forms that need prefix matching
  private val prefixTrie = PrefixTrie().apply {
    insert("urshifu", "urshifu")
    insert("ogerpon", "ogerpon")
    insert("ursaluna", "ursaluna")
    insert("rotom", "rotom")
    insert("terapagos", "terapagos")
    insert("zamazenta", "zamazenta")
    insert("zacian", "zacian")
    insert("necrozma", "necrozma")
    insert("calyrex", "calyrex")
    insert("kyurem", "kyurem")
    insert("tatsugiri", "tatsugiri")
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
      s.endsWith("-galar") -> s.dropLast(6)
      s.endsWith("-alola") -> s.dropLast(6)
      s.endsWith("-incarnate") -> s.dropLast(10)
      s.contains("-paldea") -> {
        // because of Tauros-Paldea-Aqua
        s.substringBefore("-paldea")
      }
      else -> s
    }
  }
}