package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.ChampionsDex

internal const val FLING = "Fling"

/** Ported from getFlingPower and cantFlingItem in item_data.js. */
internal object Fling {

  // getFlingPower's lists, by power
  private val POWER_130 = setOf("Iron Ball", "Big Nugget")
  private val POWER_100 = setOf("Hard Stone", "Room Service")
  private val POWER_90 = setOf("Deep Sea Tooth", "Thick Club", "Grip Claw")
  // the source checks Eviolite here before the 40 BP list, so it's 80
  private val POWER_80 = setOf("Eviolite", "Assault Vest", "Weakness Policy", "Blunder Policy", "Heavy-Duty Boots", "Quick Claw", "Razor Claw", "Safety Goggles")
  private val POWER_70 = setOf(
    "Poison Barb", "Dragon Fang", "Power Anklet", "Power Band", "Power Belt", "Power Bracer", "Power Lens",
    "Power Weight", "Burn Drive", "Chill Drive", "Douse Drive", "Shock Drive",
  )
  private val POWER_60 = setOf("Adamant Orb", "Lustrous Orb", "Macho Brace", "Leek", "Rocky Helmet", "Utility Umbrella", "Terrain Extender", "Damp Rock", "Heat Rock")
  private val POWER_50 = setOf("Sharp Beak", "Eject Pack")
  private val POWER_40 = setOf("Eviolite", "Icy Rock", "Lucky Punch")
  private val POWER_30 = setOf(
    "Black Belt", "Black Sludge", "Black Glasses", "Charcoal", "Deep Sea Scale", "Flame Orb", "King's Rock",
    "Life Orb", "Light Ball", "Magnet", "Metal Coat", "Miracle Seed", "Mystic Water", "Never-Melt Ice",
    "Razor Fang", "Soul Dew", "Spell Tag", "Toxic Orb", "Twisted Spoon", "Absorb Bulb", "Adrenaline Orb",
    "Berry Juice", "Binding Band", "Eject Button", "Float Stone", "Light Clay", "Luminous Moss",
    "Metronome", "Protective Pads", "Shell Bell", "Throat Spray", "Covert Cloak", "Loaded Dice",
    "Ability Shield", "Booster Energy", "Clear Amulet", "Punching Glove", "Big Nugget",
  )

  /** Fling's power with [attacker]'s held item. */
  fun power(attacker: BattlePokemon): Int {
    val item = heldItemName(attacker)
    return when {
      item in POWER_130 -> 130
      item in POWER_100 -> 100
      item.contains("Plate") || item in POWER_90 -> 90
      item in POWER_80 -> 80
      item in POWER_70 -> 70
      item in POWER_60 -> 60
      item.contains("Memory") || item in POWER_50 -> 50
      item in POWER_40 -> 40
      item in POWER_30 -> 30
      else -> 10
    }
  }

  /**
   * Whether Fling fails: no item (Klutz counts as holding none), a gem, the mega stone its holder mega evolves with,
   * or a berry against Unnerve.
   */
  fun cantFling(attacker: BattlePokemon, defenderAbility: Ability): Boolean {
    if (attacker.item == null || attacker.resolvedAbility == Ability.KLUTZ) return true
    val item = heldItemName(attacker)
    // like the source's canMega, only a Pokemon that can still mega evolve holds on to its stone, not the mega itself
    val megaStones = if (attacker.species.megaStone != null) emptyList() else ChampionsDex.formsOf(attacker.species).mapNotNull { it.megaStone?.value }
    return item.contains(" Gem") || item in megaStones ||
      (defenderAbility == Ability.UNNERVE && item.contains(" Berry"))
  }

  // the item's Champions name when recognized, else its name as given
  private fun heldItemName(attacker: BattlePokemon): String = attacker.resolvedItem?.displayName ?: attacker.item?.value.orEmpty()
}
