package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.AbilityName
import com.tambapps.pokemon.MoveName
import com.tambapps.pokemon.Nature
import com.tambapps.pokemon.PokeStats
import com.tambapps.pokemon.PokemonName
import com.tambapps.pokemon.champions.data.ChampionsDex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** What the KO chance texts are about, telling apart the ones that aren't a KO chance */
class KoChanceKindTest {

  private fun pokemon(species: String, ability: String, attack: Int = 0) = BattlePokemon(
    species = ChampionsDex.species(PokemonName(species)),
    ability = AbilityName(ability),
    nature = Nature.SERIOUS,
    statPoints = PokeStats(hp = 0, attack = attack, defense = 0, specialAttack = 0, specialDefense = 0, speed = 0),
  )

  private fun koChance(attacker: BattlePokemon, defender: BattlePokemon, move: String) =
    DamageCalculator.calculateMove(attacker, defender, MoveUse(ChampionsDex.move(MoveName(move))), Battlefield()).koChance

  @Test
  fun aKo() {
    val koChance = koChance(pokemon("Garchomp", "Rough Skin", attack = 32), pokemon("Incineroar", "Intimidate"), "Earthquake")
    assertEquals(KoChance.Kind.KO, koChance.kind)
    assertEquals(2, koChance.uses)
  }

  @Test
  fun noDamage() {
    val koChance = koChance(pokemon("Garchomp", "Rough Skin"), pokemon("Corviknight", "Pressure"), "Earthquake")
    assertEquals(KoChance.Kind.NO_DAMAGE, koChance.kind)
    assertEquals("No damage for you", koChance.text)
  }

  @Test
  fun noKoInTheMaxUses() {
    val koChance = koChance(pokemon("Toxapex", "Regenerator"), pokemon("Toxapex", "Regenerator"), "Fake Out")
    assertEquals(KoChance.Kind.NO_KO_IN_MAX_USES, koChance.kind)
    assertEquals("possibly the worst move ever", koChance.text)
    assertNull(koChance.uses)
  }
}
