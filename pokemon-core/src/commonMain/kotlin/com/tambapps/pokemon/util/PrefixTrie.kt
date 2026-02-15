package com.tambapps.pokemon.util

class PrefixTrie {
  private class TrieNode {
    val children = mutableMapOf<Char, TrieNode>()
    var value: String? = null
  }

  private val root = TrieNode()

    fun insert(value: String) = insert(value, value)

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
