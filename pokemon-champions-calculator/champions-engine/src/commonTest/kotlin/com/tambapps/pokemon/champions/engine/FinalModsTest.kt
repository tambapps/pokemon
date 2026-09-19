package com.tambapps.pokemon.champions.engine

import kotlin.test.Test
import kotlin.test.assertEquals

class FinalModsTest {

  private fun combinedMod(
    moveName: String,
    attacker: BattlePokemon,
    defender: BattlePokemon,
    field: Battlefield = Battlefield(),
    isCritical: Boolean = false,
    typeEffectiveness: Double = 1.0,
  ): Int {
    val move = testMove(moveName)
    val effectiveType = effectiveTypeOf(move, attacker, field)
    return chainMods(FinalMods.resolve(move, effectiveType, attacker, defender, field, isCritical, typeEffectiveness))
  }

  private val garchomp = testPokemon("Garchomp")
  private val toxapex = testPokemon("Toxapex")

  @Test
  fun reflectHalvesPhysicalDamageMoreInDoubles() {
    val doubles = Battlefield(format = BattleFormat.DOUBLES, defenderSide = SideConditions(hasReflect = true))
    val singles = doubles.copy(format = BattleFormat.SINGLES)
    assertEquals(0xAAC, combinedMod("Earthquake", garchomp, toxapex, doubles))
    assertEquals(0x800, combinedMod("Earthquake", garchomp, toxapex, singles))
  }

  @Test
  fun reflectDoesNotApplyToSpecialMoves() {
    val field = Battlefield(defenderSide = SideConditions(hasReflect = true))
    assertEquals(0x1000, combinedMod("Flamethrower", testPokemon("Charizard"), toxapex, field))
  }

  @Test
  fun lightScreenHalvesSpecialDamage() {
    val field = Battlefield(defenderSide = SideConditions(hasLightScreen = true))
    assertEquals(0xAAC, combinedMod("Flamethrower", testPokemon("Charizard"), toxapex, field))
  }

  @Test
  fun auroraVeilHalvesBothCategories() {
    val field = Battlefield(defenderSide = SideConditions(hasAuroraVeil = true))
    assertEquals(0xAAC, combinedMod("Earthquake", garchomp, toxapex, field))
    assertEquals(0xAAC, combinedMod("Flamethrower", testPokemon("Charizard"), toxapex, field))
  }

  @Test
  fun screensDontApplyOnACriticalHit() {
    val field = Battlefield(defenderSide = SideConditions(hasReflect = true))
    assertEquals(0x1000, combinedMod("Earthquake", garchomp, toxapex, field, isCritical = true))
  }

  @Test
  fun sniperBoostsCriticalHits() {
    val sniper = testPokemon("Garchomp", ability = "Sniper")
    assertEquals(0x1800, combinedMod("Earthquake", sniper, toxapex, isCritical = true))
    assertEquals(0x1000, combinedMod("Earthquake", sniper, toxapex, isCritical = false))
  }

  @Test
  fun multiscaleHalvesDamageOnlyAtFullHp() {
    val multiscale = testPokemon("Toxapex", ability = "Multiscale")
    val maxHp = multiscale.maxHp
    assertEquals(0x800, combinedMod("Earthquake", garchomp, multiscale))
    val damaged = testPokemon("Toxapex", ability = "Multiscale", currentHp = maxHp - 1)
    assertEquals(0x1000, combinedMod("Earthquake", garchomp, damaged))
  }

  @Test
  fun fluffyAndAuraGuardHalveContactDamage() {
    // Bug Bite makes contact and isn't Fire-type, so only the contact-halving clause is in play here.
    // (Earthquake, despite being a physical move, doesn't make contact in these games.)
    assertEquals(0x800, combinedMod("Bug Bite", garchomp, testPokemon("Toxapex", ability = "Fluffy")))
    assertEquals(0x800, combinedMod("Bug Bite", garchomp, testPokemon("Toxapex", ability = "Aura Guard")))
    assertEquals(0x1000, combinedMod("Dragon Pulse", testPokemon("Garchomp"), testPokemon("Toxapex", ability = "Fluffy"))) // no contact, not Fire-type
  }

  @Test
  fun fluffyDoublesIncomingFireDamage() {
    // Both the contact-halving and fire-doubling clauses can apply to the same hit; use a non-contact Fire move to isolate the second.
    assertEquals(0x2000, combinedMod("Flamethrower", testPokemon("Charizard"), testPokemon("Toxapex", ability = "Fluffy")))
  }

  @Test
  fun punkRockHalvesIncomingSoundDamage() {
    val punkRockDefender = testPokemon("Toxapex", ability = "Punk Rock")
    assertEquals(0x800, combinedMod("Hyper Voice", testPokemon("Garchomp"), punkRockDefender))
  }

  @Test
  fun friendGuardReducesDamageTakenByAnAlly() {
    val field = Battlefield(defenderSide = SideConditions(hasFriendGuard = true))
    assertEquals(0xC00, combinedMod("Earthquake", garchomp, toxapex, field))
  }

  @Test
  fun solidRockAndFilterReduceSuperEffectiveDamage() {
    assertEquals(0xC00, combinedMod("Earthquake", garchomp, testPokemon("Toxapex", ability = "Solid Rock"), typeEffectiveness = 2.0))
    assertEquals(0xC00, combinedMod("Earthquake", garchomp, testPokemon("Toxapex", ability = "Filter"), typeEffectiveness = 2.0))
    assertEquals(0x1000, combinedMod("Earthquake", garchomp, testPokemon("Toxapex", ability = "Solid Rock"), typeEffectiveness = 1.0))
  }

  @Test
  fun expertBeltBoostsSuperEffectiveHitsOnly() {
    val expertBelt = testPokemon("Garchomp", item = "Expert Belt")
    assertEquals(0x1333, combinedMod("Earthquake", expertBelt, toxapex, typeEffectiveness = 2.0))
    assertEquals(0x1000, combinedMod("Earthquake", expertBelt, toxapex, typeEffectiveness = 1.0))
  }

  @Test
  fun lifeOrbAlwaysBoostsDamage() {
    val lifeOrb = testPokemon("Garchomp", item = "Life Orb")
    assertEquals(0x14CC, combinedMod("Earthquake", lifeOrb, toxapex))
  }

  @Test
  fun resistBerryHalvesASuperEffectiveHitOfItsType() {
    val holder = testPokemon("Toxapex", item = "Shuca Berry") // resists Ground
    assertEquals(0x800, combinedMod("Earthquake", garchomp, holder, typeEffectiveness = 2.0))
  }

  @Test
  fun resistBerryAlsoAppliesToNeutralNormalTypeMoves() {
    val holder = testPokemon("Toxapex", item = "Chilan Berry") // resists Normal
    assertEquals(0x800, combinedMod("Double-Edge", garchomp, holder, typeEffectiveness = 1.0))
  }

  @Test
  fun ripenDoublesTheResistBerryReduction() {
    val holder = testPokemon("Toxapex", ability = "Ripen", item = "Shuca Berry")
    assertEquals(0x400, combinedMod("Earthquake", garchomp, holder, typeEffectiveness = 2.0))
  }

  @Test
  fun unnerveBlocksTheDefendersResistBerryEntirely() {
    val unnerveAttacker = testPokemon("Garchomp", ability = "Unnerve")
    val holder = testPokemon("Toxapex", item = "Shuca Berry")
    assertEquals(0x1000, combinedMod("Earthquake", unnerveAttacker, holder, typeEffectiveness = 2.0))
  }
}
