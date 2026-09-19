package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Item
import com.tambapps.pokemon.champions.data.Move

/** Ported from immunityChecks, scoped to abilities/items/moves reachable in Champions. */
object ImmunityChecker {

  private val EXPLOSIVE_MOVES = setOf("Self-Destruct", "Explosion", "Misty Explosion")

  /** True if [defender] takes no damage at all from [move] under [field], regardless of the raw damage roll. */
  fun isImmune(move: Move, effectiveType: PokeType, attacker: BattlePokemon, defender: BattlePokemon, field: Battlefield): Boolean {
    if (TypeEffectivenessCalculator.effectivenessOf(move, effectiveType, defender, field) == 0.0) return true
    if (typeImmunityAbilityBlocks(effectiveType, defender)) return true
    if (groundImmunityBlocks(effectiveType, defender, field)) return true
    if (move.isBullet && defender.resolvedAbility == Ability.BULLETPROOF) return true
    if (move.isSound && defender.resolvedAbility == Ability.SOUNDPROOF) return true
    if (move.name.value in EXPLOSIVE_MOVES && (defender.resolvedAbility == Ability.DAMP || attacker.resolvedAbility == Ability.DAMP)) return true
    if (move.isOHKO && defender.resolvedAbility == Ability.STURDY) return true
    if (blockedByPriorityImmunity(move, attacker, defender, field)) return true
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

  private fun groundImmunityBlocks(effectiveType: PokeType, defender: BattlePokemon, field: Battlefield): Boolean {
    if (effectiveType != PokeType.GROUND) return false
    if (field.isGravity) return false
    if (defender.resolvedAbility == Ability.LEVITATE || defender.resolvedAbility == Ability.EELEVATE) return true
    return defender.effectiveItem == Item.AIR_BALLOON
  }

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
