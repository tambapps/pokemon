package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Item
import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.engine.description.CalcFactsBuilder

/** Ported from immunityChecks, scoped to abilities/items/moves reachable in Champions. */
object ImmunityChecker {

  private val EXPLOSIVE_MOVES = setOf("Self-Destruct", "Explosion", "Misty Explosion")

  /** True if [defender] takes no damage at all from [move] under [field], regardless of the raw damage roll. */
  fun isImmune(move: Move, effectiveType: PokeType, attacker: BattlePokemon, defender: BattlePokemon, field: Battlefield): Boolean =
    isImmune(move, effectiveType, attacker, defender, field, CalcFactsBuilder())

  /**
   * Also records what the immunity comes from in [facts], like the source's immunityChecks. [defenderAbility] is the
   * defender's ability as the move sees it: none when the attacker's Mold Breaker ignores it.
   */
  internal fun isImmune(
    move: Move,
    effectiveType: PokeType,
    attacker: BattlePokemon,
    defender: BattlePokemon,
    field: Battlefield,
    facts: CalcFactsBuilder,
    defenderAbility: Ability = defender.resolvedAbility,
  ): Boolean {
    if (TypeEffectivenessCalculator.effectivenessOf(move, effectiveType, attacker, defender, field, facts) == 0.0) return true
    val levitates = (defenderAbility == Ability.LEVITATE || defenderAbility == Ability.EELEVATE) && defender.effectiveItem != Item.IRON_BALL
    val blockingAbility = typeImmunityAbilityBlocks(effectiveType, defenderAbility) ||
      (effectiveType == PokeType.GROUND && !field.isGravity && levitates) ||
      (move.isBullet && defenderAbility == Ability.BULLETPROOF) ||
      (move.isSound && defenderAbility == Ability.SOUNDPROOF)
    if (blockingAbility) {
      facts.defenderAbility(defenderAbility)
      return true
    }
    if (airBalloonBlocks(effectiveType, defender, field)) {
      facts.defenderItem(defender.effectiveItem)
      return true
    }
    if (move.name.value in EXPLOSIVE_MOVES && (defenderAbility == Ability.DAMP || attacker.resolvedAbility == Ability.DAMP)) {
      if (defenderAbility == Ability.DAMP) facts.defenderAbility(defenderAbility)
      if (attacker.resolvedAbility == Ability.DAMP) facts.attackerAbility(attacker.resolvedAbility)
      return true
    }
    if (move.isOHKO && defenderAbility == Ability.STURDY) {
      facts.defenderAbility(defenderAbility)
      return true
    }
    if (move.name.value == FLING && Fling.cantFling(attacker, defenderAbility)) {
      facts.attackerItem(attacker.effectiveItem)
      return true
    }
    if (hasEffectivePriority(move, attacker, field)) {
      if (defenderAbility == Ability.QUEENLY_MAJESTY || defenderAbility == Ability.ARMOR_TAIL) {
        facts.defenderAbility(defenderAbility)
        return true
      }
      if (field.terrain == Terrain.PSYCHIC && defender.isGrounded(field)) {
        facts.terrain(field.terrain)
        return true
      }
    }
    return false
  }

  private fun typeImmunityAbilityBlocks(effectiveType: PokeType, defenderAbility: Ability): Boolean = when {
    effectiveType == PokeType.GRASS && defenderAbility == Ability.SAP_SIPPER -> true
    effectiveType == PokeType.FIRE && defenderAbility == Ability.FLASH_FIRE -> true
    effectiveType == PokeType.WATER && (defenderAbility == Ability.DRY_SKIN || defenderAbility == Ability.WATER_ABSORB) -> true
    effectiveType == PokeType.ELECTRIC && (defenderAbility == Ability.MOTOR_DRIVE || defenderAbility == Ability.VOLT_ABSORB || defenderAbility == Ability.LIGHTNING_ROD) -> true
    effectiveType == PokeType.GROUND && defenderAbility == Ability.EARTH_EATER -> true
    else -> false
  }

  private fun airBalloonBlocks(effectiveType: PokeType, defender: BattlePokemon, field: Battlefield): Boolean =
    effectiveType == PokeType.GROUND && !field.isGravity && defender.effectiveItem == Item.AIR_BALLOON
}
