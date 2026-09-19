package com.tambapps.pokemon.champions.engine

import kotlin.math.ceil
import kotlin.math.floor

/**
 * The games round exactly halfway values DOWN, unlike the usual round-half-up convention.
 * Used throughout the damage formula's modifier chain instead of standard rounding.
 */
fun pokeRound(value: Double): Int {
  val fraction = value - floor(value)
  return if (fraction > 0.5) ceil(value).toInt() else floor(value).toInt()
}

/** A modifier chain step expressed as a fixed-point fraction with a 0x1000 (4096) denominator, matching the games' internal math. */
fun pokeRound(numerator: Int, denominator: Int = 0x1000): Int = pokeRound(numerator.toDouble() / denominator)
