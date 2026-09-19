package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.champions.data.Ability

/** Ported from calcDefMods, scoped to Champions -- most defensive items/abilities the games have (Eviolite, Assault Vest, Soul Dew...) aren't legal here. */
internal object DefenseStatMods {

  fun resolve(defender: BattlePokemon, field: Battlefield, hitsPhysical: Boolean): List<Int> {
    val mods = mutableListOf<Int>()

    val oneAndHalf = (defender.resolvedAbility == Ability.MARVEL_SCALE && defender.status.isNonHealthy && hitsPhysical) ||
      (defender.resolvedAbility == Ability.GRASS_PELT && field.terrain == Terrain.GRASSY && hitsPhysical)
    if (oneAndHalf) mods.add(0x1800)

    if (defender.resolvedAbility == Ability.FUR_COAT && hitsPhysical) mods.add(0x2000)

    return mods
  }
}
