package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.engine.description.DescriptionBuilder
import kotlin.math.min

/** Ported from basePowerFunc, scoped to the moves that exist in Champions and have a formula-driven power. */
internal object BasePowerResolver {

  fun resolve(
    move: Move,
    moveUse: MoveUse,
    attacker: BattlePokemon,
    defender: BattlePokemon,
    field: Battlefield,
    description: DescriptionBuilder = DescriptionBuilder(),
  ): Int {
    val custom = customBasePower(move, moveUse, attacker, defender, field)
    val base = custom ?: move.basePower
    describeBasePower(move, base, moveUse, attacker, field, description)
    return if (move.hasEscalatingPower) base * moveUse.hitNumber else base
  }

  // the source's basePowerFunc description, for the custom powers ported above
  private fun describeBasePower(move: Move, power: Int, moveUse: MoveUse, attacker: BattlePokemon, field: Battlefield, description: DescriptionBuilder) {
    val isChanged = power != move.basePower
    when (move.name.value) {
      "Gyro Ball", "Electro Ball", "Low Kick", "Grass Knot", "Heavy Slam", "Heat Crash", "Eruption", "Water Spout",
      "Flail", "Reversal", "Hard Press", "Stored Power", "Power Trip" -> description.moveBP = power.toDouble()
      "Acrobatics", "Hex", "Infernal Parade", "Rising Voltage" -> if (isChanged) description.moveBP = power.toDouble()
      "Weather Ball" -> if (isChanged) {
        description.moveBP = power.toDouble()
        if (field.weather != Weather.NONE) description.weather(field.weather) else description.attackerAbility(attacker.resolvedAbility)
        description.moveType(effectiveTypeOf(move, attacker, field))
      }
      "Terrain Pulse" -> if (isChanged) {
        description.moveBP = power.toDouble()
        description.terrain(field.terrain)
        description.moveType(effectiveTypeOf(move, attacker, field))
      }
      "Last Respects", "Rage Fist" -> if (moveUse.priorPowerBoosts > 0) description.moveBP = power.toDouble()
    }
  }

  private fun customBasePower(move: Move, moveUse: MoveUse, attacker: BattlePokemon, defender: BattlePokemon, field: Battlefield): Int? =
    when (move.name.value) {
      "Gyro Ball" -> gyroBallPower(attacker, defender)
      "Electro Ball" -> electroBallPower(attacker, defender)
      "Low Kick", "Grass Knot" -> weightTierPower(defender.species.weightKg)
      "Heavy Slam", "Heat Crash" -> weightRatioPower(attacker.species.weightKg / defender.species.weightKg)
      "Eruption", "Water Spout" -> maxOf(1, 150 * attacker.hp / attacker.maxHp)
      "Flail", "Reversal" -> hpFractionPower(attacker.hp, attacker.maxHp)
      "Hard Press" -> hardPressPower(defender.hp, defender.maxHp)
      "Stored Power", "Power Trip" -> 20 + 20 * countPositiveBoosts(attacker.boosts)
      "Acrobatics" -> if (attacker.effectiveItem == null) 110 else 55
      "Hex", "Infernal Parade" -> move.basePower * (if (defender.status.isNonHealthy) 2 else 1)
      "Weather Ball" -> move.basePower * (if (field.weather != Weather.NONE || attacker.resolvedAbility == Ability.MEGA_SOL) 2 else 1)
      "Terrain Pulse" -> move.basePower * (if (field.terrain != Terrain.NONE && attacker.isGrounded(field)) 2 else 1)
      "Rising Voltage" -> move.basePower * (if (field.terrain == Terrain.ELECTRIC && defender.isGrounded(field)) 2 else 1)
      "Last Respects", "Rage Fist" -> move.basePower * (moveUse.priorPowerBoosts + 1)
      else -> null
    }

  private fun gyroBallPower(attacker: BattlePokemon, defender: BattlePokemon): Int {
    val attackerSpeed = attacker.boostedStat(BoostableStat.SPEED)
    val defenderSpeed = defender.boostedStat(BoostableStat.SPEED)
    return min(150, 25 * defenderSpeed / attackerSpeed + 1)
  }

  private fun electroBallPower(attacker: BattlePokemon, defender: BattlePokemon): Int {
    val attackerSpeed = attacker.boostedStat(BoostableStat.SPEED)
    val defenderSpeed = defender.boostedStat(BoostableStat.SPEED)
    val ratio = if (defenderSpeed == 0) 0 else attackerSpeed / defenderSpeed
    return when {
      ratio >= 4 -> 150
      ratio >= 3 -> 120
      ratio >= 2 -> 80
      ratio >= 1 -> 60
      else -> 40
    }
  }

  private fun weightTierPower(weightKg: Double): Int = when {
    weightKg >= 200 -> 120
    weightKg >= 100 -> 100
    weightKg >= 50 -> 80
    weightKg >= 25 -> 60
    weightKg >= 10 -> 40
    else -> 20
  }

  private fun weightRatioPower(ratio: Double): Int = when {
    ratio >= 5 -> 120
    ratio >= 4 -> 100
    ratio >= 3 -> 80
    ratio >= 2 -> 60
    else -> 40
  }

  private fun hpFractionPower(currentHp: Int, maxHp: Int): Int {
    val p = 48 * currentHp / maxHp
    return when {
      p <= 1 -> 200
      p <= 4 -> 150
      p <= 9 -> 100
      p <= 16 -> 80
      p <= 32 -> 40
      else -> 20
    }
  }

  private fun hardPressPower(defenderHp: Int, defenderMaxHp: Int): Int {
    val hpPer4096 = defenderHp * 0x1000 / defenderMaxHp
    val hpPercent = pokeRound(100 * 100 * hpPer4096, 0x1000) / 100
    return hpPercent
  }

  private fun countPositiveBoosts(boosts: StatBoosts): Int =
    listOf(boosts.attack, boosts.defense, boosts.specialAttack, boosts.specialDefense, boosts.speed)
      .filter { it > 0 }
      .sum()
}
