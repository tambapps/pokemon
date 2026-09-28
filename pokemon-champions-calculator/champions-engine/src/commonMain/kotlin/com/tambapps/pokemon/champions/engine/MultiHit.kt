package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.HitCount
import com.tambapps.pokemon.champions.data.Move

/** How many times [this] move can hit in a single use, e.g. 2..5 for Bullet Seed, 1..1 for a single-hit move. */
val Move.hitCountRange: IntRange get() = when (val count = hitCount) {
  is HitCount.Fixed -> count.hits..count.hits
  is HitCount.Variable -> count.min..count.max
}

/**
 * The number of hits the source calculator assumes by default for [move], ported from showHits:
 * 3 for 2-5 hit moves (5 with Skill Link; Loaded Dice's 4 doesn't apply, it isn't a Champions item),
 * 1 for Dragon Darts, 4 for Beat Up, the maximum for any other variable hit count (e.g. Triple Axel,
 * Population Bomb).
 */
fun defaultHitCount(move: Move, attacker: BattlePokemon): Int {
  val range = move.hitCountRange
  return when {
    range == 2..5 -> if (attacker.resolvedAbility == Ability.SKILL_LINK) 5 else 3
    range == 1..2 -> 1
    range == 1..6 -> 4
    else -> range.last
  }
}

/**
 * The damage of every hit of one use of a move: one hit for a single-hit move, two for a Parental Bond one.
 *
 * @param description the source calculator's description of the calc, e.g. "20+ Atk Garchomp Bullet Seed (3 hits)
 * vs. 20 HP  / 20+ Def Toxapex": the first hit's [DamageResult.description] plus the number of hits and Parental Bond
 */
data class MoveDamageResult(val hits: List<DamageResult>, val description: String = "") {
  val minDamage: Int get() = hits.sumOf { it.minDamage }
  val maxDamage: Int get() = hits.sumOf { it.maxDamage }
}
