package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.champions.data.Move

/**
 * One use of a [move], with the situational details the static move data can't capture:
 * whether it crits, which hit of a combo it is, and the handful of moves whose power depends
 * on a manual toggle or on the battle's history rather than on either Pokemon's current state.
 */
data class MoveUse(
  val move: Move,
  val isCritical: Boolean = false,
  /** 1-based index of this hit within a multi-hit move's combo; only matters for Triple Axel/Triple Kick's escalating power. */
  val hitNumber: Int = 1,
  /** Manual toggle for [Move.canBePowerDoubled] moves (Payback, Avalanche, Round, Stomping Tantrum, Temper Flare...): true if their doubling condition is met. */
  val isPowerDoubled: Boolean = false,
  /** Last Respects/Rage Fist: how many times the effect has already stacked this battle. */
  val priorPowerBoosts: Int = 0,
  /** Supreme Overlord: how many of the attacker's team members have already fainted this battle. */
  val faintedAllyCount: Int = 0,
  /** Parental Bond hits twice; pass true for the second, weaker hit. See [DamageCalculator.calculateParentalBondHits]. */
  val isSecondParentalBondHit: Boolean = false,
) {
  init {
    require(hitNumber >= 1) { "hitNumber is 1-based, got $hitNumber" }
    require(priorPowerBoosts >= 0) { "priorPowerBoosts can't be negative, got $priorPowerBoosts" }
    require(faintedAllyCount in 0..5) { "faintedAllyCount must be within 0..5, got $faintedAllyCount" }
  }
}
