package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.ChampionsDex
import com.tambapps.pokemon.champions.data.Item
import kotlin.math.floor

/**
 * One end-of-turn effect on the defender, as the KO chance counts it: the HP it restores (positive) or takes
 * (negative) every turn, and how the KO chance text names it. Toxic damage grows every turn, so for it [hp] is the
 * toxic counter instead, like in the source.
 */
internal data class EndOfTurnEffect(val hp: Int, val text: String, val isToxic: Boolean = false)

/** The switch-in damage from hazards, and how the KO chance text names each hazard. */
internal data class Hazards(val damage: Int, val texts: List<String>)

/**
 * Ported from ko_chance.js's getAllEndOfTurnEffects and the hazards of getKOChanceText, scoped to Champions: the
 * effects its calculator offers (no Sea of Fire, G-Max field, Nightmare or Steelsurge), and the abilities and items it
 * has (no Black Sludge, Sticky Barb, Heavy-Duty Boots, Safety Goggles, Bad Dreams or Comatose). Dynamax doesn't exist
 * there, so no damage is halved.
 */
internal object EndOfTurnEffects {

  /** The effects that change the defender's HP, in the order they happen at the end of a turn (the source's END_TURN_ORDER_GEN_5_ONWARDS). */
  fun of(defender: BattlePokemon, field: Battlefield, preventsHeal: Boolean, preventsHealItem: Boolean): List<EndOfTurnEffect> {
    val side = field.defenderSide
    val maxHp = defender.maxHp
    val ability = defender.resolvedAbility
    val item = defender.effectiveItem
    val hasMagicGuard = ability == Ability.MAGIC_GUARD
    val bigRootMod = if (item == Item.BIG_ROOT) 1.3 else 1.0
    return listOfNotNull(
      weatherEffect(defender, field, preventsHeal),
      if (field.terrain == Terrain.GRASSY && !preventsHeal && defender.isDefenderGrounded(field)) {
        EndOfTurnEffect(maxHp / 16, "Grassy Terrain recovery")
      } else null,
      if (item == Item.LEFTOVERS && !preventsHealItem) EndOfTurnEffect(maxHp / 16, "Leftovers recovery") else null,
      if (side.hasAquaRing && !preventsHeal) EndOfTurnEffect(floor(maxHp / 16 * bigRootMod).toInt(), "Aqua Ring recovery") else null,
      if (side.isIngrained && !preventsHeal) EndOfTurnEffect(floor(maxHp / 16 * bigRootMod).toInt(), "Ingrain recovery") else null,
      if (side.isLeechSeeded && !hasMagicGuard) EndOfTurnEffect(-(maxHp / 8), "Leech Seed damage") else null,
      statusEffect(defender, preventsHeal),
      if (side.isCursed && !hasMagicGuard) EndOfTurnEffect(-(maxHp / 4), "ghost Curse") else null,
      if (side.isSaltCured && !hasMagicGuard) {
        if (!defender.hasType(PokeType.WATER) && !defender.hasType(PokeType.STEEL)) {
          EndOfTurnEffect(-(maxHp / 16), "Salt Cure damage")
        } else {
          EndOfTurnEffect(-(maxHp / 8), "extra Salt Cure damage")
        }
      } else null,
      if (side.isBound && !hasMagicGuard) {
        val divisor = if (item == Item.BINDING_BAND) 6 else 8
        EndOfTurnEffect(-(maxHp / divisor), "binding damage")
      } else null,
    ).filter { it.hp != 0 } // the source skips an effect worth 0 HP (e.g. 1/16 of less than 16 HP)
  }

  private fun weatherEffect(defender: BattlePokemon, field: Battlefield, preventsHeal: Boolean): EndOfTurnEffect? {
    val maxHp = defender.maxHp
    val ability = defender.resolvedAbility
    return when (field.weather) {
      Weather.SUN -> if (ability == Ability.DRY_SKIN || ability == Ability.SOLAR_POWER) {
        EndOfTurnEffect(-(maxHp / 8), "${ability.displayName} damage")
      } else null
      Weather.RAIN -> when {
        preventsHeal -> null
        ability == Ability.DRY_SKIN -> EndOfTurnEffect(maxHp / 8, "Dry Skin recovery")
        ability == Ability.RAIN_DISH -> EndOfTurnEffect(maxHp / 16, "Rain Dish recovery")
        else -> null
      }
      Weather.SAND -> {
        val isImmune = defender.hasType(PokeType.ROCK) || defender.hasType(PokeType.GROUND) || defender.hasType(PokeType.STEEL) ||
          ability in SAND_IMMUNE_ABILITIES
        if (!isImmune) EndOfTurnEffect(-(maxHp / 16), "sandstorm damage") else null
      }
      Weather.HAIL -> when {
        ability == Ability.ICE_BODY && !preventsHeal -> EndOfTurnEffect(maxHp / 16, "Ice Body recovery")
        !defender.hasType(PokeType.ICE) && ability !in HAIL_IMMUNE_ABILITIES -> EndOfTurnEffect(-(maxHp / 16), "hail damage")
        else -> null
      }
      Weather.SNOW -> if (ability == Ability.ICE_BODY && !preventsHeal) EndOfTurnEffect(maxHp / 16, "Ice Body recovery") else null
      Weather.NONE -> null
    }
  }

  private fun statusEffect(defender: BattlePokemon, preventsHeal: Boolean): EndOfTurnEffect? {
    val maxHp = defender.maxHp
    val ability = defender.resolvedAbility
    val hasMagicGuard = ability == Ability.MAGIC_GUARD
    return when (defender.status) {
      Status.POISONED -> when {
        ability == Ability.POISON_HEAL && !preventsHeal -> EndOfTurnEffect(maxHp / 8, "Poison Heal")
        !hasMagicGuard -> EndOfTurnEffect(-(maxHp / 8), "poison damage")
        else -> null
      }
      Status.BADLY_POISONED -> when {
        ability == Ability.POISON_HEAL && !preventsHeal -> EndOfTurnEffect(maxHp / 8, "Poison Heal")
        !hasMagicGuard -> EndOfTurnEffect(defender.toxicCounter, "toxic damage", isToxic = true)
        else -> null
      }
      Status.BURNED -> when {
        ability == Ability.HEATPROOF -> EndOfTurnEffect(-(maxHp / 32), "reduced burn damage")
        !hasMagicGuard -> EndOfTurnEffect(-(maxHp / 16), "burn damage")
        else -> null
      }
      Status.HEALTHY, Status.PARALYZED, Status.ASLEEP, Status.FROZEN -> null
    }
  }

  /** The damage the defender took from its side's hazards when it switched in. */
  fun hazards(defender: BattlePokemon, field: Battlefield): Hazards {
    val side = field.defenderSide
    val maxHp = defender.maxHp
    if (defender.resolvedAbility == Ability.MAGIC_GUARD) return Hazards(0, emptyList())
    var damage = 0
    val texts = mutableListOf<String>()
    if (side.hasStealthRock) {
      val typeChart = ChampionsDex.typeChart
      val effectiveness = typeChart.effectivenessOf(PokeType.ROCK, defender.species.primaryType) *
        (defender.species.secondaryType?.let { typeChart.effectivenessOf(PokeType.ROCK, it) } ?: 1.0)
      damage += maxOf(1, floor(effectiveness * maxHp / 8).toInt())
      texts += "Stealth Rock"
    }
    if (defender.isDefenderGrounded(field)) {
      when (side.spikesLayers) {
        1 -> {
          damage += maxOf(1, maxHp / 8)
          texts += "1 layer of Spikes"
        }
        2 -> {
          damage += maxHp / 6
          texts += "2 layers of Spikes"
        }
        3 -> {
          damage += maxHp / 4
          texts += "3 layers of Spikes"
        }
      }
    }
    return Hazards(damage, texts)
  }

  private val SAND_IMMUNE_ABILITIES = setOf(Ability.MAGIC_GUARD, Ability.OVERCOAT, Ability.SAND_FORCE, Ability.SAND_RUSH, Ability.SAND_VEIL)
  private val HAIL_IMMUNE_ABILITIES = setOf(Ability.MAGIC_GUARD, Ability.OVERCOAT, Ability.SNOW_CLOAK)
}
