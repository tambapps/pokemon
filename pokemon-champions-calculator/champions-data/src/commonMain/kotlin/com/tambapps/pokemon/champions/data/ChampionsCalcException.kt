package com.tambapps.pokemon.champions.data

/**
 * Thrown for every error of the Champions damage calculator: an unknown species or move, or an
 * invalid calc input (e.g. a stat boost out of the -6..6 range). Callers can catch this single
 * type instead of generic exceptions.
 */
class ChampionsCalcException(message: String) : RuntimeException(message)
