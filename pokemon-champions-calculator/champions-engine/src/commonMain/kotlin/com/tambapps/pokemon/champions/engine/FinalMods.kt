package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Item
import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.data.MoveCategory
import com.tambapps.pokemon.champions.engine.description.CalcFactsBuilder
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
    facts: CalcFactsBuilder = CalcFactsBuilder(),
    defenderAbility: Ability = defender.resolvedAbility,
  ): List<Int> {
    val effectiveCategory = effectiveCategoryOf(move, attacker, defender)
    val mods = mutableListOf<Int>()

    screenMod(move, effectiveCategory, field, isCritical, facts)?.let(mods::add)
    if (attacker.resolvedAbility == Ability.SNIPER && isCritical) {
      mods.add(0x1800)
      facts.attackerAbility(attacker.resolvedAbility)
    }
    if (defenderAbility == Ability.MULTISCALE && defender.hp == defender.maxHp) {
      mods.add(0x800)
      facts.defenderAbility(defenderAbility)
    }
    if ((defenderAbility == Ability.FLUFFY || defenderAbility == Ability.AURA_GUARD) && makesContact(move, attacker, effectiveCategory)) {
      mods.add(0x800)
      facts.defenderAbility(defenderAbility)
    }
    if (defenderAbility == Ability.PUNK_ROCK && move.isSound) {
      mods.add(0x800)
      facts.defenderAbility(defenderAbility)
    }
    // Mold Breaker ignores the ally's Friend Guard too
    if (field.defenderSide.hasFriendGuard && attacker.resolvedAbility != Ability.MOLD_BREAKER) {
      mods.add(0xC00)
      facts.isFriendGuard = true
    }
    if ((defenderAbility == Ability.SOLID_ROCK || defenderAbility == Ability.FILTER) && typeEffectiveness > 1) {
      mods.add(0xC00)
      facts.defenderAbility(defenderAbility)
    }
    if (defenderAbility == Ability.FLUFFY && effectiveType == PokeType.FIRE) {
      mods.add(0x2000)
      facts.defenderAbility(defenderAbility)
    }

    itemMod(attacker, typeEffectiveness)?.let {
      mods.add(it)
      facts.attackerItem(attacker.effectiveItem)
    }
    resistBerryMod(effectiveType, attacker, defender, typeEffectiveness, defenderAbility)?.let {
      mods.add(it)
      if (defenderAbility == Ability.RIPEN) facts.defenderAbility(defenderAbility)
      facts.defenderItem(defender.effectiveItem)
      facts.consumedResistBerry = true
    }

    return mods
  }

  // unlike Reflect and Aurora Veil, the source doesn't check the screen-breaking moves for Light Screen (all of them are physical)
  private fun screenMod(move: Move, effectiveCategory: MoveCategory, field: Battlefield, isCritical: Boolean, facts: CalcFactsBuilder): Int? {
    if (isCritical) return null
    when {
      field.defenderSide.hasAuroraVeil && !move.ignoresScreens -> facts.screen = Screen.AURORA_VEIL
      field.defenderSide.hasReflect && effectiveCategory == MoveCategory.PHYSICAL && !move.ignoresScreens -> facts.screen = Screen.REFLECT
      field.defenderSide.hasLightScreen && effectiveCategory == MoveCategory.SPECIAL -> facts.screen = Screen.LIGHT_SCREEN
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

  private fun resistBerryMod(effectiveType: PokeType, attacker: BattlePokemon, defender: BattlePokemon, typeEffectiveness: Double, defenderAbility: Ability): Int? {
    val item = defender.effectiveItem
    val resists = resistsType(item, effectiveType) && (typeEffectiveness > 1 || effectiveType == PokeType.NORMAL)
    if (!resists || attacker.resolvedAbility == Ability.UNNERVE) return null
    return if (defenderAbility == Ability.RIPEN) 0x400 else 0x800
  }
}
