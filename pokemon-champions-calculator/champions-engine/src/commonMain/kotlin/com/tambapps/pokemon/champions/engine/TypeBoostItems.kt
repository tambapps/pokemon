package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Item

/** Held items that boost same-type moves by 1.2x. Ported from getItemBoostType, scoped to the non-Plate items Champions has. */
private val TYPE_BOOST_ITEMS: Map<PokeType, Item> = mapOf(
  PokeType.BUG to Item.SILVER_POWDER,
  PokeType.STEEL to Item.METAL_COAT,
  PokeType.GROUND to Item.SOFT_SAND,
  PokeType.ROCK to Item.HARD_STONE,
  PokeType.GRASS to Item.MIRACLE_SEED,
  PokeType.DARK to Item.BLACK_GLASSES,
  PokeType.FIGHTING to Item.BLACK_BELT,
  PokeType.ELECTRIC to Item.MAGNET,
  PokeType.WATER to Item.MYSTIC_WATER,
  PokeType.FLYING to Item.SHARP_BEAK,
  PokeType.POISON to Item.POISON_BARB,
  PokeType.ICE to Item.NEVER_MELT_ICE,
  PokeType.GHOST to Item.SPELL_TAG,
  PokeType.PSYCHIC to Item.TWISTED_SPOON,
  PokeType.FIRE to Item.CHARCOAL,
  PokeType.DRAGON to Item.DRAGON_FANG,
  PokeType.NORMAL to Item.SILK_SCARF,
  PokeType.FAIRY to Item.FAIRY_FEATHER,
)

fun boostsType(item: Item?, type: PokeType): Boolean = TYPE_BOOST_ITEMS[type] == item
