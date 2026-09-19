package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Item

/** Held berries that halve incoming super-effective damage of their type. Ported from getBerryResistType. */
private val RESIST_BERRIES: Map<PokeType, Item> = mapOf(
  PokeType.NORMAL to Item.CHILAN_BERRY,
  PokeType.FIRE to Item.OCCA_BERRY,
  PokeType.WATER to Item.PASSHO_BERRY,
  PokeType.ELECTRIC to Item.WACAN_BERRY,
  PokeType.GRASS to Item.RINDO_BERRY,
  PokeType.ICE to Item.YACHE_BERRY,
  PokeType.FIGHTING to Item.CHOPLE_BERRY,
  PokeType.POISON to Item.KEBIA_BERRY,
  PokeType.GROUND to Item.SHUCA_BERRY,
  PokeType.FLYING to Item.COBA_BERRY,
  PokeType.PSYCHIC to Item.PAYAPA_BERRY,
  PokeType.BUG to Item.TANGA_BERRY,
  PokeType.ROCK to Item.CHARTI_BERRY,
  PokeType.GHOST to Item.KASIB_BERRY,
  PokeType.DRAGON to Item.HABAN_BERRY,
  PokeType.DARK to Item.COLBUR_BERRY,
  PokeType.STEEL to Item.BABIRI_BERRY,
  PokeType.FAIRY to Item.ROSELI_BERRY,
)

fun resistsType(item: Item?, type: PokeType): Boolean = RESIST_BERRIES[type] == item
