package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.AbilityName
import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.Stat
import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.HitCount
import com.tambapps.pokemon.champions.data.Item
import com.tambapps.pokemon.champions.data.MoveCategory
import com.tambapps.pokemon.champions.engine.DamageCalculator.asParentalBondChild
import com.tambapps.pokemon.champions.engine.description.CalcFactsBuilder
import com.tambapps.pokemon.champions.engine.description.StatDisplay
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Ported from checkAddCalcQualifications and additionalDamageCalcs, scoped to Champions: the hits of a move after its
 * first one, accounting for what the previous hits changed. Parental Bond's second hit (with its move's stat change),
 * Triple Axel's growing power, a resist berry consumed or Multiscale broken by the first hit, and each hit's Weak Armor,
 * Gooey, Stamina or Spicy Spray. Like the source, those abilities are the defender's own, even when Mold Breaker
 * ignored them for the hit itself.
 */
internal object AdditionalHits {

  private val BURN_HEALING_BERRIES = setOf(Item.RAWST_BERRY, Item.LUM_BERRY)
  private val BURN_IMMUNE_ABILITIES = setOf(Ability.WATER_BUBBLE, Ability.THERMAL_EXCHANGE, Ability.PURIFYING_SALT)

  /** Every hit of the move, the first one being [first], recording the facts the whole move's description mentions. */
  fun hitsOf(
    battle: PreparedBattle,
    moveUse: MoveUse,
    hits: Int,
    first: HitCalc,
    facts: CalcFactsBuilder,
    statDisplay: StatDisplay,
  ): List<DamageResult> {
    val (attacker, defender, field) = battle
    val move = moveUse.move
    val isParentalBond = attacker.resolvedAbility == Ability.PARENTAL_BOND && move.hitCount == HitCount.Once &&
      (field.format == BattleFormat.SINGLES || !isSpreadHit(move, attacker, field))
    if (hits <= 1 && !isParentalBond) return listOf(first.result)
    val totalHits = if (isParentalBond) 2 else hits

    val isTriple = move.hasEscalatingPower && !isParentalBond
    val isMultiscale = defender.resolvedAbility == Ability.MULTISCALE && defender.hp == defender.maxHp
    val isWeakArmor = defender.resolvedAbility == Ability.WEAK_ARMOR && first.hitsPhysical && defender.boosts.defense > -6
    val isGooey = defender.resolvedAbility == Ability.GOOEY && makesContact(move, attacker, first.effectiveCategory)
    val isStamina = defender.resolvedAbility == Ability.STAMINA && first.hitsPhysical && defender.boosts.defense < 6
    val isSpicySpray = defender.resolvedAbility == Ability.SPICY_SPRAY && first.effectiveCategory == MoveCategory.PHYSICAL &&
      canBeBurned(attacker, effectiveTypeOf(move, attacker, field), field)
    val gooeyBoostStat = when (attacker.resolvedAbility) {
      Ability.DEFIANT -> BoostableStat.ATTACK
      Ability.COMPETITIVE -> BoostableStat.SPECIAL_ATTACK
      else -> null
    }

    var nextAttacker = attacker
    var nextDefender = defender
    var nextMoveUse = moveUse
    var uniqueHits = 1
    if (isParentalBond) {
      nextAttacker = nextAttacker.asParentalBondChild()
      nextMoveUse = nextMoveUse.copy(isSecondParentalBondHit = true)
      val statChange = move.statChange
      if (statChange != null) {
        val stat = statChange.stat.toBoostableStat()
        if (statChange.affectsUser) {
          nextAttacker = nextAttacker.withBoost(stat, nextAttacker.boosts[stat] + statChange.stages)
        } else {
          nextDefender = nextDefender.withBoost(stat, nextDefender.boosts[stat] + statChange.stages)
        }
      } else if (move.name.value == "Assurance") {
        nextMoveUse = nextMoveUse.copy(isPowerDoubled = true)
      }
      facts.attackerAbility(attacker.resolvedAbility)
      facts.hits = 2
      uniqueHits = 2
    } else if (isTriple) {
      uniqueHits = totalHits
    }

    var burnHealConsumed = false
    if (isMultiscale) {
      nextDefender = nextDefender.copy(ability = AbilityName(""))
      if (uniqueHits == 1) uniqueHits = 2
    } else if (isWeakArmor) {
      uniqueHits = max(
        uniqueHits,
        max(min(defender.boosts.defense + 7, totalHits), min(ceil((6 - defender.boosts.speed) / 2.0).toInt() + 1, totalHits)),
      )
      facts.defenderAbility(defender.resolvedAbility)
    } else if (isGooey) {
      uniqueHits = max(uniqueHits, min(attacker.boosts.speed + 7, totalHits))
      facts.defenderAbility(defender.resolvedAbility)
      if (gooeyBoostStat != null) {
        uniqueHits = max(uniqueHits, min(ceil((6 - attacker.boosts[gooeyBoostStat]) / 2.0).toInt() + 1, totalHits))
        facts.attackerAbility(attacker.resolvedAbility)
      }
    } else if (isStamina) {
      uniqueHits = max(uniqueHits, min(6 - defender.boosts.defense + 1, totalHits))
      facts.defenderAbility(defender.resolvedAbility)
    } else if (isSpicySpray) {
      if (attacker.effectiveItem in BURN_HEALING_BERRIES) {
        burnHealConsumed = true
        facts.attackerItem(attacker.effectiveItem)
        if (totalHits >= 3) uniqueHits = 3
      } else {
        nextAttacker = nextAttacker.copy(status = Status.BURNED)
        if (uniqueHits == 1) uniqueHits = 2
      }
      facts.defenderAbility(defender.resolvedAbility)
    }
    if (facts.consumedResistBerry) {
      nextDefender = nextDefender.copy(item = null)
      if (uniqueHits == 1) uniqueHits = 2
    }

    val computedHits = mutableListOf(first.result)
    for (i in 0 until uniqueHits - 1) {
      if (isGooey) {
        nextAttacker = nextAttacker.withBoost(BoostableStat.SPEED, attacker.boosts.speed - 1)
        if (gooeyBoostStat != null) nextAttacker = nextAttacker.withBoost(gooeyBoostStat, attacker.boosts[gooeyBoostStat] + 2)
      } else if (isWeakArmor) {
        nextDefender = nextDefender.withBoost(BoostableStat.SPEED, nextDefender.boosts.speed + 2)
          .withBoost(BoostableStat.DEFENSE, nextDefender.boosts.defense - 1)
      } else if (isStamina) {
        nextDefender = nextDefender.withBoost(BoostableStat.DEFENSE, nextDefender.boosts.defense + 1)
      }
      if (isTriple) nextMoveUse = nextMoveUse.copy(hitNumber = i + 2)
      val nextBattle = PreparedBattle(nextAttacker, nextDefender, field)
      computedHits += DamageCalculator.calculateHit(nextBattle, nextMoveUse, DamageCalculator.newFacts(nextBattle, nextMoveUse), statDisplay).result
      if (burnHealConsumed) {
        burnHealConsumed = false
        nextAttacker = nextAttacker.copy(item = null, status = Status.BURNED)
      }
    }
    // the hits past the last one computed are the same as it
    return List(totalHits) { computedHits[min(it, computedHits.lastIndex)] }
  }

  private fun canBeBurned(attacker: BattlePokemon, effectiveType: PokeType, field: Battlefield): Boolean =
    attacker.status != Status.BURNED &&
      !attacker.hasType(PokeType.FIRE) &&
      !((attacker.resolvedAbility == Ability.PROTEAN || attacker.resolvedAbility == Ability.LIBERO) && attacker.abilityIsActive && effectiveType == PokeType.FIRE) &&
      !(attacker.resolvedAbility == Ability.LEAF_GUARD && field.weather == Weather.SUN) &&
      attacker.resolvedAbility !in BURN_IMMUNE_ABILITIES &&
      (field.terrain != Terrain.MISTY || !attacker.isGrounded(field))

  private fun Stat.toBoostableStat() = when (this) {
    Stat.ATTACK -> BoostableStat.ATTACK
    Stat.DEFENSE -> BoostableStat.DEFENSE
    Stat.SPECIAL_ATTACK -> BoostableStat.SPECIAL_ATTACK
    Stat.SPECIAL_DEFENSE -> BoostableStat.SPECIAL_DEFENSE
    Stat.SPEED -> BoostableStat.SPEED
    Stat.HP -> error("HP has no boost stages")
  }
}
