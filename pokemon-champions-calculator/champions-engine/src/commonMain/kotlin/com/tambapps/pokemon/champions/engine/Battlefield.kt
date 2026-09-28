package com.tambapps.pokemon.champions.engine

enum class BattleFormat { SINGLES, DOUBLES }

enum class Weather { NONE, SUN, RAIN, SAND, HAIL, SNOW }

enum class Terrain { NONE, ELECTRIC, GRASSY, MISTY, PSYCHIC }

/**
 * Field conditions that belong to one side of the field rather than the whole battlefield. Like the source
 * calculator's toggles, the end-of-turn states ([isLeechSeeded], [isSaltCured], [isCursed], [isBound], [hasAquaRing],
 * [isIngrained]) are the side's: the KO chance applies the defender side's to the defender.
 */
data class SideConditions(
  val hasTailwind: Boolean = false,
  val hasReflect: Boolean = false,
  val hasLightScreen: Boolean = false,
  val hasAuroraVeil: Boolean = false,
  val hasFriendGuard: Boolean = false,
  val hasBattery: Boolean = false,
  val hasPowerSpot: Boolean = false,
  val hasAllySteelySpirit: Boolean = false,
  val hasHelpingHand: Boolean = false,
  val isProtected: Boolean = false,
  /** Spikes damage on switch-in, which the KO chance counts. */
  val spikesLayers: Int = 0,
  /** Stealth Rock damage on switch-in, which the KO chance counts. */
  val hasStealthRock: Boolean = false,
  /** Loses 1/8 of its max HP at the end of each turn. */
  val isLeechSeeded: Boolean = false,
  /** Loses 1/16 of its max HP at the end of each turn, 1/8 for a Water or Steel type. */
  val isSaltCured: Boolean = false,
  /** Loses 1/4 of its max HP at the end of each turn (a Ghost-type's Curse). */
  val isCursed: Boolean = false,
  /** Trapped by a binding move (Bind, Wrap, Fire Spin...): loses 1/8 of its max HP at the end of each turn, 1/6 against a Binding Band. */
  val isBound: Boolean = false,
  /** Recovers 1/16 of its max HP at the end of each turn, more with a Big Root. */
  val hasAquaRing: Boolean = false,
  /** Recovers 1/16 of its max HP at the end of each turn, more with a Big Root, and is grounded. */
  val isIngrained: Boolean = false,
) {
  init {
    requireValid(spikesLayers in 0..3) { "Spikes has at most 3 layers, got $spikesLayers" }
  }

  companion object {
    val NONE = SideConditions()
  }
}

/**
 * Battle conditions for a single damage calculation. Champions is always played in Doubles,
 * so [format] defaults to that, but it can be overridden for a Singles matchup.
 */
data class Battlefield(
  val format: BattleFormat = BattleFormat.DOUBLES,
  val weather: Weather = Weather.NONE,
  val terrain: Terrain = Terrain.NONE,
  val isGravity: Boolean = false,
  /** Electromorphosis/Wind Power's field-wide trigger (e.g. after Thunder Wave / a Charge user). */
  val isCharge: Boolean = false,
  /** A Fairy Aura Pokemon is on the field: every Fairy-type move's power is boosted, whoever uses it. */
  val isFairyAura: Boolean = false,
  val attackerSide: SideConditions = SideConditions.NONE,
  val defenderSide: SideConditions = SideConditions.NONE,
)
