package com.tambapps.pokemon.champions.engine

enum class BattleFormat { SINGLES, DOUBLES }

enum class Weather { NONE, SUN, RAIN, SAND, HAIL, SNOW }

enum class Terrain { NONE, ELECTRIC, GRASSY, MISTY, PSYCHIC }

/** Field conditions that belong to one side of the field rather than the whole battlefield. */
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
  val spikesLayers: Int = 0,
  val hasStealthRock: Boolean = false,
) {
  init {
    require(spikesLayers in 0..3) { "Spikes has at most 3 layers, got $spikesLayers" }
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
  val attackerSide: SideConditions = SideConditions.NONE,
  val defenderSide: SideConditions = SideConditions.NONE,
)
