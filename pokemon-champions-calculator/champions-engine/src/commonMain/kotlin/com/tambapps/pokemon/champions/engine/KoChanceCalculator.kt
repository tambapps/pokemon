package com.tambapps.pokemon.champions.engine

/**
 * How likely a move is to knock out a target within a given number of uses, ported from the
 * probability core of ko_chance.js -- the part that convolves the 16 damage rolls across
 * repeated hits. Unlike the source calculator, this does NOT factor in between-turn effects
 * (residual damage from weather/status/hazards, or HP recovered from Leftovers/berries): see
 * the module README for why that's out of scope for now.
 */
object KoChanceCalculator {

  /**
   * The probability (0.0-1.0) that [hits] independent uses of a move -- each rolling
   * uniformly among [perHitRolls] -- add up to at least [targetHp] damage.
   */
  fun koChance(perHitRolls: List<Int>, hits: Int, targetHp: Int): Double {
    requireValid(perHitRolls.isNotEmpty()) { "perHitRolls must not be empty" }
    requireValid(hits >= 1) { "hits must be at least 1, got $hits" }

    var totalDamageDistribution = mapOf(0 to 1.0)
    repeat(hits) {
      totalDamageDistribution = convolve(totalDamageDistribution, perHitRolls)
    }
    return totalDamageDistribution.filterKeys { it >= targetHp }.values.sum()
  }

  /** The fewest number of uses (up to [maxHits]) that has any chance at all of scoring the KO, or null if none do. */
  fun minimumHitsToKo(perHitRolls: List<Int>, targetHp: Int, maxHits: Int = 4): KoChanceResult? {
    for (hits in 1..maxHits) {
      val chance = koChance(perHitRolls, hits, targetHp)
      if (chance > 0.0) return KoChanceResult(hits, chance)
    }
    return null
  }

  /**
   * Like [minimumHitsToKo], but counting uses of a whole move, whose hits can each roll differently
   * (a multi-hit move, Triple Axel's escalating power, Parental Bond's weaker second hit). The
   * returned [KoChanceResult.hits] is a number of uses of the move.
   */
  fun minimumUsesToKo(move: MoveDamageResult, targetHp: Int, maxUses: Int = 4): KoChanceResult? {
    requireValid(move.hits.isNotEmpty()) { "a move has at least one hit" }
    val perUseDistribution = move.hits.fold(mapOf(0 to 1.0)) { distribution, hit -> convolve(distribution, hit.rolls) }
    var totalDamageDistribution = mapOf(0 to 1.0)
    for (uses in 1..maxUses) {
      totalDamageDistribution = convolveDistributions(totalDamageDistribution, perUseDistribution)
      val chance = totalDamageDistribution.filterKeys { it >= targetHp }.values.sum()
      if (chance > 0.0) return KoChanceResult(uses, chance)
    }
    return null
  }

  private fun convolveDistributions(first: Map<Int, Double>, second: Map<Int, Double>): Map<Int, Double> {
    val combined = mutableMapOf<Int, Double>()
    for ((firstTotal, firstProbability) in first) {
      for ((secondTotal, secondProbability) in second) {
        val total = firstTotal + secondTotal
        combined[total] = (combined[total] ?: 0.0) + firstProbability * secondProbability
      }
    }
    return combined
  }

  private fun convolve(damageSoFar: Map<Int, Double>, nextHitRolls: List<Int>): Map<Int, Double> {
    val rollProbability = 1.0 / nextHitRolls.size
    val combined = mutableMapOf<Int, Double>()
    for ((priorTotal, priorProbability) in damageSoFar) {
      for (roll in nextHitRolls) {
        val total = priorTotal + roll
        combined[total] = (combined[total] ?: 0.0) + priorProbability * rollProbability
      }
    }
    return combined
  }
}

/** [hits] uses of a move have a [chance] (0.0-1.0) of scoring the KO; 1.0 means guaranteed. */
data class KoChanceResult(val hits: Int, val chance: Double) {
  val isGuaranteed: Boolean get() = chance >= 1.0
}
