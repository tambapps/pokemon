package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.engine.description.CalcFactsBuilder

/** Ported from calcDefMods, scoped to Champions -- most defensive items/abilities the games have (Eviolite, Assault Vest, Soul Dew...) aren't legal here. */
internal object DefenseStatMods {

  fun resolve(
    defender: BattlePokemon,
    field: Battlefield,
    hitsPhysical: Boolean,
    facts: CalcFactsBuilder = CalcFactsBuilder(),
    defenderAbility: Ability = defender.resolvedAbility,
  ): List<Int> {
    val mods = mutableListOf<Int>()

    val oneAndHalf = (defenderAbility == Ability.MARVEL_SCALE && defender.status.isNonHealthy && hitsPhysical) ||
      (defenderAbility == Ability.GRASS_PELT && field.terrain == Terrain.GRASSY && hitsPhysical)
    if (oneAndHalf) {
      mods.add(0x1800)
      facts.defenderAbility(defenderAbility)
    } else if (defenderAbility == Ability.FUR_COAT && hitsPhysical) {
      mods.add(0x2000)
      facts.defenderAbility(defenderAbility)
    }

    return mods
  }
}
