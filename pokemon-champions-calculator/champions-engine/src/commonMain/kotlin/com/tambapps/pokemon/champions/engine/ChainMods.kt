package com.tambapps.pokemon.champions.engine

import kotlin.math.floor

/**
 * Combines a list of fixed-point modifiers (each a fraction over 0x1000, e.g. 0x1800 = 1.5x)
 * into one, the same way the games apply a chain of independent multipliers: each step rounds
 * to the nearest integer (standard round-half-up, unlike [pokeRound]) before the next is applied.
 */
fun chainMods(mods: List<Int>): Int =
  mods.fold(0x1000) { accumulated, mod ->
    if (mod == 0x1000) accumulated else roundHalfUp(accumulated.toDouble() * mod / 0x1000)
  }

private fun roundHalfUp(value: Double): Int = floor(value + 0.5).toInt()
