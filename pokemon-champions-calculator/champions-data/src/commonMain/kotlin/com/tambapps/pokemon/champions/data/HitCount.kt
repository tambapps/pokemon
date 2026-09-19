package com.tambapps.pokemon.champions.data

/** How many times a move strikes in a single use. */
sealed interface HitCount {
  /** Bullet Seed, Icicle Spear, Rock Blast... hit a random number of times in [min, max]. */
  data class Variable(val min: Int, val max: Int) : HitCount

  /** Double Hit, Dragon Darts, Dual Wingbeat, Twin Beam... always hit exactly [hits] times. */
  data class Fixed(val hits: Int) : HitCount

  companion object {
    val Once = Fixed(1)
  }
}
