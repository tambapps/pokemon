package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.data.MoveCategory

/**
 * Shell Side Arm picks Physical or Special depending on which stat ratio favors the attacker
 * more; every other move's category is fixed. Ported from usesPhysicalAttack.
 */
fun effectiveCategoryOf(move: Move, attacker: BattlePokemon, defender: BattlePokemon): MoveCategory {
  if (move.name.value != "Shell Side Arm") return move.category

  val physicalRatio = attacker.boostedStat(BoostableStat.ATTACK).toDouble() / defender.boostedStat(BoostableStat.DEFENSE)
  val specialRatio = attacker.boostedStat(BoostableStat.SPECIAL_ATTACK).toDouble() / defender.boostedStat(BoostableStat.SPECIAL_DEFENSE)
  return if (physicalRatio > specialRatio) MoveCategory.PHYSICAL else MoveCategory.SPECIAL
}

/** Whether this hit rolls damage off the defender's Defense rather than Special Defense. */
fun hitsPhysicalDefense(move: Move, effectiveCategory: MoveCategory): Boolean =
  effectiveCategory == MoveCategory.PHYSICAL || move.dealsPhysicalDamage
