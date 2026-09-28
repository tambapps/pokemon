package com.tambapps.pokemon.champions.engine

/**
 * A plain JS object used as a dictionary, the way ko_chance.js uses them: damage totals (as string keys) to their
 * probability. A key is a whole number, suffixed with "*" once the damage triggered the defender's berry.
 *
 * Its [keys] iterate in the order a JS object's do: the non-negative whole numbers first, ascending, then every other
 * key (a negative number, a "*" key) in insertion order. That order decides which damage the source reads first and
 * in which order it adds up probabilities, so it is kept to match the source's results to the last bit.
 */
internal class JsDict {
  private val values = LinkedHashMap<String, Double>()

  val keys: List<String>
    get() {
      val (indexKeys, otherKeys) = values.keys.partition(::isArrayIndex)
      return indexKeys.sortedBy { it.toLong() } + otherKeys
    }

  operator fun get(key: String): Double? = values[key]

  operator fun set(key: String, value: Double) {
    values[key] = value
  }

  operator fun contains(key: String): Boolean = key in values

  /** The source's `dict[key] = key in dict ? dict[key] + value : value`. */
  fun add(key: String, value: Double) {
    val current = values[key]
    values[key] = if (current != null) current + value else value
  }

  /** The value at [key], which must be there. */
  fun valueOf(key: String): Double = values.getValue(key)

  private fun isArrayIndex(key: String): Boolean =
    // JS array indices are canonical numbers from 0 to 2^32 - 2; damage keys never get near that bound
    key == "0" || (key.first() in '1'..'9' && key.all { it in '0'..'9' } && key.length <= 9)

  companion object {
    /** The source's sortByKeys: a new dict with the same entries, inserted in [sortedKeys] order. */
    fun sortByKeys(dict: JsDict): JsDict {
      val sorted = JsDict()
      for (key in sortedKeys(dict)) sorted[key] = dict.valueOf(key)
      return sorted
    }

    /** The source's `Object.keys(dict).sort(numericSortParseInt)`: [keys] stably sorted by [parseKey]. */
    fun sortedKeys(dict: JsDict): List<String> = dict.keys.sortedBy(::parseKey)

    /** The source's parseInt of a key: the number, without the "*". */
    fun parseKey(key: String): Int = key.removeSuffix("*").toInt()

    /** Whether the damage of [key] triggered the defender's berry, i.e. the source's isNaN(key). */
    fun isStarred(key: String): Boolean = key.endsWith("*")

    fun key(damage: Int, isStarred: Boolean = false): String = if (isStarred) "$damage*" else damage.toString()
  }
}
