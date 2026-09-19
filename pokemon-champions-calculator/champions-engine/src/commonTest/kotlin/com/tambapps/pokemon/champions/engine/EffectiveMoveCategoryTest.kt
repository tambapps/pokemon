package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.champions.data.MoveCategory
import kotlin.test.Test
import kotlin.test.assertEquals

class EffectiveMoveCategoryTest {

  @Test
  fun everyMoveButShellSideArmKeepsItsStaticCategory() {
    val earthquake = testMove("Earthquake")
    assertEquals(MoveCategory.PHYSICAL, effectiveCategoryOf(earthquake, testPokemon("Garchomp"), testPokemon("Toxapex")))
  }

  @Test
  fun shellSideArmGoesPhysicalWhenTheAttackRatioFavorsIt() {
    // Attack:Defense ratio dwarfs Sp.Atk:Sp.Def -> Physical
    val attacker = testPokemon("Toxapex", attack = 32, specialAttack = 0)
    val defender = testPokemon("Toxapex", defense = 0, specialDefense = 32)
    assertEquals(MoveCategory.PHYSICAL, effectiveCategoryOf(testMove("Shell Side Arm"), attacker, defender))
  }

  @Test
  fun shellSideArmGoesSpecialWhenTheSpecialRatioFavorsIt() {
    val attacker = testPokemon("Toxapex", attack = 0, specialAttack = 32)
    val defender = testPokemon("Toxapex", defense = 32, specialDefense = 0)
    assertEquals(MoveCategory.SPECIAL, effectiveCategoryOf(testMove("Shell Side Arm"), attacker, defender))
  }
}
