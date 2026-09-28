package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Item
import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.engine.description.DescriptionBuilder

/** Ported from immunityChecks, scoped to abilities/items/moves reachable in Champions. */
object ImmunityChecker {

  private val EXPLOSIVE_MOVES = setOf("Self-Destruct", "Explosion", "Misty Explosion")

  /** True if [defender] takes no damage at all from [move] under [field], regardless of the raw damage roll. */
  fun isImmune(move: Move, effectiveType: PokeType, attacker: BattlePokemon, defender: BattlePokemon, field: Battlefield): Boolean =
    isImmune(move, effectiveType, attacker, defender, field, DescriptionBuilder())

  /** Also records what the immunity comes from in [description], like the source's immunityChecks. */
  internal fun isImmune(
    move: Move,
    effectiveType: PokeType,
    attacker: BattlePokemon,
    defender: BattlePokemon,
    field: Battlefield,
    description: DescriptionBuilder,
  ): Boolean {
    if (TypeEffectivenessCalculator.effectivenessOf(move, effectiveType, attacker, defender, field, description) == 0.0) return true
    val blockingAbility = typeImmunityAbilityBlocks(effectiveType, defender) ||
      (effectiveType == PokeType.GROUND && !field.isGravity && (defender.resolvedAbility == Ability.LEVITATE || defender.resolvedAbility == Ability.EELEVATE)) ||
      (move.isBullet && defender.resolvedAbility == Ability.BULLETPROOF) ||
      (move.isSound && defender.resolvedAbility == Ability.SOUNDPROOF)
    if (blockingAbility) {
      description.defenderAbility(defender.resolvedAbility)
      return true
    }
    if (airBalloonBlocks(effectiveType, defender, field)) {
      description.defenderItem(defender.effectiveItem)
      return true
    }
    if (move.name.value in EXPLOSIVE_MOVES && (defender.resolvedAbility == Ability.DAMP || attacker.resolvedAbility == Ability.DAMP)) {
      if (defender.resolvedAbility == Ability.DAMP) description.defenderAbility(defender.resolvedAbility)
      if (attacker.resolvedAbility == Ability.DAMP) description.attackerAbility(attacker.resolvedAbility)
      return true
    }
    if (move.isOHKO && defender.resolvedAbility == Ability.STURDY) {
      description.defenderAbility(defender.resolvedAbility)
      return true
    }
    if (blockedByPriorityImmunity(move, attacker, defender, field)) {
      if (defender.resolvedAbility == Ability.QUEENLY_MAJESTY || defender.resolvedAbility == Ability.ARMOR_TAIL) {
        description.defenderAbility(defender.resolvedAbility)
      } else {
        description.terrain(field.terrain)
      }
      return true
    }
    return false
  }

  private fun typeImmunityAbilityBlocks(effectiveType: PokeType, defender: BattlePokemon): Boolean = when {
    effectiveType == PokeType.GRASS && defender.resolvedAbility == Ability.SAP_SIPPER -> true
    effectiveType == PokeType.FIRE && defender.resolvedAbility == Ability.FLASH_FIRE -> true
    effectiveType == PokeType.WATER && (defender.resolvedAbility == Ability.DRY_SKIN || defender.resolvedAbility == Ability.WATER_ABSORB) -> true
    effectiveType == PokeType.ELECTRIC && (defender.resolvedAbility == Ability.MOTOR_DRIVE || defender.resolvedAbility == Ability.VOLT_ABSORB || defender.resolvedAbility == Ability.LIGHTNING_ROD) -> true
    effectiveType == PokeType.GROUND && defender.resolvedAbility == Ability.EARTH_EATER -> true
    else -> false
  }

  private fun airBalloonBlocks(effectiveType: PokeType, defender: BattlePokemon, field: Battlefield): Boolean =
    effectiveType == PokeType.GROUND && !field.isGravity && defender.effectiveItem == Item.AIR_BALLOON

  /** Grassy Glide gains priority only in Grassy Terrain while its user is grounded; every other priority move carries it statically. */
  private fun hasEffectivePriority(move: Move, attacker: BattlePokemon, field: Battlefield): Boolean =
    move.hasPriority || (move.name.value == "Grassy Glide" && field.terrain == Terrain.GRASSY && attacker.isGrounded(field))

  private fun blockedByPriorityImmunity(move: Move, attacker: BattlePokemon, defender: BattlePokemon, field: Battlefield): Boolean {
    val defenderIsProtected = defender.resolvedAbility == Ability.QUEENLY_MAJESTY ||
      defender.resolvedAbility == Ability.ARMOR_TAIL ||
      (field.terrain == Terrain.PSYCHIC && defender.isGrounded(field))
    return defenderIsProtected && hasEffectivePriority(move, attacker, field)
  }
}
