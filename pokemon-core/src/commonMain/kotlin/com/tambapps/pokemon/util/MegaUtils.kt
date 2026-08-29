package com.tambapps.pokemon.util

import com.tambapps.pokemon.ItemName
import com.tambapps.pokemon.PokemonName

object MegaUtils {

  val ABOMASITE = ItemName("abomasite")
  val ABSOLITE = ItemName("absolite")
  val ABSOLITE_Z = ItemName("absolite-z")
  val AERODACTYLITE = ItemName("aerodactylite")
  val AGGRONITE = ItemName("aggronite")
  val ALAKAZITE = ItemName("alakazite")
  val ALTARIANITE = ItemName("altarianite")
  val AMPHAROSITE = ItemName("ampharosite")
  val AUDINITE = ItemName("audinite")
  val BANETTITE = ItemName("banettite")
  val BARBARACITE = ItemName("barbaracite")
  val BAXCALIBRITE = ItemName("baxcalibrite")
  val BEEDRILLITE = ItemName("beedrillite")
  val BLASTOISINITE = ItemName("blastoisinite")
  val BLAZIKENITE = ItemName("blazikenite")
  val CAMERUPTITE = ItemName("cameruptite")
  val CHANDELURITE = ItemName("chandelurite")
  val CHARIZARDITE_X = ItemName("charizardite-x")
  val CHARIZARDITE_Y = ItemName("charizardite-y")
  val CHESNAUGHTITE = ItemName("chesnaughtite")
  val CHIMECHITE = ItemName("chimechite")
  val CLEFABLITE = ItemName("clefablite")
  val CRABOMINITE = ItemName("crabominite")
  val DARKRANITE = ItemName("darkranite")
  val DELPHOXITE = ItemName("delphoxite")
  val DIANCITE = ItemName("diancite")
  val DRAGALGITE = ItemName("dragalgite")
  val DRAGONINITE = ItemName("dragoninite")
  val DRAMPANITE = ItemName("drampanite")
  val EELEKTROSSITE = ItemName("eelektrossite")
  val EMBOARITE = ItemName("emboarite")
  val EXCADRITE = ItemName("excadrite")
  val FALINKSITE = ItemName("falinksite")
  val FERALIGITE = ItemName("feraligite")
  val FLOETTITE = ItemName("floettite")
  val FROSLASSITE = ItemName("froslassite")
  val GALLADITE = ItemName("galladite")
  val GARCHOMPITE = ItemName("garchompite")
  val GARCHOMPITE_Z = ItemName("garchompite-z")
  val GARDEVOIRITE = ItemName("gardevoirite")
  val GENGARITE = ItemName("gengarite")
  val GLALITITE = ItemName("glalitite")
  val GLIMMORANITE = ItemName("glimmoranite")
  val GOLISOPITE = ItemName("golisopite")
  val GOLURKITE = ItemName("golurkite")
  val GRENINJITE = ItemName("greninjite")
  val GYARADOSITE = ItemName("gyaradosite")
  val HAWLUCHANITE = ItemName("hawluchanite")
  val HEATRANITE = ItemName("heatranite")
  val HERACRONITE = ItemName("heracronite")
  val HOUNDOOMINITE = ItemName("houndoominite")
  val KANGASKHANITE = ItemName("kangaskhanite")
  val LATIASITE = ItemName("latiasite")
  val LATIOSITE = ItemName("latiosite")
  val LOPUNNITE = ItemName("lopunnite")
  val LUCARIONITE = ItemName("lucarionite")
  val LUCARIONITE_Z = ItemName("lucarionite-z")
  val MAGEARNITE = ItemName("magearnite")
  val MALAMARITE = ItemName("malamarite")
  val MANECTITE = ItemName("manectite")
  val MAWILITE = ItemName("mawilite")
  val MEDICHAMITE = ItemName("medichamite")
  val MEGANIUMITE = ItemName("meganiumite")
  val MEOWSTICITE = ItemName("meowsticite")
  val METAGROSSITE = ItemName("metagrossite")
  val MEWTWONITE_X = ItemName("mewtwonite-x")
  val MEWTWONITE_Y = ItemName("mewtwonite-y")
  val PIDGEOTITE = ItemName("pidgeotite")
  val PINSIRITE = ItemName("pinsirite")
  val PYROARITE = ItemName("pyroarite")
  val RAICHUNITE_X = ItemName("raichunite-x")
  val RAICHUNITE_Y = ItemName("raichunite-y")
  val SABLENITE = ItemName("sablenite")
  val SALAMENCITE = ItemName("salamencite")
  val SCEPTILITE = ItemName("sceptilite")
  val SCIZORITE = ItemName("scizorite")
  val SCOLIPITE = ItemName("scolipite")
  val SCOVILLAINITE = ItemName("scovillainite")
  val SCRAFTINITE = ItemName("scraftinite")
  val SHARPEDONITE = ItemName("sharpedonite")
  val SKARMORITE = ItemName("skarmorite")
  val SLOWBRONITE = ItemName("slowbronite")
  val STARAPTITE = ItemName("staraptite")
  val STARMINITE = ItemName("starminite")
  val STEELIXITE = ItemName("steelixite")
  val SWAMPERTITE = ItemName("swampertite")
  val TATSUGIRINITE = ItemName("tatsugirinite")
  val TYRANITARITE = ItemName("tyranitarite")
  val VENUSAURITE = ItemName("venusaurite")
  val VICTREEBELITE = ItemName("victreebelite")
  val ZERAORITE = ItemName("zeraorite")
  val ZYGARDITE = ItemName("zygardite")

  private val MEGA_STONES: Set<ItemName> = setOf(
    ABOMASITE, ABSOLITE, ABSOLITE_Z, AERODACTYLITE, AGGRONITE, ALAKAZITE,
    ALTARIANITE, AMPHAROSITE, AUDINITE, BANETTITE, BARBARACITE, BAXCALIBRITE,
    BEEDRILLITE, BLASTOISINITE, BLAZIKENITE, CAMERUPTITE, CHANDELURITE,
    CHARIZARDITE_X, CHARIZARDITE_Y, CHESNAUGHTITE, CHIMECHITE, CLEFABLITE,
    CRABOMINITE, DARKRANITE, DELPHOXITE, DIANCITE, DRAGALGITE, DRAGONINITE,
    DRAMPANITE, EELEKTROSSITE, EMBOARITE, EXCADRITE, FALINKSITE, FERALIGITE,
    FLOETTITE, FROSLASSITE, GALLADITE, GARCHOMPITE, GARCHOMPITE_Z, GARDEVOIRITE,
    GENGARITE, GLALITITE, GLIMMORANITE, GOLISOPITE, GOLURKITE, GRENINJITE,
    GYARADOSITE, HAWLUCHANITE, HEATRANITE, HERACRONITE, HOUNDOOMINITE,
    KANGASKHANITE, LATIASITE, LATIOSITE, LOPUNNITE, LUCARIONITE, LUCARIONITE_Z,
    MAGEARNITE, MALAMARITE, MANECTITE, MAWILITE, MEDICHAMITE, MEGANIUMITE,
    MEOWSTICITE, METAGROSSITE, MEWTWONITE_X, MEWTWONITE_Y, PIDGEOTITE, PINSIRITE,
    PYROARITE, RAICHUNITE_X, RAICHUNITE_Y, SABLENITE, SALAMENCITE, SCEPTILITE,
    SCIZORITE, SCOLIPITE, SCOVILLAINITE, SCRAFTINITE, SHARPEDONITE, SKARMORITE,
    SLOWBRONITE, STARAPTITE, STARMINITE, STEELIXITE, SWAMPERTITE, TATSUGIRINITE,
    TYRANITARITE, VENUSAURITE, VICTREEBELITE, ZERAORITE, ZYGARDITE,
  )

  private val MEGA_STONE_TO_POKEMON: Map<ItemName, PokemonName> = mapOf(
    ABOMASITE to PokemonName("abomasnow-mega"),
    ABSOLITE to PokemonName("absol-mega"),
    ABSOLITE_Z to PokemonName("absol-mega-z"),
    AERODACTYLITE to PokemonName("aerodactyl-mega"),
    AGGRONITE to PokemonName("aggron-mega"),
    ALAKAZITE to PokemonName("alakazam-mega"),
    ALTARIANITE to PokemonName("altaria-mega"),
    AMPHAROSITE to PokemonName("ampharos-mega"),
    AUDINITE to PokemonName("audino-mega"),
    BANETTITE to PokemonName("banette-mega"),
    BARBARACITE to PokemonName("barbaracle-mega"),
    BAXCALIBRITE to PokemonName("baxcalibur-mega"),
    BEEDRILLITE to PokemonName("beedrill-mega"),
    BLASTOISINITE to PokemonName("blastoise-mega"),
    BLAZIKENITE to PokemonName("blaziken-mega"),
    CAMERUPTITE to PokemonName("camerupt-mega"),
    CHANDELURITE to PokemonName("chandelure-mega"),
    CHARIZARDITE_X to PokemonName("charizard-mega-x"),
    CHARIZARDITE_Y to PokemonName("charizard-mega-y"),
    CHESNAUGHTITE to PokemonName("chesnaught-mega"),
    CHIMECHITE to PokemonName("chimecho-mega"),
    CLEFABLITE to PokemonName("clefable-mega"),
    CRABOMINITE to PokemonName("crabominable-mega"),
    DARKRANITE to PokemonName("darkrai-mega"),
    DELPHOXITE to PokemonName("delphox-mega"),
    DIANCITE to PokemonName("diancie-mega"),
    DRAGALGITE to PokemonName("dragalge-mega"),
    DRAGONINITE to PokemonName("dragonite-mega"),
    DRAMPANITE to PokemonName("drampa-mega"),
    EELEKTROSSITE to PokemonName("eelektross-mega"),
    EMBOARITE to PokemonName("emboar-mega"),
    EXCADRITE to PokemonName("excadrill-mega"),
    FALINKSITE to PokemonName("falinks-mega"),
    FERALIGITE to PokemonName("feraligatr-mega"),
    FLOETTITE to PokemonName("floette-mega"),
    FROSLASSITE to PokemonName("froslass-mega"),
    GALLADITE to PokemonName("gallade-mega"),
    GARCHOMPITE to PokemonName("garchomp-mega"),
    GARCHOMPITE_Z to PokemonName("garchomp-mega-z"),
    GARDEVOIRITE to PokemonName("gardevoir-mega"),
    GENGARITE to PokemonName("gengar-mega"),
    GLALITITE to PokemonName("glalie-mega"),
    GLIMMORANITE to PokemonName("glimmora-mega"),
    GOLISOPITE to PokemonName("golisopod-mega"),
    GOLURKITE to PokemonName("golurk-mega"),
    GRENINJITE to PokemonName("greninja-mega"),
    GYARADOSITE to PokemonName("gyarados-mega"),
    HAWLUCHANITE to PokemonName("hawlucha-mega"),
    HEATRANITE to PokemonName("heatran-mega"),
    HERACRONITE to PokemonName("heracross-mega"),
    HOUNDOOMINITE to PokemonName("houndoom-mega"),
    KANGASKHANITE to PokemonName("kangaskhan-mega"),
    LATIASITE to PokemonName("latias-mega"),
    LATIOSITE to PokemonName("latios-mega"),
    LOPUNNITE to PokemonName("lopunny-mega"),
    LUCARIONITE to PokemonName("lucario-mega"),
    LUCARIONITE_Z to PokemonName("lucario-mega-z"),
    MAGEARNITE to PokemonName("magearna-mega"),
    MALAMARITE to PokemonName("malamar-mega"),
    MANECTITE to PokemonName("manectric-mega"),
    MAWILITE to PokemonName("mawile-mega"),
    MEDICHAMITE to PokemonName("medicham-mega"),
    MEGANIUMITE to PokemonName("meganium-mega"),
    MEOWSTICITE to PokemonName("meowstic-mega"),
    METAGROSSITE to PokemonName("metagross-mega"),
    MEWTWONITE_X to PokemonName("mewtwo-mega-x"),
    MEWTWONITE_Y to PokemonName("mewtwo-mega-y"),
    PIDGEOTITE to PokemonName("pidgeot-mega"),
    PINSIRITE to PokemonName("pinsir-mega"),
    PYROARITE to PokemonName("pyroar-mega"),
    RAICHUNITE_X to PokemonName("raichu-mega-x"),
    RAICHUNITE_Y to PokemonName("raichu-mega-y"),
    SABLENITE to PokemonName("sableye-mega"),
    SALAMENCITE to PokemonName("salamence-mega"),
    SCEPTILITE to PokemonName("sceptile-mega"),
    SCIZORITE to PokemonName("scizor-mega"),
    SCOLIPITE to PokemonName("scolipede-mega"),
    SCOVILLAINITE to PokemonName("scovillain-mega"),
    SCRAFTINITE to PokemonName("scrafty-mega"),
    SHARPEDONITE to PokemonName("sharpedo-mega"),
    SKARMORITE to PokemonName("skarmory-mega"),
    SLOWBRONITE to PokemonName("slowbro-mega"),
    STARAPTITE to PokemonName("staraptor-mega"),
    STARMINITE to PokemonName("starmie-mega"),
    STEELIXITE to PokemonName("steelix-mega"),
    SWAMPERTITE to PokemonName("swampert-mega"),
    TATSUGIRINITE to PokemonName("tatsugiri-mega"),
    TYRANITARITE to PokemonName("tyranitar-mega"),
    VENUSAURITE to PokemonName("venusaur-mega"),
    VICTREEBELITE to PokemonName("victreebel-mega"),
    ZERAORITE to PokemonName("zeraora-mega"),
    ZYGARDITE to PokemonName("zygarde-mega"),
  )

  fun getMegaPokemons(name: PokemonName): Set<PokemonName> = MEGA_STONE_TO_POKEMON.entries
    .asSequence()
    .filter { it.value.baseMatches(name) }
    .map { it.value }
    .toSet()

  fun getMegaStones(name: PokemonName): Set<ItemName> = MEGA_STONE_TO_POKEMON.entries
    .asSequence()
    .filter { it.value.baseMatches(name) }
    .map { it.key }
    .toSet()

  fun getMegaPokemon(item: ItemName?) = MEGA_STONE_TO_POKEMON[item?.normalized]

  val ItemName.isMegaStone: Boolean get() = normalized in MEGA_STONES

  fun PokemonName.toMega(itemName: ItemName): PokemonName? = getMegaPokemon(itemName)?.takeIf { it.baseMatches(this) }

  val PokemonName.canMega: Boolean get() = !isMega && MEGA_STONE_TO_POKEMON.values.any { it.baseMatches(this) }
}
