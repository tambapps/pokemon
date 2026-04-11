package com.tambapps.pokemon

import kotlin.test.Test
import kotlin.test.assertEquals

class PokeStatsTest {

  private val megaDelphoxBaseStats = PokeStats(hp = 75, attack = 69, defense = 72, specialAttack = 159, specialDefense = 125, speed = 134)

  @Test
  fun computeWithNeutralNatureAndNoStatPoints() {
    val computed = PokeStats.compute(
      baseStats = megaDelphoxBaseStats,
      statPoints = PokeStats.default(0),
      nature = Nature.QUIRKY,
    )
    assertEquals(
      PokeStats(hp = 150, attack = 89, defense = 92, specialAttack = 179, specialDefense = 145, speed = 154),
      computed
    )
  }

  @Test
  fun computeWithTimidNature() {
    // TIMID: +Spe / -Atk
    val computed = PokeStats.compute(
      baseStats = megaDelphoxBaseStats,
      statPoints = buildStats(0) { hp = 2; specialAttack = 32; speed = 32 },
      nature = Nature.TIMID,
    )
    assertEquals(
      PokeStats(hp = 152, attack = 80, defense = 92, specialAttack = 211, specialDefense = 145, speed = 204),
      computed
    )
  }

  @Test
  fun computeWithModestNature() {
    // MODEST: +SpA / -Atk
    val computed = PokeStats.compute(
      baseStats = megaDelphoxBaseStats,
      statPoints = buildStats(0) { specialAttack = 16; speed = 8 },
      nature = Nature.MODEST,
    )
    assertEquals(
      PokeStats(hp = 150, attack = 80, defense = 92, specialAttack = 214, specialDefense = 145, speed = 162),
      computed
    )
  }

  @Test
  fun computeHpIsUnaffectedByNature() {
    val base = megaDelphoxBaseStats
    val statPoints = PokeStats.default(0)

    val timid  = PokeStats.compute(baseStats = base, statPoints = statPoints, nature = Nature.TIMID)
    val modest = PokeStats.compute(baseStats = base, statPoints = statPoints, nature = Nature.MODEST)

    assertEquals(timid.hp, modest.hp)
  }
}
