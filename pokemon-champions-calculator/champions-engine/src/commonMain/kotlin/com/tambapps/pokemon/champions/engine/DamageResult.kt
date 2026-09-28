package com.tambapps.pokemon.champions.engine

/**
 * Which stat a hit's attack is read from, and whose. Usually the attacker's Attack or Special
 * Attack, but Body Press uses the attacker's Defense and Foul Play the defender's Attack.
 */
data class AttackStatSource(val stat: BoostableStat, val isDefenderStat: Boolean)

/**
 * The outcome of one hit: 16 damage values, one for each of the games' 85%-100% random rolls,
 * ascending. A move that can't deal damage (a status move, or one blocked by an immunity)
 * reports a single zero roll.
 */
data class DamageResult(
  val rolls: List<Int>,
  val typeEffectiveness: Double,
  val isCritical: Boolean,
  /** The stat the hit's attack was read from, e.g. to describe the calc. Null when no damage was calculated. */
  val attackStat: AttackStatSource? = null,
  /** The defender's stat the hit's defense was read from. Null when no damage was calculated. */
  val defenseStat: BoostableStat? = null,
) {
  val minDamage: Int get() = rolls.first()
  val maxDamage: Int get() = rolls.last()

  companion object {
    fun noDamage(typeEffectiveness: Double = 1.0, isCritical: Boolean = false) =
      DamageResult(rolls = listOf(0), typeEffectiveness = typeEffectiveness, isCritical = isCritical)
  }
}
