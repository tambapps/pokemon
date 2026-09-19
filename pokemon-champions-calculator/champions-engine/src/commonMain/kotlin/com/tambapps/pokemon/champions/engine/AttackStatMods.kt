package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Item
import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.data.MoveCategory

/** Ported from calcAtMods, scoped to Champions. */
internal object AttackStatMods {

  fun resolve(move: Move, effectiveType: PokeType, attacker: BattlePokemon, defender: BattlePokemon, field: Battlefield): List<Int> {
    val category = effectiveCategoryOf(move, attacker, defender)
    val mods = mutableListOf<Int>()

    offensiveOneAndHalfMod(move, effectiveType, attacker, category)?.let(mods::add)
      ?: solarPowerMod(attacker, category, field)?.let(mods::add)

    defensiveHalfMod(effectiveType, defender)?.let(mods::add)
    offensiveDoubleMod(effectiveType, attacker, category)?.let(mods::add)
    itemDoubleMod(attacker)?.let(mods::add)

    return mods
  }

  private fun offensiveOneAndHalfMod(move: Move, effectiveType: PokeType, attacker: BattlePokemon, category: MoveCategory): Int? {
    val belowThirdHp = attacker.hp <= attacker.maxHp / 3
    val qualifies = (attacker.resolvedAbility == Ability.GUTS && attacker.status.isNonHealthy && category == MoveCategory.PHYSICAL) ||
      (attacker.resolvedAbility == Ability.OVERGROW && belowThirdHp && effectiveType == PokeType.GRASS) ||
      (attacker.resolvedAbility == Ability.BLAZE && belowThirdHp && effectiveType == PokeType.FIRE) ||
      (attacker.resolvedAbility == Ability.TORRENT && belowThirdHp && effectiveType == PokeType.WATER) ||
      (attacker.resolvedAbility == Ability.SWARM && belowThirdHp && effectiveType == PokeType.BUG) ||
      (attacker.resolvedAbility == Ability.FLASH_FIRE && attacker.abilityIsActive && effectiveType == PokeType.FIRE) ||
      (attacker.resolvedAbility == Ability.SHARPNESS && move.isSlice) ||
      (attacker.resolvedAbility == Ability.FIRE_MANE && effectiveType == PokeType.FIRE) ||
      ((attacker.resolvedAbility == Ability.PLUS || attacker.resolvedAbility == Ability.MINUS) && attacker.abilityIsActive && category == MoveCategory.SPECIAL)
    return if (qualifies) 0x1800 else null
  }

  private fun solarPowerMod(attacker: BattlePokemon, category: MoveCategory, field: Battlefield): Int? {
    val qualifies = attacker.resolvedAbility == Ability.SOLAR_POWER && field.weather == Weather.SUN && category == MoveCategory.SPECIAL
    return if (qualifies) 0x1800 else null
  }

  private fun defensiveHalfMod(effectiveType: PokeType, defender: BattlePokemon): Int? {
    val qualifies = (defender.resolvedAbility == Ability.THICK_FAT && (effectiveType == PokeType.FIRE || effectiveType == PokeType.ICE)) ||
      (defender.resolvedAbility == Ability.WATER_BUBBLE && effectiveType == PokeType.FIRE) ||
      (defender.resolvedAbility == Ability.PURIFYING_SALT && effectiveType == PokeType.GHOST) ||
      (defender.resolvedAbility == Ability.HEATPROOF && effectiveType == PokeType.FIRE)
    return if (qualifies) 0x800 else null
  }

  private fun offensiveDoubleMod(effectiveType: PokeType, attacker: BattlePokemon, category: MoveCategory): Int? {
    val qualifies = (attacker.resolvedAbility == Ability.WATER_BUBBLE && effectiveType == PokeType.WATER) ||
      ((attacker.resolvedAbility == Ability.HUGE_POWER || attacker.resolvedAbility == Ability.PURE_POWER) && category == MoveCategory.PHYSICAL) ||
      (attacker.resolvedAbility == Ability.STAKEOUT && attacker.abilityIsActive)
    return if (qualifies) 0x2000 else null
  }

  private fun itemDoubleMod(attacker: BattlePokemon): Int? {
    val isPikachu = attacker.species.name.value == "Pikachu" || attacker.species.name.value == "Pikachu-Gmax"
    return if (attacker.effectiveItem == Item.LIGHT_BALL && isPikachu) 0x2000 else null
  }
}
