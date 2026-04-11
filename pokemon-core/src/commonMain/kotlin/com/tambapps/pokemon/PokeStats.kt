package com.tambapps.pokemon

enum class Stat {
  ATTACK,
  DEFENSE,
  SPECIAL_ATTACK,
  SPECIAL_DEFENSE,
  SPEED,
  HP
}

data class PokeStats(
  val hp: Int,
  val speed: Int,
  val attack: Int,
  val specialAttack: Int,
  val defense: Int,
  val specialDefense: Int,
) {
  companion object {
    fun default(defaultValue: Int) =
      PokeStats(defaultValue, defaultValue, defaultValue, defaultValue, defaultValue, defaultValue)

    fun compute(
      pokemon: Pokemon,
      baseStats: PokeStats,
      legacySystem: Boolean
    ) = when {
      legacySystem -> computeLegacy(pokemon, baseStats)
      else -> TODO()
    }

    fun compute(
      baseStats: PokeStats,
      evs: PokeStats,
      ivs: PokeStats = default(31),
      nature: Nature,
      level: Int,
      legacySystem: Boolean) = when {
      legacySystem -> computeLegacy(baseStats = baseStats, evs = evs, ivs = ivs, nature = nature, level = level)
      else -> TODO()
    }

    fun computeLegacy(
      pokemon: Pokemon,
      baseStats: PokeStats,
    ) = computeLegacy(baseStats = baseStats, evs = pokemon.evs, ivs = pokemon.ivs, nature = pokemon.nature ?: Nature.QUIRKY, level = pokemon.level)

    fun computeLegacy(
      baseStats: PokeStats,
      evs: PokeStats,
      ivs: PokeStats = default(31),
      nature: Nature,
      level: Int) = PokeStats(
      hp = computeLegacyHpStat(base = baseStats.hp, ev = evs.hp, iv = ivs.hp, level = level),
      speed = computeLegacyStat(stat = Stat.SPEED, base = baseStats.speed, ev = evs.speed, iv = ivs.speed, nature = nature, level = level),
      attack = computeLegacyStat(stat = Stat.ATTACK, base = baseStats.attack, ev = evs.attack, iv = ivs.attack, nature = nature, level = level),
      specialAttack = computeLegacyStat(stat = Stat.SPECIAL_ATTACK, base = baseStats.specialAttack, ev = evs.specialAttack, iv = ivs.specialAttack, nature = nature, level = level),
      defense = computeLegacyStat(stat = Stat.DEFENSE, base = baseStats.defense, ev = evs.defense, iv = ivs.defense, nature = nature, level = level),
      specialDefense = computeLegacyStat(stat = Stat.SPECIAL_DEFENSE, base = baseStats.specialDefense, ev = evs.specialDefense, iv = ivs.specialDefense, nature = nature, level = level),
    )
  }

  fun all(predicate: (Int) -> Boolean) = Stat.entries.all { predicate(get(it)) }

  fun any(predicate: (Int) -> Boolean) = Stat.entries.any { predicate(get(it)) }

  operator fun get(stat: Stat) = when (stat) {
    Stat.ATTACK -> attack
    Stat.DEFENSE -> defense
    Stat.SPECIAL_ATTACK -> specialAttack
    Stat.SPECIAL_DEFENSE -> specialDefense
    Stat.SPEED -> speed
    Stat.HP -> hp
  }
}

fun buildStats(defaultValue: Int, builder: PokeStatsBuilder.() -> Unit): PokeStats =
  PokeStatsBuilder(defaultValue).apply(builder).run {
    PokeStats(
      attack = attack,
      speed = speed,
      specialAttack = specialAttack,
      defense = defense,
      specialDefense = specialDefense,
      hp = hp
    )
  }

class PokeStatsBuilder(defaultValue: Int) {
  var hp = defaultValue
  var speed = defaultValue
  var attack = defaultValue
  var specialAttack = defaultValue
  var defense = defaultValue
  var specialDefense = defaultValue
}
