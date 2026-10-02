package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.champions.data.Ability
import com.tambapps.pokemon.champions.data.Item
import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.data.MoveCategory
import com.tambapps.pokemon.champions.engine.JsDict.Companion.isStarred
import com.tambapps.pokemon.champions.engine.JsDict.Companion.key
import com.tambapps.pokemon.champions.engine.JsDict.Companion.parseKey
import com.tambapps.pokemon.champions.engine.JsDict.Companion.sortByKeys
import com.tambapps.pokemon.champions.engine.JsDict.Companion.sortedKeys
import kotlin.math.floor

/**
 * The KO chance of a whole move, as the source calculator shows it after the damage: [text] is its exact text, e.g.
 * "guaranteed 3HKO after Sitrus Berry recovery" or "43.75% chance to 2HKO after Stealth Rock and Leftovers recovery".
 *
 * @param uses the number of uses of the move the text is about (1 for an OHKO, up to 9), null when the text isn't a
 * KO chance (a status move, no damage, an OHKO move, or a move too weak to KO in 9 uses)
 * @param chance the probability (0.0-1.0) of the KO in [uses] uses, 1.0 when guaranteed. Null when the text is only a
 * "possible" KO (5 uses and more, where the source only compares the lowest and highest rolls), or not a KO chance
 * @param kind what [text] is about, to tell apart the texts that aren't a KO chance without comparing them
 */
data class KoChance(val text: String, val uses: Int?, val chance: Double?, val kind: Kind = Kind.KO) {
  val isGuaranteed: Boolean get() = chance == 1.0

  enum class Kind {
    /** A KO in [uses] uses */
    KO,
    /** An OHKO move (Fissure, Sheer Cold...), whose damage isn't calculated */
    OHKO_MOVE,
    /** Pain Split, which deals no damage but shares the HP */
    PAIN_SPLIT,
    STATUS_MOVE,
    /** No roll deals damage (an immunity, or a fixed damage of 0) */
    NO_DAMAGE,
    /** Even the highest rolls don't KO in [MAX_KO_USES] uses ("possibly the worst move ever") */
    NO_KO_IN_MAX_USES,
  }

  companion object {
    /** The most uses of a move the KO chance is looked for in, like the source calculator */
    const val MAX_KO_USES = 9
  }
}

/**
 * The KO chance of a move and its text, ported from ko_chance.js's getKOChanceText and the text replacement of
 * ap_calc.js's calculate(), scoped to Champions (no Dynamax, no Gen 1 multi-hit rules). It counts the hazards the
 * defender switched in on, the end-of-turn effects between uses (weather, status, Leftovers, Leech Seed, Salt Cure,
 * Grassy Terrain...: see [EndOfTurnEffects]) and the HP restored by a Sitrus/Oran Berry once the defender drops to half
 * HP.
 *
 * The source's probability code keeps its damage totals in JS objects, keys suffixed with "*" once the berry was
 * eaten; it is ported as is, with [JsDict] reproducing a JS object's key order, including its quirks (e.g. a berry
 * eaten by an earlier hit counts for every later one, and toxic damage is counted from the same counter every use),
 * so that the chance and text match the source's to the last digit.
 */
internal object KoChanceCalculator {

  private const val OHKO_MOVE_TEXT = "is it a one-hit KO?!"
  private const val ROLL_PROBABILITY = 1.0 / 16

  /** The KO chance of a use of [move] whose hits deal [hitRolls] (the 16 rolls of each hit, in order). */
  fun koChance(hitRolls: List<List<Int>>, move: Move, attacker: BattlePokemon, defender: BattlePokemon, field: Battlefield): KoChance {
    requireValid(hitRolls.isNotEmpty()) { "a move has at least one hit" }
    // ap_calc.js shows this (as a link) instead of the KO chance of an OHKO move
    if (move.isOHKO) return KoChance(OHKO_MOVE_TEXT, uses = null, chance = null, KoChance.Kind.OHKO_MOVE)
    if (move.name.value == "Pain Split") {
      return KoChance("The battlers shared their pain!", uses = null, chance = null, KoChance.Kind.PAIN_SPLIT)
    }
    if (move.category == MoveCategory.STATUS) {
      return KoChance("It's a status move, it won't deal damage.", uses = null, chance = null, KoChance.Kind.STATUS_MOVE)
    }
    if (hitRolls.all { it.last() == 0 }) return KoChance("No damage for you", uses = null, chance = null, KoChance.Kind.NO_DAMAGE)

    val moveName = move.name.value
    val preventsHeal = moveName == "Psychic Noise"
    // the source's attacker.item === '': a Klutz attacker's item is "Klutz", so it isn't itemless
    val preventsHealItem = moveName == "Knock Off" || moveName == "Psychic Noise" ||
      ((moveName == "Thief" || moveName == "Covet") && !attacker.holdsItem)
    // Klutz makes the source's held item "Klutz", which is no berry
    val item = defender.effectiveItem
    val holdsBerry = item?.displayName?.contains(" Berry") == true
    // Incinerate also burns the berry in the source, but it isn't a Champions move
    val preventsRestoreHp = preventsHealItem || (holdsBerry && (moveName == "Bug Bite" || moveName == "Pluck"))
    var restoreHp = if (preventsRestoreHp) 0 else restoreHpOf(item, defender.maxHp)
    val isRipen = defender.resolvedAbility == Ability.RIPEN && holdsBerry && restoreHp != 0
    if (isRipen) restoreHp *= 2
    // Gluttony only changes the threshold of the pinch berries (Figy, Aguav...), which Champions doesn't have
    val restoreThreshold = if (restoreHp != 0) defender.maxHp / 2.0 else 0.0

    val curHp = defender.hp
    val maxHp = defender.maxHp
    val multihit = hitRolls.size > 1
    val damage = damageArrToDict(hitRolls, curHp, restoreHp, restoreThreshold)
    val damageNums = damage.keys.map(::parseKey)

    // the source compares a probability to the HP here, which only passes when every roll is the same and the defender has 1 HP left
    val firstProbability = damage[key(damageNums.first())]
    if ((!multihit || restoreHp == 0) && firstProbability != null && firstProbability >= curHp) {
      return KoChance("guaranteed OHKO", uses = 1, chance = 1.0)
    } else if (multihit && restoreHp != 0 && firstProbability != null && firstProbability >= curHp + restoreHp) {
      return KoChance("guaranteed OHKO", uses = 1, chance = 1.0)
    }

    val hazards = EndOfTurnEffects.hazards(defender, field)
    val hp = curHp - hazards.damage
    val effects = EndOfTurnEffects.of(defender, field, preventsHeal, preventsHealItem)
    var eot = 0
    val eotTexts = mutableListOf<String>()
    var toxicCounter = 0
    for (effect in effects) {
      if (effect.isToxic) {
        toxicCounter = effect.hp
        eot -= toxicCounter * maxHp / 16
      } else {
        eot += effect.hp
      }
      eotTexts += effect.text
    }

    val computation = Computation(multihit, maxHp, restoreHp, restoreThreshold, effects)
    var c = computation.koChance(damage, hp, 0, 1, toxicCounter)
    var afterText = if (hazards.texts.isNotEmpty()) " after " + serializeText(hazards.texts) else ""
    if (c == 1.0) {
      return KoChance("guaranteed OHKO$afterText", uses = 1, chance = 1.0)
    } else if (c > 0 && eot >= 0) {
      return KoChance("${percentText(c)}% chance to OHKO$afterText", uses = 1, chance = c)
    }

    if (restoreHp != 0) {
      eotTexts += (if (isRipen) "Ripen " else "") + item!!.displayName + " recovery"
    }

    afterText = if (hazards.texts.isNotEmpty() || eotTexts.isNotEmpty()) " after " + serializeText(hazards.texts + eotTexts) else ""
    for (uses in 1..4) {
      c = computation.koChance(damage, hp, eot, uses, toxicCounter)
      val koText = if (uses == 1) "OHKO" else "${uses}HKO"
      if (c == 1.0) {
        return KoChance("guaranteed $koText$afterText", uses = uses, chance = 1.0)
      } else if (c > 0) {
        return KoChance("${percentText(c)}% chance to $koText$afterText", uses = uses, chance = c)
      }
    }

    for (uses in 5..KoChance.MAX_KO_USES) {
      if (predictTotal(damageNums.first(), eot, uses, toxicCounter, hp, maxHp, restoreHp, restoreThreshold) >= hp) {
        return KoChance("guaranteed ${uses}HKO$afterText", uses = uses, chance = 1.0)
      } else if (predictTotal(damageNums.last(), eot, uses, toxicCounter, hp, maxHp, restoreHp, restoreThreshold) >= hp) {
        return KoChance("possible ${uses}HKO$afterText", uses = uses, chance = null)
      }
    }
    return KoChance("possibly the worst move ever", uses = null, chance = null, KoChance.Kind.NO_KO_IN_MAX_USES)
  }

  /** The source's getRestoreHP, for the restoring items Champions has. */
  private fun restoreHpOf(item: Item?, maxHp: Int): Int = when (item) {
    Item.ORAN_BERRY -> 10
    Item.SITRUS_BERRY -> maxHp / 4
    else -> 0
  }

  /** The source's damageArrToDict: the damage of a whole use, from the rolls of each of its hits. */
  private fun damageArrToDict(hitRolls: List<List<Int>>, currHp: Int, restoreHp: Int, restoreThreshold: Double): JsDict {
    // the source gets a single roll list for a move whose hits all deal the same, else one per hit; both add up the same
    var pivotSpread = arrayToProbabilityDict(hitRolls[if (hitRolls.size > 1) 1 else 0], currHp, restoreHp, restoreThreshold)
    var addedSpread = arrayToProbabilityDict(hitRolls[0], currHp, restoreHp, restoreThreshold)
    for (i in 0 until hitRolls.size - 1) {
      if (i != 0) {
        pivotSpread = arrayToProbabilityDict(hitRolls[minOf(i + 1, hitRolls.size - 1)], currHp, restoreHp, restoreThreshold)
      }
      val pivotKeys = pivotSpread.keys
      val tempSpread = JsDict()
      for (addedKey in addedSpread.keys) {
        val addedNum = parseKey(addedKey)
        for (pivotKey in pivotKeys) {
          val pivotNum = parseKey(pivotKey)
          val isStarred = checkThresholdCriteria(currHp, pivotNum, restoreHp, restoreThreshold, addedKey)
          tempSpread.add(key(pivotNum + addedNum, isStarred), pivotSpread.valueOf(pivotKey) * addedSpread.valueOf(addedKey))
        }
      }
      addedSpread = sortByKeys(tempSpread)
    }
    return addedSpread
  }

  private fun arrayToProbabilityDict(rolls: List<Int>, currHp: Int, restoreHp: Int, restoreThreshold: Double): JsDict {
    val dict = JsDict()
    for (roll in rolls) {
      dict.add(key(roll, checkThresholdCriteria(currHp, roll, restoreHp, restoreThreshold, 0)), ROLL_PROBABILITY)
    }
    return dict
  }

  /** Whether a [roll] after [prevCalcRolls] damage drops the defender to its berry's threshold, without KOing it. */
  private fun checkThresholdCriteria(currHp: Int, roll: Int, restoreHp: Int, restoreThreshold: Double, prevCalcRolls: Int): Boolean =
    restoreHp != 0 && currHp - prevCalcRolls - roll <= restoreThreshold && currHp - prevCalcRolls - roll > 0

  /** Same, after a damage key: once a key is starred, the berry was eaten, and every later total stays starred. */
  private fun checkThresholdCriteria(currHp: Int, roll: Int, restoreHp: Int, restoreThreshold: Double, prevCalcRolls: String): Boolean =
    isStarred(prevCalcRolls) || checkThresholdCriteria(currHp, roll, restoreHp, restoreThreshold, parseKey(prevCalcRolls))

  /** The inputs of the source's getKOChance, verifyKOChance and eotProcess that don't change between their calls. */
  private class Computation(
    val multihit: Boolean,
    val maxHp: Int,
    val restoreHp: Int,
    val restoreThreshold: Double,
    val effects: List<EndOfTurnEffect>,
  ) {

    /** The source's getKOChance: the chance to KO a defender with [hp] HP in [timesUsed] uses, [eotSum] being the net end-of-turn HP change. */
    fun koChance(damage: JsDict, hp: Int, eotSum: Int, timesUsed: Int, toxicCounter: Int): Double {
      val damageKeys = sortedKeys(damage)
      val firstDamage = damageKeys.first()
      val lastDamage = damageKeys.last()
      val minDamage = parseKey(firstDamage)
      val maxDamage = parseKey(lastDamage)
      if (timesUsed == 1) {
        if (((!multihit && !isStarred(lastDamage)) || restoreHp == 0) && maxDamage - eotSum < hp) {
          return 0.0
        } else if ((multihit || isStarred(lastDamage)) && restoreHp != 0 && maxDamage - eotSum < hp + restoreHp) {
          return 0.0
        } else if (((!multihit && !isStarred(firstDamage)) || restoreHp == 0) && minDamage - eotSum >= hp) {
          return 1.0
        } else if ((multihit || isStarred(firstDamage)) && restoreHp != 0 && minDamage - eotSum >= hp + restoreHp) {
          return 1.0
        }
      }
      val tempSpread = JsDict()
      for (damageKey in damageKeys) {
        var damageNum = parseKey(damageKey)
        var activateHealItem = isStarred(damageKey)
        if (eotSum != 0) {
          val (processed, activated) = eotProcess(damageNum, toxicCounter, hp, activateHealItem)
          damageNum = processed
          activateHealItem = activated
        }
        tempSpread.add(key(damageNum, activateHealItem), damage.valueOf(damageKey))
      }
      if (timesUsed == 1) {
        val sortedSpreadKeys = sortedKeys(tempSpread)
        if (parseKey(sortedSpreadKeys.last()) < hp) return 0.0
        val earlyTotalSpread = JsDict()
        for (spreadKey in sortedSpreadKeys) {
          val itemConsumed = isStarred(spreadKey)
          var finalNum = parseKey(spreadKey)
          if (itemConsumed) {
            finalNum -= restoreHp
            if (hp - finalNum > maxHp) finalNum = hp - maxHp
          }
          addToTotal(earlyTotalSpread, key(finalNum), tempSpread.valueOf(spreadKey), itemConsumed)
        }
        return koProbability(earlyTotalSpread, hp)
      }
      return verifyKoChance(damage, hp, eotSum, timesUsed, toxicCounter + 1, sortByKeys(tempSpread))
    }

    /** The source's verifyKOChance: [inSpread] is the damage after the first use and its end of turn. */
    private fun verifyKoChance(damage: JsDict, targetHp: Int, eotSum: Int, timesUsed: Int, toxicCounter: Int, inSpread: JsDict): Double {
      val pivotKeys = damage.keys
      var addedSpread = inSpread
      repeat(timesUsed - 1) {
        val tempSpread = JsDict()
        for (addedKey in addedSpread.keys) {
          val addedNum = parseKey(addedKey)
          for (pivotKey in pivotKeys) {
            val pivotNum = parseKey(pivotKey)
            var total = pivotNum + addedNum
            var activateHealItem = checkThresholdCriteria(targetHp, pivotNum, restoreHp, restoreThreshold, addedKey)
            if (eotSum != 0) {
              val (processed, activated) = eotProcess(total, toxicCounter, targetHp, activateHealItem)
              total = processed
              activateHealItem = activated
            }
            tempSpread.add(key(total, activateHealItem), damage.valueOf(pivotKey) * addedSpread.valueOf(addedKey))
          }
        }
        addedSpread = sortByKeys(tempSpread)
      }
      val totalSpread = JsDict()
      for (spreadKey in addedSpread.keys) {
        val itemConsumed = isStarred(spreadKey)
        var finalNum = parseKey(spreadKey)
        if (itemConsumed && timesUsed > 1) {
          finalNum -= restoreHp
          if (targetHp - finalNum > maxHp) finalNum = targetHp - maxHp
        }
        addToTotal(totalSpread, key(finalNum), addedSpread.valueOf(spreadKey), itemConsumed)
      }
      return koProbability(totalSpread, targetHp)
    }

    /** The source adds up the probability of a total after a consumed berry, but overwrites it otherwise. */
    private fun addToTotal(totalSpread: JsDict, key: String, probability: Double, itemConsumed: Boolean) {
      if (itemConsumed && key in totalSpread) totalSpread.add(key, probability) else totalSpread[key] = probability
    }

    private fun koProbability(totalSpread: JsDict, targetHp: Int): Double {
      var returnSum = 0.0
      var probabilitySum = 0.0
      for (finalKey in totalSpread.keys) {
        if (parseKey(finalKey) >= targetHp) returnSum += totalSpread.valueOf(finalKey)
        probabilitySum += totalSpread.valueOf(finalKey)
      }
      return if (returnSum == probabilitySum) 1.0 else returnSum
    }

    /** The source's eotProcess: the damage after the end of turn, and whether the berry was eaten. */
    private fun eotProcess(damageRoll: Int, toxicCounter: Int, targetHp: Int, activateHealItem: Boolean): Pair<Int, Boolean> {
      var damage = damageRoll
      var activated = activateHealItem
      for (effect in effects) {
        val eotApply = if (!effect.isToxic) effect.hp else -(toxicCounter * maxHp / 16)
        if (!activated && checkThresholdCriteria(targetHp, -eotApply, restoreHp, restoreThreshold, damage)) {
          activated = true
        }
        // the source only applies the end of turn to a defender still standing
        if ((activated && targetHp - damage + restoreHp > 0) || (!activated && targetHp - damage > 0)) {
          damage -= eotApply
        }
      }
      return damage to activated
    }
  }

  /** The source's predictTotal: the total damage of [timesUsed] uses all rolling [damage], for 5 uses and more. */
  private fun predictTotal(
    damage: Int,
    eot: Int,
    timesUsed: Int,
    toxicCounter: Int,
    hp: Int,
    maxHp: Int,
    restoreHp: Int,
    restoreThreshold: Double,
  ): Int {
    var total = 0
    var remainingRestoreHp = restoreHp
    for (i in 0 until timesUsed) {
      total += damage
      if (hp - total <= restoreThreshold && remainingRestoreHp != 0) {
        total -= remainingRestoreHp
        if (hp - total > maxHp) total = hp - maxHp
        remainingRestoreHp = 0
      }
      if (i < timesUsed - 1) {
        total -= eot
        if (toxicCounter > 0) total += (toxicCounter + i) * maxHp / 16
      }
    }
    return total
  }

  /** The source's serializeText: "a", "a and b", "a, b, and c". */
  private fun serializeText(texts: List<String>): String = when (texts.size) {
    0 -> ""
    1 -> texts[0]
    2 -> "${texts[0]} and ${texts[1]}"
    else -> texts.dropLast(1).joinToString(", ") + ", and " + texts.last()
  }

  /** The chance as the source writes it: Math.round(c * 10000) / 100, a number JS prints without trailing zeros. */
  private fun percentText(chance: Double): String = when {
    chance < 0.0001 -> "<0.01"
    chance > 0.9999 -> ">99.99"
    else -> {
      val hundredths = jsRound(chance * 10000)
      val integerPart = hundredths / 100
      val fraction = hundredths % 100
      when {
        fraction == 0L -> "$integerPart"
        fraction % 10 == 0L -> "$integerPart.${fraction / 10}"
        else -> "$integerPart.${fraction.toString().padStart(2, '0')}"
      }
    }
  }

  /** JS's Math.round: the closest integer, halves rounded up. */
  private fun jsRound(value: Double): Long {
    val floor = floor(value)
    return (if (value - floor >= 0.5) floor + 1 else floor).toLong()
  }
}
