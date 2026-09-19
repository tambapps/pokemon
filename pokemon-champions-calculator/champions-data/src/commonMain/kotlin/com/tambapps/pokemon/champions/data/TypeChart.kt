package com.tambapps.pokemon.champions.data

import com.tambapps.pokemon.PokeType

/** The type effectiveness matrix: how much an attacking type's damage is multiplied against each defending type. */
class TypeChart(private val multipliers: Map<PokeType, Map<PokeType, Double>>) {
  fun effectivenessOf(attackType: PokeType, defenseType: PokeType): Double =
    multipliers.getValue(attackType).getValue(defenseType)
}
