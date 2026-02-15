package com.tambapps.pokemon

import kotlin.test.Test
import kotlin.test.assertEquals

class PokeStatsTest {

  private val miraidonBaseStats = PokeStats(hp = 100, attack = 85, defense = 100, specialAttack = 135, specialDefense = 115, speed = 135)
  private val shedinjaBaseStats = PokeStats(hp = 1, attack = 90, defense = 45, specialAttack = 30, specialDefense = 30, speed = 40)

  @Test
  fun defaultCreatesUniformStats() {
    assertEquals(
      PokeStats(hp = 31, attack = 31, defense = 31, specialAttack = 31, specialDefense = 31, speed = 31),
      PokeStats.default(31)
    )
  }

  @Test
  fun buildStatsAppliesOverrides() {
    val stats = buildStats(0) {
      attack = 252
      speed = 252
      hp = 4
    }
    assertEquals(
      PokeStats(hp = 4, attack = 252, defense = 0, specialAttack = 0, specialDefense = 0, speed = 252),
      stats
    )
  }

  @Test
  fun buildStatsWithDefaultValue() {
    val stats = buildStats(31) {
      attack = 0
    }
    assertEquals(
      PokeStats(hp = 31, attack = 0, defense = 31, specialAttack = 31, specialDefense = 31, speed = 31),
      stats
    )
  }

  @Test
  fun getOperatorReturnsStat() {
    val stats = buildStats(0) {
      hp = 1
      attack = 2
      defense = 3
      specialAttack = 4
      specialDefense = 5
      speed = 6
    }
    assertEquals(1, stats[Stat.HP])
    assertEquals(2, stats[Stat.ATTACK])
    assertEquals(3, stats[Stat.DEFENSE])
    assertEquals(4, stats[Stat.SPECIAL_ATTACK])
    assertEquals(5, stats[Stat.SPECIAL_DEFENSE])
    assertEquals(6, stats[Stat.SPEED])
  }

  @Test
  fun computeWithNeutralNature() {
    val computed = PokeStats.compute(
      baseStats = miraidonBaseStats,
      evs = PokeStats.default(0),
      ivs = PokeStats.default(31),
      nature = Nature.QUIRKY,
      level = 50
    )
    assertEquals(
      PokeStats(hp = 175, attack = 105, defense = 120, specialAttack = 155, specialDefense = 135, speed = 155),
      computed
    )
  }

  @Test
  fun computeWithNatureBoostAndPenalty() {
    // MODEST: +SpAtk / -Atk
    val computed = PokeStats.compute(
      baseStats = miraidonBaseStats,
      evs = buildStats(0) { specialAttack = 252; speed = 252; specialDefense = 4 },
      ivs = PokeStats.default(31),
      nature = Nature.MODEST,
      level = 50
    )
    assertEquals(
      PokeStats(hp = 175, attack = 94, defense = 120, specialAttack = 205, specialDefense = 136, speed = 187),
      computed
    )
  }

  @Test
  fun computeShedinja() {
    val computed = PokeStats.compute(
      baseStats = shedinjaBaseStats,
      evs = buildStats(0) { attack = 252; speed = 252; hp = 4 },
      ivs = PokeStats.default(31),
      nature = Nature.JOLLY,
      level = 100
    )
    assertEquals(
      PokeStats(hp = 1, attack = 279, defense = 126, specialAttack = 86, specialDefense = 96, speed = 196),
      computed
    )
  }

  @Test
  fun computeHpIsUnaffectedByNature() {
    val baseStats = PokeStats.default(100)
    val evs = PokeStats.default(0)
    val ivs = PokeStats.default(31)

    val adamant = PokeStats.compute(baseStats = baseStats, evs = evs, ivs = ivs, nature = Nature.ADAMANT, level = 50)
    val modest  = PokeStats.compute(baseStats = baseStats, evs = evs, ivs = ivs, nature = Nature.MODEST,  level = 50)

    assertEquals(adamant.hp, modest.hp)
  }
}
