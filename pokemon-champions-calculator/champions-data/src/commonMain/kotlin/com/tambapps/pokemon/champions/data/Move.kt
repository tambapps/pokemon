package com.tambapps.pokemon.champions.data

import com.tambapps.pokemon.MoveName
import com.tambapps.pokemon.PokeType

/**
 * Static move data: the numbers and flags the damage engine needs to reproduce the game's
 * formula. This intentionally excludes anything that only matters for non-damage mechanics
 * (accuracy, PP, status-effect chances, targeting) since this library only computes damage.
 */
data class Move(
  val name: MoveName,
  val type: PokeType,
  val category: MoveCategory,
  val basePower: Int,
  val hitCount: HitCount = HitCount.Once,
  /** Punches, Iron Fist, Long Reach, Protective Pads, Fluffy, Tough Claws all key off this. */
  val makesContact: Boolean = false,
  /** Punk Rock, Soundproof, Liquid Voice. */
  val isSound: Boolean = false,
  /** Bulletproof. */
  val isBullet: Boolean = false,
  /** Mega Launcher. */
  val isPulse: Boolean = false,
  /** Strong Jaw. */
  val isBite: Boolean = false,
  /** Iron Fist, Punching Glove. */
  val isPunch: Boolean = false,
  /** Sharpness. */
  val isSlice: Boolean = false,
  /** Hits both opponents in Doubles for 0.75x damage instead of the full amount. */
  val isSpread: Boolean = false,
  /** Fissure, Guillotine, Horn Drill, Sheer Cold: bypassed by Sturdy. */
  val isOHKO: Boolean = false,
  /** Sheer Force disables the move's secondary effect but boosts its power by 1.3x. */
  val hasSecondaryEffect: Boolean = false,
  /** Facade: not halved by the user's own burn. */
  val ignoresBurn: Boolean = false,
  /** Sacred Sword-style moves that hit through the defender's positive Defense/Sp. Def boosts. */
  val ignoresDefenseBoosts: Boolean = false,
  /** Raging Bull-style moves that break through Reflect/Light Screen/Aurora Veil. */
  val ignoresScreens: Boolean = false,
  /** Psyshock-style specials that roll damage off the target's Defense instead of Sp. Def. */
  val dealsPhysicalDamage: Boolean = false,
  /** Storm Throw, Frost Breath: always a critical hit. */
  val alwaysCrits: Boolean = false,
  /** Triple Axel/Triple Kick: base power increases with each successive hit of the combo. */
  val hasEscalatingPower: Boolean = false,
  /** Payback-style moves whose power doubles under a move-specific condition the caller resolves and reports via the move use. */
  val canBePowerDoubled: Boolean = false,
  /** Queenly Majesty and Armor Tail block any move with positive priority outright. */
  val hasPriority: Boolean = false,
  /** Reckless boosts moves that hurt their own user on use (recoil) or on a miss (crash, e.g. Jump Kick). */
  val hasRecoil: Boolean = false,
)
