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
      baseStats: PokeStats,
      evs: PokeStats,
      ivs: PokeStats = default(31),
      nature: Nature = Nature.QUIRKY,
      level: Int) = PokeStats(
      hp = computeHpStat(base = baseStats.hp, ev = evs.hp, iv = ivs.hp, level = level),
      speed = computeStat(stat = Stat.SPEED, base = baseStats.speed, ev = evs.speed, iv = ivs.speed, nature = nature, level = level),
      attack = computeStat(stat = Stat.ATTACK, base = baseStats.attack, ev = evs.attack, iv = ivs.attack, nature = nature, level = level),
      specialAttack = computeStat(stat = Stat.SPECIAL_ATTACK, base = baseStats.specialAttack, ev = evs.specialAttack, iv = ivs.specialAttack, nature = nature, level = level),
      defense = computeStat(stat = Stat.DEFENSE, base = baseStats.defense, ev = evs.defense, iv = ivs.defense, nature = nature, level = level),
      specialDefense = computeStat(stat = Stat.SPECIAL_DEFENSE, base = baseStats.specialDefense, ev = evs.specialDefense, iv = ivs.specialDefense, nature = nature, level = level),
    )

  }

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

private fun computeCoreStat(
  base: Int,
  ev: Int,
  iv: Int,
  level: Int
) = ((2f * base + iv + (ev / 4)) * level) / 100f

private fun computeStat(
  stat: Stat,
  base: Int,
  ev: Int,
  iv: Int,
  nature: Nature,
  level: Int
) = (
    (computeCoreStat(base=base, ev=ev, iv=iv, level=level) + 5) * nature.coefficient(stat)
    ).toInt()

private fun Nature.coefficient(stat: Stat) = when {
  bonusStat == stat -> 1.1f
  malusStat == stat -> 0.9f
  else -> 1f
}

private fun computeHpStat(
  base: Int,
  ev: Int,
  iv: Int,
  level: Int
) = when {
  base > 1 -> (computeCoreStat(base=base, ev=ev, iv=iv, level=level) + level + 10).toInt()
  else -> 1
}