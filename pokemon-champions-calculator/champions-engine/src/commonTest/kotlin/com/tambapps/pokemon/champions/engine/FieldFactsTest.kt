package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.AbilityName
import com.tambapps.pokemon.MoveName
import com.tambapps.pokemon.Nature
import com.tambapps.pokemon.PokeStats
import com.tambapps.pokemon.PokemonName
import com.tambapps.pokemon.champions.data.ChampionsDex
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The spread move reduction and the field's Fairy Aura are facts of the calc only when they applied */
class FieldFactsTest {

  private fun pokemon(species: String, ability: String) = BattlePokemon(
    species = ChampionsDex.species(PokemonName(species)),
    ability = AbilityName(ability),
    nature = Nature.SERIOUS,
    statPoints = PokeStats(hp = 0, attack = 0, defense = 0, specialAttack = 0, specialDefense = 0, speed = 0),
  )

  private fun isSpread(move: String, format: BattleFormat) = DamageCalculator.calculateMove(
    pokemon("Garchomp", "Rough Skin"),
    pokemon("Incineroar", "Intimidate"),
    MoveUse(ChampionsDex.move(MoveName(move))),
    Battlefield(format = format),
  ).facts.isSpread

  @Test
  fun aSpreadMoveInDoubles() = assertTrue(isSpread("Earthquake", BattleFormat.DOUBLES))

  @Test
  fun aSpreadMoveInSingles() = assertFalse(isSpread("Earthquake", BattleFormat.SINGLES))

  @Test
  fun aSingleTargetMoveInDoubles() = assertFalse(isSpread("Dragon Claw", BattleFormat.DOUBLES))

  private fun isFairyAuraBoosted(move: String) = DamageCalculator.calculateMove(
    pokemon("Garchomp", "Rough Skin"),
    pokemon("Incineroar", "Intimidate"),
    MoveUse(ChampionsDex.move(MoveName(move))),
    Battlefield(isFairyAura = true),
  ).facts.isFairyAuraBoosted

  @Test
  fun fairyAuraBoostsAFairyMove() = assertTrue(isFairyAuraBoosted("Play Rough"))

  @Test
  fun fairyAuraDoesNotBoostAnotherMove() = assertFalse(isFairyAuraBoosted("Dragon Claw"))
}
