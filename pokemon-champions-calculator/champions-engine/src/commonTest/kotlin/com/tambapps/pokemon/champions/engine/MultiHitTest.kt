package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.Nature
import com.tambapps.pokemon.champions.data.ChampionsCalcException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MultiHitTest {

  private val garchomp = testPokemon("Garchomp", nature = Nature.ADAMANT, hp = 20, attack = 20, defense = 8, specialDefense = 8, speed = 10)
  private val toxapex = testPokemon("Toxapex", nature = Nature.BOLD, hp = 20, defense = 20, specialDefense = 16, speed = 6)

  @Test
  fun hitCountRanges() {
    assertEquals(1..1, testMove("Earthquake").hitCountRange)
    assertEquals(2..2, testMove("Double Hit").hitCountRange)
    assertEquals(2..5, testMove("Bullet Seed").hitCountRange)
    assertEquals(1..3, testMove("Triple Axel").hitCountRange)
  }

  @Test
  fun defaultHitCountsFollowTheSourceCalculator() {
    assertEquals(1, defaultHitCount(testMove("Earthquake"), garchomp))
    assertEquals(2, defaultHitCount(testMove("Double Hit"), garchomp))
    assertEquals(3, defaultHitCount(testMove("Bullet Seed"), garchomp))
    assertEquals(5, defaultHitCount(testMove("Bullet Seed"), testPokemon("Garchomp", ability = "Skill Link")))
    assertEquals(1, defaultHitCount(testMove("Dragon Darts"), garchomp))
    assertEquals(4, defaultHitCount(testMove("Beat Up"), garchomp))
    assertEquals(3, defaultHitCount(testMove("Triple Axel"), garchomp))
    assertEquals(10, defaultHitCount(testMove("Population Bomb"), garchomp))
  }

  @Test
  fun aSingleHitMoveHasOneHit() {
    val move = MoveUse(testMove("Dragon Claw"))
    val result = DamageCalculator.calculateMove(garchomp, toxapex, move, Battlefield())
    assertEquals(listOf(DamageCalculator.calculateSingleHit(garchomp, toxapex, move, Battlefield())), result.hits)
  }

  @Test
  fun aMultiHitMoveRepeatsTheSameHit() {
    val move = MoveUse(testMove("Bullet Seed"))
    val result = DamageCalculator.calculateMove(garchomp, toxapex, move, Battlefield(), hits = 4)
    val hit = DamageCalculator.calculateSingleHit(garchomp, toxapex, move, Battlefield())
    assertEquals(List(4) { hit }, result.hits)
    assertEquals(hit.minDamage * 4, result.minDamage)
    assertEquals(hit.maxDamage * 4, result.maxDamage)
  }

  @Test
  fun tripleAxelHitsGetStronger() {
    val result = DamageCalculator.calculateMove(garchomp, toxapex, MoveUse(testMove("Triple Axel")), Battlefield())
    assertEquals(3, result.hits.size)
    assertTrue(result.hits[0].maxDamage < result.hits[1].maxDamage)
    assertTrue(result.hits[1].maxDamage < result.hits[2].maxDamage)
  }

  @Test
  fun parentalBondHitsTwice() {
    val bond = testPokemon("Garchomp", ability = "Parental Bond", nature = Nature.ADAMANT, hp = 20, attack = 20, defense = 8, specialDefense = 8, speed = 10)
    val move = MoveUse(testMove("Dragon Claw"))
    val result = DamageCalculator.calculateMove(bond, toxapex, move, Battlefield())
    val expected = DamageCalculator.calculateParentalBondHits(bond, toxapex, move, Battlefield())
    assertEquals(listOf(expected.firstHit, expected.secondHit), result.hits)
  }

  @Test
  fun parentalBondDoesNotApplyToSpreadMovesInDoubles() {
    val bond = testPokemon("Garchomp", ability = "Parental Bond", attack = 20)
    val earthquake = MoveUse(testMove("Earthquake"))
    assertEquals(1, DamageCalculator.calculateMove(bond, toxapex, earthquake, Battlefield(format = BattleFormat.DOUBLES)).hits.size)
    assertEquals(2, DamageCalculator.calculateMove(bond, toxapex, earthquake, Battlefield(format = BattleFormat.SINGLES)).hits.size)
  }

  @Test
  fun hitCountMustBeInTheMoveRange() {
    assertFailsWith<ChampionsCalcException> {
      DamageCalculator.calculateMove(garchomp, toxapex, MoveUse(testMove("Bullet Seed")), Battlefield(), hits = 6)
    }
    assertFailsWith<ChampionsCalcException> {
      DamageCalculator.calculateMove(garchomp, toxapex, MoveUse(testMove("Dragon Claw")), Battlefield(), hits = 2)
    }
  }

  @Test
  fun usesToKoMatchesHitsToKoForASingleHitMove() {
    val move = MoveUse(testMove("Dragon Claw"))
    val result = DamageCalculator.calculateMove(garchomp, toxapex, move, Battlefield())
    assertEquals(
      KoChanceCalculator.minimumHitsToKo(result.hits.single().rolls, toxapex.hp),
      KoChanceCalculator.minimumUsesToKo(result, toxapex.hp),
    )
  }

  @Test
  fun usesToKoCountsEveryHitOfAMultiHitMove() {
    val result = DamageCalculator.calculateMove(garchomp, toxapex, MoveUse(testMove("Bullet Seed")), Battlefield(), hits = 3)
    // one use can never KO, two uses can
    val targetHp = result.maxDamage + 1
    val koChance = KoChanceCalculator.minimumUsesToKo(result, targetHp)!!
    assertEquals(2, koChance.hits)
    // identical hits: 2 uses of a 3 hit move are 6 hits
    assertEquals(KoChanceCalculator.koChance(result.hits.first().rolls, hits = 6, targetHp = targetHp), koChance.chance, 1e-9)
  }
}
