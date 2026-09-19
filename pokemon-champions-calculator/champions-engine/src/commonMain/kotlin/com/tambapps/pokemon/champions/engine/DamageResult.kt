package com.tambapps.pokemon.champions.engine

/**
 * The outcome of one hit: 16 damage values, one for each of the games' 85%-100% random rolls,
 * ascending. A move that can't deal damage (a status move, or one blocked by an immunity)
 * reports a single zero roll.
 */
data class DamageResult(
  val rolls: List<Int>,
  val typeEffectiveness: Double,
  val isCritical: Boolean,
) {
  val minDamage: Int get() = rolls.first()
  val maxDamage: Int get() = rolls.last()

  companion object {
    fun noDamage(typeEffectiveness: Double = 1.0, isCritical: Boolean = false) =
      DamageResult(rolls = listOf(0), typeEffectiveness = typeEffectiveness, isCritical = isCritical)
  }
}
