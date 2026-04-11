package com.tambapps.pokemon

/*
 * Non-legacy system (level 50)
 * HP    = base + statPoints + 75
 * Other = floor(nature × (base + statPoints + 20))
 */
internal fun computeHpStat(
  base: Int,
  statPoints: Int,
) = base + statPoints + 75

internal fun computeStat(
  stat: Stat,
  base: Int,
  statPoints: Int,
  nature: Nature,
) = (nature.coefficient(stat) * (base + statPoints + 20)).toInt()


/*
 * Legacy system
 */
internal fun computeLegacyCoreStat(
  base: Int,
  ev: Int,
  iv: Int,
  level: Int
) = ((2f * base + iv + (ev / 4)) * level) / 100f

internal fun computeLegacyStat(
  stat: Stat,
  base: Int,
  ev: Int,
  iv: Int,
  nature: Nature,
  level: Int
) = (
    (computeLegacyCoreStat(base=base, ev=ev, iv=iv, level=level) + 5) * nature.coefficient(stat)
    ).toInt()

internal fun Nature.coefficient(stat: Stat) = when {
  bonusStat == stat -> 1.1f
  malusStat == stat -> 0.9f
  else -> 1f
}

internal fun computeLegacyHpStat(
  base: Int,
  ev: Int,
  iv: Int,
  level: Int
) = when {
  base > 1 -> (computeLegacyCoreStat(base=base, ev=ev, iv=iv, level=level) + level + 10).toInt()
  else -> 1
}