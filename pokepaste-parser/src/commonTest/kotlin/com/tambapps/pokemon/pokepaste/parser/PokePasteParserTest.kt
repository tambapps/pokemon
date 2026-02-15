package com.tambapps.pokemon.pokepaste.parser

import com.tambapps.pokemon.Gender
import com.tambapps.pokemon.Nature
import com.tambapps.pokemon.PokeStats
import com.tambapps.pokemon.Pokemon
import com.tambapps.pokemon.AbilityName
import com.tambapps.pokemon.ItemName
import com.tambapps.pokemon.MoveName
import com.tambapps.pokemon.PokemonName
import com.tambapps.pokemon.TeraType
import com.tambapps.pokemon.buildStats
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PokePasteParserTest {

  private val parser = PokepasteParser()

  @Test
  fun parseTest() {
    val pokepaste = parser.parse(PokePastes.BASIC)

    val expectedMons = listOf(
      Pokemon(
        name = PokemonName("Miraidon"),
        surname = null,
        gender = null,
        nature = Nature.MODEST,
        item = ItemName("Choice Specs"),
        shiny = false,
        happiness = 255,
        ability = AbilityName("Hadron Engine"),
        teraType = TeraType.FAIRY,
        level = 50,
        moves = listOf(MoveName("Volt Switch"), MoveName("Draco Meteor"), MoveName("Electro Drift"), MoveName("Dazzling Gleam")),
        ivs = PokeStats.default(31),
        evs = buildStats(0) {
          specialAttack = 252
          specialDefense = 4
          speed = 252
        }
      ),
      Pokemon(
        name = PokemonName("Entei"),
        surname = null,
        gender = null,
        nature = Nature.ADAMANT,
        item = ItemName("Choice Band"),
        shiny = false,
        happiness = 255,
        ability = AbilityName("Inner Focus"),
        teraType = TeraType.NORMAL,
        level = 50,
        moves = listOf(MoveName("Sacred Fire"), MoveName("Crunch"), MoveName("Stomping Tantrum"), MoveName("Extreme Speed")),
        ivs = PokeStats.default(31),
        evs = buildStats(0) {
          hp = 140
          attack = 252
          specialDefense = 116
        }
      ),
      Pokemon(
        name = PokemonName("Chien-Pao"),
        surname = null,
        gender = null,
        nature = Nature.JOLLY,
        item = ItemName("Focus Sash"),
        shiny = false,
        happiness = 255,
        ability = AbilityName("Sword of Ruin"),
        teraType = TeraType.STELLAR,
        level = 50,
        moves = listOf(MoveName("Icicle Crash"), MoveName("Sucker Punch"), MoveName("Sacred Sword"), MoveName("Protect")),
        ivs = PokeStats.default(31),
        evs = buildStats(0) {
          attack = 252
          speed = 252
        }
      ),
      Pokemon(
        name = PokemonName("Iron Hands"),
        surname = null,
        gender = null,
        nature = Nature.BRAVE,
        item = ItemName("Assault Vest"),
        shiny = false,
        happiness = 255,
        ability = AbilityName("Quark Drive"),
        teraType = TeraType.WATER,
        level = 50,
        moves = listOf(MoveName("Drain Punch"), MoveName("Low Kick"), MoveName("Heavy Slam"), MoveName("Fake Out")),
        ivs = buildStats(31) {
          speed = 0
        },
        evs = buildStats(0) {
          attack = 252
          defense = 20
          specialDefense = 236
        }
      ),
      Pokemon(
        name = PokemonName("Whimsicott"),
        surname = null,
        gender = null,
        nature = Nature.MODEST,
        item = ItemName("Covert Cloak"),
        shiny = false,
        happiness = 255,
        ability = AbilityName("Prankster"),
        teraType = TeraType.DARK,
        level = 50,
        moves = listOf(MoveName("Moonblast"), MoveName("Encore"), MoveName("Light Screen"), MoveName("Tailwind")),
        ivs = buildStats(31) {
          attack = 0
        },
        evs = buildStats(0) {
          specialAttack = 252
          speed = 252
        }
      ),
      Pokemon(
        name = PokemonName("Ogerpon-Cornerstone"),
        surname = null,
        gender = Gender.FEMALE,
        nature = Nature.ADAMANT,
        item = ItemName("Cornerstone Mask"),
        shiny = false,
        happiness = 255,
        ability = AbilityName("Sturdy"),
        teraType = TeraType.ROCK,
        level = 50,
        moves = listOf(MoveName("Ivy Cudgel"), MoveName("Horn Leech"), MoveName("Spiky Shield"), MoveName("Follow Me")),
        ivs = PokeStats.default(31),
        evs = buildStats(0) {
          attack = 252
          speed = 252
        }
      )
    )

    assertEquals(expectedMons.size, pokepaste.pokemons.size)
    for (i in expectedMons.indices) {
      assertEquals(expectedMons[i], pokepaste.pokemons[i])
    }
    assertEquals(false, pokepaste.isOts)
  }

  @Test
  fun parseTestWithSurnames() {
    val pokepaste = parser.parse(PokePastes.WITH_SURNAMES)

    val expectedMons = listOf(
      Pokemon(
        name = PokemonName("Miraidon"),
        surname = "Dondochakka",
        gender = null,
        nature = Nature.MODEST,
        item = ItemName("Choice Specs"),
        shiny = false,
        happiness = 255,
        ability = AbilityName("Hadron Engine"),
        teraType = TeraType.ELECTRIC,
        level = 50,
        moves = listOf(MoveName("Electro Drift"), MoveName("Draco Meteor"), MoveName("Volt Switch"), MoveName("Discharge")),
        ivs = PokeStats.default(31),
        evs = buildStats(0) {
          specialAttack = 252
          speed = 252
        }
      ),
      Pokemon(
        name = PokemonName("Farigiraf"),
        surname = "Sophie",
        gender = Gender.FEMALE,
        nature = Nature.BOLD,
        item = ItemName("Electric Seed"),
        shiny = false,
        happiness = 255,
        ability = AbilityName("Armor Tail"),
        teraType = TeraType.GROUND,
        level = 54,
        moves = listOf(MoveName("Dazzling Gleam"), MoveName("Foul Play"), MoveName("Trick Room"), MoveName("Helping Hand")),
        ivs = buildStats(31) {
          attack = 0
        },
        evs = buildStats(0) {
          hp = 180
          defense = 236
          specialDefense = 92
        }
      ),
      Pokemon(
        name = PokemonName("Landorus"),
        surname = "Genius",
        gender = null,
        nature = Nature.TIMID,
        item = ItemName("Life Orb"),
        shiny = false,
        happiness = 255,
        ability = AbilityName("Sheer Force"),
        teraType = TeraType.STEEL,
        level = 70,
        moves = listOf(MoveName("Earth Power"), MoveName("Sludge Bomb"), MoveName("Taunt"), MoveName("Protect")),
        ivs = buildStats(31) {
          attack = 0
        },
        evs = buildStats(0) {
          specialAttack = 252
          specialDefense = 100
          speed = 156
        }
      ),
      Pokemon(
        name = PokemonName("Whimsicott"),
        surname = null,
        gender = Gender.MALE,
        nature = Nature.TIMID,
        item = ItemName("Focus Sash"),
        shiny = false,
        happiness = 255,
        ability = AbilityName("Prankster"),
        teraType = TeraType.GHOST,
        level = 50,
        moves = listOf(MoveName("Moonblast"), MoveName("Tailwind"), MoveName("Encore"), MoveName("Protect")),
        ivs = buildStats(31) {
          attack = 0
        },
        evs = buildStats(0) {
          hp = 4
          defense = 44
          specialAttack = 204
          specialDefense = 4
          speed = 252
        }
      ),
      Pokemon(
        name = PokemonName("Ogerpon-Cornerstone"),
        surname = "Rockpon",
        gender = null,
        nature = Nature.ADAMANT,
        item = ItemName("Cornerstone Mask"),
        shiny = false,
        happiness = 255,
        ability = AbilityName("Sturdy"),
        teraType = TeraType.ROCK,
        level = 100,
        moves = listOf(MoveName("Ivy Cudgel"), MoveName("Horn Leech"), MoveName("Follow Me"), MoveName("Spiky Shield")),
        ivs = PokeStats.default(31),
        evs = buildStats(0) {
          attack = 252
          speed = 252
          hp = 4
        }
      ),
      Pokemon(
        name = PokemonName("Incineroar"),
        surname = "Cat",
        gender = null,
        nature = Nature.IMPISH,
        item = null,
        shiny = false,
        happiness = 255,
        ability = AbilityName("Intimidate"),
        teraType = TeraType.GRASS,
        level = 50,
        moves = listOf(MoveName("Flare Blitz"), MoveName("Knock Off"), MoveName("Fake Out"), MoveName("U-turn")),
        ivs = buildStats(31) {
          speed = 29
        },
        evs = buildStats(0) {
          hp = 252
          defense = 196
          specialDefense = 60
        }
      )
    )

    assertEquals(expectedMons.size, pokepaste.pokemons.size)
    for (i in expectedMons.indices) {
      assertEquals(expectedMons[i], pokepaste.pokemons[i])
    }
    assertEquals(false, pokepaste.isOts)
  }

  @Test
  fun parseIncompletePokepasteWithoutMoves() {
    val exception = assertFailsWith<PokePasteParseException> {
      parser.parse(PokePastes.INCOMPLETE_WITHOUT_MOVES)
    }
    assertContains(exception.message ?: "", "has no moves")
  }

  @Test
  fun testToPokePasteString() {
    for (str in listOf(PokePastes.BASIC, PokePastes.WITH_SURNAMES)) {
      val pp = parser.parse(str)
      assertEquals(pp, parser.parse(pp.toPokePasteString()))
    }
  }

  @Test
  fun parseEmptyPokepaste() {
    val exception = assertFailsWith<PokePasteParseException> {
      parser.parse("")
    }
    assertContains(exception.message ?: "", "Missing name")
  }

  @Test
  fun testOts() {
    val pokepaste = parser.parse(PokePastes.OTS)
    assertEquals(true, pokepaste.isOts)
  }
}