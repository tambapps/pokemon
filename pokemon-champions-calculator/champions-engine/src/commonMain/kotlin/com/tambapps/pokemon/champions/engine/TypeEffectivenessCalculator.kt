package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.ChampionsDex
import com.tambapps.pokemon.champions.data.Item
import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.engine.description.CalcFactsBuilder

/** Ported from getMoveEffectiveness/getSingleTypeEffectiveness, scoped to mechanics reachable in Champions (no Tera, no Ring Target/Thousand Arrows edge items). */
object TypeEffectivenessCalculator {

  /** [effectiveType] is the move's type after Weather Ball/Terrain Pulse/-ate-ability resolution -- see [effectiveTypeOf]. */
  fun effectivenessOf(move: Move, effectiveType: PokeType, attacker: BattlePokemon, defender: BattlePokemon, field: Battlefield): Double =
    effectivenessOf(move, effectiveType, attacker, defender, field, CalcFactsBuilder())

  internal fun effectivenessOf(
    move: Move,
    effectiveType: PokeType,
    attacker: BattlePokemon,
    defender: BattlePokemon,
    field: Battlefield,
    facts: CalcFactsBuilder,
  ): Double {
    // Struggle is Typeless: always neutral, no immunities, regardless of the defender's types.
    if (move.name.value == STRUGGLE_MOVE_NAME) return 1.0

    val primary = singleTypeEffectiveness(move, effectiveType, defender.species.primaryType, attacker, defender, field, facts)
    val secondary = defender.species.secondaryType
      ?.takeIf { it != defender.species.primaryType }
      ?.let { singleTypeEffectiveness(move, effectiveType, it, attacker, defender, field, facts) }
      ?: 1.0
    return primary * secondary
  }

  private fun singleTypeEffectiveness(
    move: Move,
    effectiveType: PokeType,
    defendingType: PokeType,
    attacker: BattlePokemon,
    defender: BattlePokemon,
    field: Battlefield,
    facts: CalcFactsBuilder,
  ): Double {
    // Scrappy/Mind's Eye are the ATTACKER's ability: they let normally-blocked Normal/Fighting moves hit Ghosts.
    val bypassesGhostImmunity = attacker.resolvedAbility == Ability.SCRAPPY &&
      defendingType == PokeType.GHOST &&
      (effectiveType == PokeType.NORMAL || effectiveType == PokeType.FIGHTING)
    if (bypassesGhostImmunity) {
      facts.attackerAbility(attacker.resolvedAbility)
      return 1.0
    }

    val groundHitsFlying = (field.isGravity || defender.effectiveItem == Item.IRON_BALL) &&
      defendingType == PokeType.FLYING &&
      effectiveType == PokeType.GROUND
    if (groundHitsFlying) {
      if (field.isGravity) facts.isGravity = true else facts.defenderItem(defender.effectiveItem)
      return 1.0
    }

    if (move.name.value == "Freeze-Dry" && defendingType == PokeType.WATER) return 2.0

    var effectiveness = ChampionsDex.typeChart.effectivenessOf(effectiveType, defendingType)
    if (move.name.value == "Flying Press") {
      effectiveness *= ChampionsDex.typeChart.effectivenessOf(PokeType.FLYING, defendingType)
    }
    return effectiveness
  }
}
