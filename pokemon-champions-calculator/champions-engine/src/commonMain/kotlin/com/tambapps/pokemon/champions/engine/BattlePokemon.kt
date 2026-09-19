package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.AbilityName
import com.tambapps.pokemon.Gender
import com.tambapps.pokemon.ItemName
import com.tambapps.pokemon.Nature
import com.tambapps.pokemon.PokeStats
import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Item
import com.tambapps.pokemon.champions.data.PokemonSpecies

/**
 * A Pokemon as it stands mid-battle: everything the damage formula needs about one side of a
 * matchup. Stat stage boosts, status and current HP describe the moment being calculated for,
 * not the Pokemon's base configuration.
 *
 * [ability] and [item] are the general-purpose [AbilityName]/[ItemName] types used everywhere
 * else in this project (e.g. straight from a parsed pokepaste), not this engine's closed
 * [Ability]/[Item] enums -- callers shouldn't have to know or care whether Champions recognizes
 * a given name. [resolvedAbility]/[resolvedItem] do that resolution once, lazily, and every
 * comparison inside the engine reads those instead of re-resolving on every check.
 */
data class BattlePokemon(
  val species: PokemonSpecies,
  val ability: AbilityName,
  val nature: Nature,
  val statPoints: PokeStats,
  val item: ItemName? = null,
  val gender: Gender = Gender.ASEXUAL,
  val boosts: StatBoosts = StatBoosts.NONE,
  val status: Status = Status.HEALTHY,
  /** Null means "at full health". Set this to model Reversal/Flail/Eruption-style moves or a target already worn down. */
  val currentHp: Int? = null,
  /**
   * Mirrors the calculator's "ability on" toggle for abilities whose effect is situational
   * rather than passive, e.g. Flash Fire once triggered, Stakeout on the turn a Pokemon
   * switches in, or Electromorphosis after being hit by a contact move.
   */
  val abilityIsActive: Boolean = false,
  /** True if this Pokemon used Glaive Rush on its last turn, making it take double damage until its next move. */
  val isVulnerableFromGlaiveRush: Boolean = false,
) {
  /** [ability] resolved to Champions' closed ability set; null if [ability] isn't legal here, which the engine treats as no special ability. */
  val resolvedAbility: Ability? by lazy { Ability.from(ability) }

  /** [item] resolved to Champions' closed item set; null if there's no item, or [item] isn't legal here, which the engine treats as no held-item effect. */
  val resolvedItem: Item? by lazy { item?.let(Item::from) }

  val stats: PokeStats = PokeStats.compute(baseStats = species.baseStats, statPoints = statPoints, nature = nature)

  val maxHp: Int get() = stats.hp
  val hp: Int get() = currentHp ?: maxHp

  /** [resolvedItem], unless Klutz is suppressing it -- Klutz disables essentially every held item effect. */
  val effectiveItem: Item? get() = if (resolvedAbility == Ability.KLUTZ) null else resolvedItem

  fun hasType(type: PokeType): Boolean = species.hasType(type)

  /** The stat as boosted by [boosts]; HP has no boost stages, so it is not offered here. */
  fun boostedStat(stat: BoostableStat): Int = applyBoostStage(stats[stat.toStat()], boosts[stat])
}
