package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Item

/** The attacker, defender and field of a calc, once [BattleSetup] applied what happens before any move. */
internal data class PreparedBattle(val attacker: BattlePokemon, val defender: BattlePokemon, val field: Battlefield)

/**
 * Ported from the setup pass CALCULATE_ALL_MOVES_SV runs before any damage calc, in the same order, scoped to what's
 * legal in Champions: Trace, Cloud Nine, Forecast, Mimicry, terrain seeds, Intimidate, Supersweet Syrup, Infiltrator
 * and Heavy/Light Metal. Klutz is handled by [BattlePokemon.effectiveItem] and the speeds by [SpeedCalculator].
 * Abilities the source toggles (Trace, Intimidate, Supersweet Syrup) apply when [BattlePokemon.abilityIsActive].
 */
internal object BattleSetup {

  // the abilities Trace can't copy, among those legal in Champions
  private val UNCOPYABLE_ABILITIES = setOf(
    Ability.DISGUISE, Ability.FORECAST, Ability.ILLUSION, Ability.IMPOSTER, Ability.RECEIVER,
    Ability.STANCE_CHANGE, Ability.TRACE, Ability.ZERO_TO_HERO,
  )
  private val INTIMIDATE_IMMUNE_ABILITIES = setOf(
    Ability.CLEAR_BODY, Ability.WHITE_SMOKE, Ability.HYPER_CUTTER,
    Ability.INNER_FOCUS, Ability.OBLIVIOUS, Ability.OWN_TEMPO, Ability.SCRAPPY,
  )
  private val TERRAIN_SEEDS = mapOf(
    Terrain.ELECTRIC to Item.ELECTRIC_SEED,
    Terrain.GRASSY to Item.GRASSY_SEED,
    Terrain.MISTY to Item.MISTY_SEED,
    Terrain.PSYCHIC to Item.PSYCHIC_SEED,
  )

  fun prepare(attacker: BattlePokemon, defender: BattlePokemon, field: Battlefield): PreparedBattle {
    var p1 = trace(attacker, defender)
    var p2 = trace(defender, p1)

    var battlefield = field
    if (p1.resolvedAbility == Ability.CLOUD_NINE || p2.resolvedAbility == Ability.CLOUD_NINE) {
      battlefield = battlefield.copy(weather = Weather.NONE)
    }
    p1 = mimicry(forecast(p1, battlefield), battlefield)
    p2 = mimicry(forecast(p2, battlefield), battlefield)
    p1 = terrainSeed(p1, battlefield)
    p2 = terrainSeed(p2, battlefield)

    intimidate(p1, p2).let { (source, target) -> p1 = source; p2 = target }
    intimidate(p2, p1).let { (source, target) -> p2 = source; p1 = target }
    p2 = supersweetSyrup(p1, p2)
    p1 = supersweetSyrup(p2, p1)

    if (p1.resolvedAbility == Ability.INFILTRATOR) {
      battlefield = battlefield.copy(
        defenderSide = battlefield.defenderSide.copy(hasReflect = false, hasLightScreen = false, hasAuroraVeil = false),
      )
    }
    return PreparedBattle(weightMods(p1), weightMods(p2), battlefield)
  }

  private fun trace(source: BattlePokemon, target: BattlePokemon): BattlePokemon {
    val copies = source.resolvedAbility == Ability.TRACE && source.abilityIsActive && target.resolvedAbility !in UNCOPYABLE_ABILITIES
    return if (copies) source.copy(ability = target.ability) else source
  }

  private fun forecast(pokemon: BattlePokemon, field: Battlefield): BattlePokemon {
    if (pokemon.resolvedAbility != Ability.FORECAST || pokemon.species.name.value != "Castform") return pokemon
    val type = when (field.weather) {
      Weather.SUN -> PokeType.FIRE
      Weather.RAIN -> PokeType.WATER
      Weather.HAIL, Weather.SNOW -> PokeType.ICE
      else -> PokeType.NORMAL
    }
    return pokemon.withType(type)
  }

  private fun mimicry(pokemon: BattlePokemon, field: Battlefield): BattlePokemon {
    if (pokemon.resolvedAbility != Ability.MIMICRY || field.terrain == Terrain.NONE) return pokemon
    val type = when (field.terrain) {
      Terrain.ELECTRIC -> PokeType.ELECTRIC
      Terrain.GRASSY -> PokeType.GRASS
      Terrain.MISTY -> PokeType.FAIRY
      else -> PokeType.PSYCHIC
    }
    return pokemon.withType(type)
  }

  private fun BattlePokemon.withType(type: PokeType) = copy(species = species.copy(primaryType = type, secondaryType = null))

  // the seed is consumed: +1 Defense in Electric/Grassy Terrain, +1 Sp. Def in Misty/Psychic Terrain
  private fun terrainSeed(pokemon: BattlePokemon, field: Battlefield): BattlePokemon {
    if (field.terrain == Terrain.NONE || pokemon.effectiveItem != TERRAIN_SEEDS[field.terrain]) return pokemon
    val stat = if (field.terrain == Terrain.ELECTRIC || field.terrain == Terrain.GRASSY) BoostableStat.DEFENSE else BoostableStat.SPECIAL_DEFENSE
    return pokemon.withBoost(stat, pokemon.boosts[stat] + 1).copy(item = null)
  }

  /** [source]'s Intimidate on [target], returning both since Mirror Armor turns it back on [source]. */
  private fun intimidate(source: BattlePokemon, target: BattlePokemon): Pair<BattlePokemon, BattlePokemon> {
    if (source.resolvedAbility != Ability.INTIMIDATE || !source.abilityIsActive) return source to target
    var intimidator = source
    var intimidated = target
    val attack = target.boosts.attack
    when (target.resolvedAbility) {
      Ability.CONTRARY, Ability.GUARD_DOG -> intimidated = intimidated.withBoost(BoostableStat.ATTACK, attack + 1)
      in INTIMIDATE_IMMUNE_ABILITIES -> Unit
      Ability.MIRROR_ARMOR -> intimidator = intimidator.withBoost(BoostableStat.ATTACK, source.boosts.attack - 1)
      else -> {
        val drop = if (target.resolvedAbility == Ability.SIMPLE) 2 else 1
        intimidated = intimidated.withBoost(BoostableStat.ATTACK, attack - drop)
        if (target.resolvedAbility == Ability.DEFIANT) {
          intimidated = intimidated.withBoost(BoostableStat.ATTACK, intimidated.boosts.attack + 2)
        } else if (target.resolvedAbility == Ability.COMPETITIVE) {
          intimidated = intimidated.withBoost(BoostableStat.SPECIAL_ATTACK, intimidated.boosts.specialAttack + 2)
        }
      }
    }
    if (target.resolvedAbility == Ability.RATTLED) {
      intimidated = intimidated.withBoost(BoostableStat.SPEED, intimidated.boosts.speed + 1)
    }
    return intimidator to intimidated
  }

  private fun supersweetSyrup(source: BattlePokemon, target: BattlePokemon): BattlePokemon {
    if (source.resolvedAbility != Ability.SUPERSWEET_SYRUP || !source.abilityIsActive) return target
    return when (target.resolvedAbility) {
      Ability.DEFIANT -> target.withBoost(BoostableStat.ATTACK, target.boosts.attack + 2)
      // as the source: Competitive raises Attack by 2 from the Sp. Atk boost
      Ability.COMPETITIVE -> target.withBoost(BoostableStat.ATTACK, target.boosts.specialAttack + 2)
      else -> target
    }
  }

  private fun weightMods(pokemon: BattlePokemon): BattlePokemon = when (pokemon.resolvedAbility) {
    Ability.HEAVY_METAL -> pokemon.copy(species = pokemon.species.copy(weightKg = pokemon.species.weightKg * 2))
    Ability.LIGHT_METAL -> pokemon.copy(species = pokemon.species.copy(weightKg = pokemon.species.weightKg / 2))
    else -> pokemon
  }
}
