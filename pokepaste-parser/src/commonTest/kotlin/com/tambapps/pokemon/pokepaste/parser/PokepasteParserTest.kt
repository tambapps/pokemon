package com.tambapps.pokemon.pokepaste.parser

import com.tambapps.pokemon.*
import kotlin.test.*

class PokepasteParserTest {

    private val parser = PokepasteParser()

    @Test
    fun parseTest() {
        val pokepaste = parser.parse(PokePastes.BASIC)

        val expectedMons = listOf(
            Pokemon(
                name = "Miraidon",
                surname = null,
                gender = null,
                nature = "Modest",
                item = "Choice Specs",
                shiny = false,
                happiness = 255,
                ability = "Hadron Engine",
                teraType = PokeType.FAIRY,
                level = 50,
                moves = listOf("Volt Switch", "Draco Meteor", "Electro Drift", "Dazzling Gleam"),
                ivs = PokeStats.default(31),
                evs = buildStats(0) {
                    specialAttack = 252
                    specialDefense = 4
                    speed = 252
                }
            ),
            Pokemon(
                name = "Entei",
                surname = null,
                gender = null,
                nature = "Adamant",
                item = "Choice Band",
                shiny = false,
                happiness = 255,
                ability = "Inner Focus",
                teraType = PokeType.NORMAL,
                level = 50,
                moves = listOf("Sacred Fire", "Crunch", "Stomping Tantrum", "Extreme Speed"),
                ivs = PokeStats.default(31),
                evs = buildStats(0) {
                    hp = 140
                    attack = 252
                    specialDefense = 116
                }
            ),
            Pokemon(
                name = "Chien-Pao",
                surname = null,
                gender = null,
                nature = "Jolly",
                item = "Focus Sash",
                shiny = false,
                happiness = 255,
                ability = "Sword of Ruin",
                teraType = PokeType.STELLAR,
                level = 50,
                moves = listOf("Icicle Crash", "Sucker Punch", "Sacred Sword", "Protect"),
                ivs = PokeStats.default(31),
                evs = buildStats(0) {
                    attack = 252
                    speed = 252
                }
            ),
            Pokemon(
                name = "Iron Hands",
                surname = null,
                gender = null,
                nature = "Brave",
                item = "Assault Vest",
                shiny = false,
                happiness = 255,
                ability = "Quark Drive",
                teraType = PokeType.WATER,
                level = 50,
                moves = listOf("Drain Punch", "Low Kick", "Heavy Slam", "Fake Out"),
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
                name = "Whimsicott",
                surname = null,
                gender = null,
                nature = "Modest",
                item = "Covert Cloak",
                shiny = false,
                happiness = 255,
                ability = "Prankster",
                teraType = PokeType.DARK,
                level = 50,
                moves = listOf("Moonblast", "Encore", "Light Screen", "Tailwind"),
                ivs = buildStats(31) {
                    attack = 0
                },
                evs = buildStats(0) {
                    specialAttack = 252
                    speed = 252
                }
            ),
            Pokemon(
                name = "Ogerpon-Cornerstone",
                surname = null,
                gender = "F",
                nature = "Adamant",
                item = "Cornerstone Mask",
                shiny = false,
                happiness = 255,
                ability = "Sturdy",
                teraType = PokeType.ROCK,
                level = 50,
                moves = listOf("Ivy Cudgel", "Horn Leech", "Spiky Shield", "Follow Me"),
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
    }

    @Test
    fun parseTestWithSurnames() {
        val pokepaste = parser.parse(PokePastes.WITH_SURNAMES)

        val expectedMons = listOf(
            Pokemon(
                name = "Miraidon",
                surname = "Dondochakka",
                gender = null,
                nature = "Modest",
                item = "Choice Specs",
                shiny = false,
                happiness = 255,
                ability = "Hadron Engine",
                teraType = PokeType.ELECTRIC,
                level = 50,
                moves = listOf("Electro Drift", "Draco Meteor", "Volt Switch", "Discharge"),
                ivs = PokeStats.default(31),
                evs = buildStats(0) {
                    specialAttack = 252
                    speed = 252
                }
            ),
            Pokemon(
                name = "Farigiraf",
                surname = "Sophie",
                gender = "F",
                nature = "Bold",
                item = "Electric Seed",
                shiny = false,
                happiness = 255,
                ability = "Armor Tail",
                teraType = PokeType.GROUND,
                level = 54,
                moves = listOf("Dazzling Gleam", "Foul Play", "Trick Room", "Helping Hand"),
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
                name = "Landorus",
                surname = "Genius",
                gender = null,
                nature = "Timid",
                item = "Life Orb",
                shiny = false,
                happiness = 255,
                ability = "Sheer Force",
                teraType = PokeType.STEEL,
                level = 70,
                moves = listOf("Earth Power", "Sludge Bomb", "Taunt", "Protect"),
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
                name = "Whimsicott",
                surname = null,
                gender = "M",
                nature = "Timid",
                item = "Focus Sash",
                shiny = false,
                happiness = 255,
                ability = "Prankster",
                teraType = PokeType.GHOST,
                level = 50,
                moves = listOf("Moonblast", "Tailwind", "Encore", "Protect"),
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
                name = "Ogerpon-Cornerstone",
                surname = "Rockpon",
                gender = null,
                nature = "Adamant",
                item = "Cornerstone Mask",
                shiny = false,
                happiness = 255,
                ability = "Sturdy",
                teraType = PokeType.ROCK,
                level = 100,
                moves = listOf("Ivy Cudgel", "Horn Leech", "Follow Me", "Spiky Shield"),
                ivs = PokeStats.default(31),
                evs = buildStats(0) {
                    attack = 252
                    speed = 252
                    hp = 4
                }
            ),
            Pokemon(
                name = "Incineroar",
                surname = "Cat",
                gender = null,
                nature = "Impish",
                item = null,
                shiny = false,
                happiness = 255,
                ability = "Intimidate",
                teraType = PokeType.GRASS,
                level = 50,
                moves = listOf("Flare Blitz", "Knock Off", "Fake Out", "U-turn"),
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
    }
}