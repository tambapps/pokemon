package com.tambapps.pokemon.util

import com.tambapps.pokemon.ItemName
import com.tambapps.pokemon.PokemonName

object MegaUtils {

  val ABOMASITE = ItemName("abomasite")
  val ABSOLITE = ItemName("absolite")
  val AERODACTYLITE = ItemName("aerodactylite")
  val AGGRONITE = ItemName("aggronite")
  val ALAKAZITE = ItemName("alakazite")
  val ALTARIANITE = ItemName("altarianite")
  val AMPHAROSITE = ItemName("ampharosite")
  val AUDINITE = ItemName("audinite")
  val BANETTITE = ItemName("banettite")
  val BEEDRILLITE = ItemName("beedrillite")
  val BLASTOISINITE = ItemName("blastoisinite")
  val BLAZIKENITE = ItemName("blazikenite")
  val CAMERUPTITE = ItemName("cameruptite")
  val CHARIZARDITE_X = ItemName("charizardite-x")
  val CHARIZARDITE_Y = ItemName("charizardite-y")
  val DIANCITE = ItemName("diancite")
  val FLOETTITE = ItemName("floettite")
  val GALLADITE = ItemName("galladite")
  val GARCHOMPITE = ItemName("garchompite")
  val GARDEVOIRITE = ItemName("gardevoirite")
  val GENGARITE = ItemName("gengarite")
  val GLALITITE = ItemName("glalitite")
  val GYARADOSITE = ItemName("gyaradosite")
  val HERACRONITE = ItemName("heracronite")
  val HOUNDOOMINITE = ItemName("houndoominite")
  val KANGASKHANITE = ItemName("kangaskhanite")
  val LATIASITE = ItemName("latiasite")
  val LATIOSITE = ItemName("latiosite")
  val LOPUNNITE = ItemName("lopunnite")
  val LUCARIONITE = ItemName("lucarionite")
  val MANECTITE = ItemName("manectite")
  val MAWILITE = ItemName("mawilite")
  val MEDICHAMITE = ItemName("medichamite")
  val METAGROSSITE = ItemName("metagrossite")
  val MEWTWONITE_X = ItemName("mewtwonite-x")
  val MEWTWONITE_Y = ItemName("mewtwonite-y")
  val PIDGEOTITE = ItemName("pidgeotite")
  val PINSIRITE = ItemName("pinsirite")
  val SABLENITE = ItemName("sablenite")
  val SALAMENCITE = ItemName("salamencite")
  val SCEPTILITE = ItemName("sceptilite")
  val SCIZORITE = ItemName("scizorite")
  val SHARPEDONITE = ItemName("sharpedonite")
  val SLOWBRONITE = ItemName("slowbronite")
  val STEELIXITE = ItemName("steelixite")
  val SWAMPERTITE = ItemName("swampertite")
  val TYRANITARITE = ItemName("tyranitarite")
  val VENUSAURITE = ItemName("venusaurite")

  private val MEGA_STONES: Set<ItemName> = setOf(
    ABOMASITE, ABSOLITE, AERODACTYLITE, AGGRONITE, ALAKAZITE, ALTARIANITE,
    AMPHAROSITE, AUDINITE, BANETTITE, BEEDRILLITE, BLASTOISINITE, BLAZIKENITE,
    CAMERUPTITE, CHARIZARDITE_X, CHARIZARDITE_Y, DIANCITE, FLOETTITE, GALLADITE, GARCHOMPITE,
    GARDEVOIRITE, GENGARITE, GLALITITE, GYARADOSITE, HERACRONITE, HOUNDOOMINITE,
    KANGASKHANITE, LATIASITE, LATIOSITE, LOPUNNITE, LUCARIONITE, MANECTITE,
    MAWILITE, MEDICHAMITE, METAGROSSITE, MEWTWONITE_X, MEWTWONITE_Y, PIDGEOTITE,
    PINSIRITE, SABLENITE, SALAMENCITE, SCEPTILITE, SCIZORITE, SHARPEDONITE,
    SLOWBRONITE, STEELIXITE, SWAMPERTITE, TYRANITARITE, VENUSAURITE,
  )

  private val MEGA_STONE_TO_POKEMON: Map<ItemName, PokemonName> = mapOf(
    ABOMASITE to PokemonName("abomasnow-mega"),
    ABSOLITE to PokemonName("absol-mega"),
    AERODACTYLITE to PokemonName("aerodactyl-mega"),
    AGGRONITE to PokemonName("aggron-mega"),
    ALAKAZITE to PokemonName("alakazam-mega"),
    ALTARIANITE to PokemonName("altaria-mega"),
    AMPHAROSITE to PokemonName("ampharos-mega"),
    AUDINITE to PokemonName("audino-mega"),
    BANETTITE to PokemonName("banette-mega"),
    BEEDRILLITE to PokemonName("beedrill-mega"),
    BLASTOISINITE to PokemonName("blastoise-mega"),
    BLAZIKENITE to PokemonName("blaziken-mega"),
    CAMERUPTITE to PokemonName("camerupt-mega"),
    CHARIZARDITE_X to PokemonName("charizard-mega-x"),
    CHARIZARDITE_Y to PokemonName("charizard-mega-y"),
    DIANCITE to PokemonName("diancie-mega"),
    FLOETTITE to PokemonName("floette-mega"),
    GALLADITE to PokemonName("gallade-mega"),
    GARCHOMPITE to PokemonName("garchomp-mega"),
    GARDEVOIRITE to PokemonName("gardevoir-mega"),
    GENGARITE to PokemonName("gengar-mega"),
    GLALITITE to PokemonName("glalie-mega"),
    GYARADOSITE to PokemonName("gyarados-mega"),
    HERACRONITE to PokemonName("heracross-mega"),
    HOUNDOOMINITE to PokemonName("houndoom-mega"),
    KANGASKHANITE to PokemonName("kangaskhan-mega"),
    LATIASITE to PokemonName("latias-mega"),
    LATIOSITE to PokemonName("latios-mega"),
    LOPUNNITE to PokemonName("lopunny-mega"),
    LUCARIONITE to PokemonName("lucario-mega"),
    MANECTITE to PokemonName("manectric-mega"),
    MAWILITE to PokemonName("mawile-mega"),
    MEDICHAMITE to PokemonName("medicham-mega"),
    METAGROSSITE to PokemonName("metagross-mega"),
    MEWTWONITE_X to PokemonName("mewtwo-mega-x"),
    MEWTWONITE_Y to PokemonName("mewtwo-mega-y"),
    PIDGEOTITE to PokemonName("pidgeot-mega"),
    PINSIRITE to PokemonName("pinsir-mega"),
    SABLENITE to PokemonName("sableye-mega"),
    SALAMENCITE to PokemonName("salamence-mega"),
    SCEPTILITE to PokemonName("sceptile-mega"),
    SCIZORITE to PokemonName("scizor-mega"),
    SHARPEDONITE to PokemonName("sharpedo-mega"),
    SLOWBRONITE to PokemonName("slowbro-mega"),
    STEELIXITE to PokemonName("steelix-mega"),
    SWAMPERTITE to PokemonName("swampert-mega"),
    TYRANITARITE to PokemonName("tyranitar-mega"),
    VENUSAURITE to PokemonName("venusaur-mega"),
  )

  fun getMegaPokemon(item: ItemName?) = MEGA_STONE_TO_POKEMON[item?.normalized]

  val ItemName.isMegaStone: Boolean get() = normalized in MEGA_STONES

  fun PokemonName.toMega(itemName: ItemName): PokemonName? = getMegaPokemon(itemName)?.takeIf { it.baseMatches(this) }

  val PokemonName.canMega: Boolean get() = !isMega && MEGA_STONE_TO_POKEMON.values.any { it.baseMatches(this) }
}