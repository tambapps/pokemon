package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Item
import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.data.MoveCategory
import com.tambapps.pokemon.champions.engine.description.DescriptionBuilder
import com.tambapps.pokemon.champions.engine.description.Screen

/** Ported from calcFinalMods, scoped to Champions. */
internal object FinalMods {

  fun resolve(
    move: Move,
    effectiveType: PokeType,
    attacker: BattlePokemon,
    defender: BattlePokemon,
    field: Battlefield,
    isCritical: Boolean,
    typeEffectiveness: Double,
    description: DescriptionBuilder = DescriptionBuilder(),
  ): List<Int> {
    val effectiveCategory = effectiveCategoryOf(move, attacker, defender)
    val mods = mutableListOf<Int>()

    screenMod(move, effectiveCategory, field, isCritical, description)?.let(mods::add)
    if (attacker.resolvedAbility == Ability.SNIPER && isCritical) {
      mods.add(0x1800)
      description.attackerAbility(attacker.resolvedAbility)
    }
    if (defender.resolvedAbility == Ability.MULTISCALE && defender.hp == defender.maxHp) {
      mods.add(0x800)
      description.defenderAbility(defender.resolvedAbility)
    }
    if ((defender.resolvedAbility == Ability.FLUFFY || defender.resolvedAbility == Ability.AURA_GUARD) && move.makesContact) {
      mods.add(0x800)
      description.defenderAbility(defender.resolvedAbility)
    }
    if (defender.resolvedAbility == Ability.PUNK_ROCK && move.isSound) {
      mods.add(0x800)
      description.defenderAbility(defender.resolvedAbility)
    }
    if (field.defenderSide.hasFriendGuard) {
      mods.add(0xC00)
      description.isFriendGuard = true
    }
    if ((defender.resolvedAbility == Ability.SOLID_ROCK || defender.resolvedAbility == Ability.FILTER) && typeEffectiveness > 1) {
      mods.add(0xC00)
      description.defenderAbility(defender.resolvedAbility)
    }
    if (defender.resolvedAbility == Ability.FLUFFY && effectiveType == PokeType.FIRE) {
      mods.add(0x2000)
      description.defenderAbility(defender.resolvedAbility)
    }

    itemMod(attacker, typeEffectiveness)?.let {
      mods.add(it)
      description.attackerItem(attacker.effectiveItem)
    }
    resistBerryMod(effectiveType, attacker, defender, typeEffectiveness)?.let {
      mods.add(it)
      if (defender.resolvedAbility == Ability.RIPEN) description.defenderAbility(defender.resolvedAbility)
      description.defenderItem(defender.effectiveItem)
    }

    return mods
  }

  private fun screenMod(move: Move, effectiveCategory: MoveCategory, field: Battlefield, isCritical: Boolean, description: DescriptionBuilder): Int? {
    if (isCritical || move.ignoresScreens) return null
    when {
      field.defenderSide.hasAuroraVeil -> description.screen = Screen.AURORA_VEIL
      field.defenderSide.hasReflect && effectiveCategory == MoveCategory.PHYSICAL -> description.screen = Screen.REFLECT
      field.defenderSide.hasLightScreen && effectiveCategory == MoveCategory.SPECIAL -> description.screen = Screen.LIGHT_SCREEN
      else -> return null
    }
    return if (field.format != BattleFormat.SINGLES) 0xAAC else 0x800
  }

  private fun itemMod(attacker: BattlePokemon, typeEffectiveness: Double): Int? {
    val item = attacker.effectiveItem
    return when {
      item == Item.EXPERT_BELT && typeEffectiveness > 1 -> 0x1333
      item == Item.LIFE_ORB -> 0x14CC
      else -> null
    }
  }

  private fun resistBerryMod(effectiveType: PokeType, attacker: BattlePokemon, defender: BattlePokemon, typeEffectiveness: Double): Int? {
    val item = defender.effectiveItem
    val resists = resistsType(item, effectiveType) && (typeEffectiveness > 1 || effectiveType == PokeType.NORMAL)
    if (!resists || attacker.resolvedAbility == Ability.UNNERVE) return null
    return if (defender.resolvedAbility == Ability.RIPEN) 0x400 else 0x800
  }
}
