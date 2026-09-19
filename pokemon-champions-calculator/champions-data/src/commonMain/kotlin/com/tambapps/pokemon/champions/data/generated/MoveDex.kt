package com.tambapps.pokemon.champions.data.generated

import com.tambapps.pokemon.MoveName
import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.PokemonNormalizer
import com.tambapps.pokemon.champions.data.HitCount
import com.tambapps.pokemon.champions.data.Move
import com.tambapps.pokemon.champions.data.MoveCategory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class MoveDto(
  val bp: Int = 0,
  val type: String,
  val category: String,
  val makesContact: Boolean = false,
  val isSound: Boolean = false,
  val isBullet: Boolean = false,
  val isPulse: Boolean = false,
  val isBite: Boolean = false,
  val isPunch: Boolean = false,
  val isSlice: Boolean = false,
  val isSpread: Boolean = false,
  val isOHKO: Boolean = false,
  val hasSecondaryEffect: Boolean = false,
  val ignoresBurn: Boolean = false,
  val ignoresDefenseBoosts: Boolean = false,
  val ignoresScreens: Boolean = false,
  val dealsPhysicalDamage: Boolean = false,
  val hitRange: List<Int>? = null,
  val isTripleHit: Boolean = false,
  val alwaysCrit: Boolean = false,
  val canDouble: Boolean = false,
  val linearAddBP: Boolean = false,
  val isPriority: Boolean = false,
  val recoilHP: List<Int>? = null,
  val hasCrash: Boolean = false,
)

private fun MoveDto.toDomain(name: String) = Move(
  name = MoveName(name),
  type = PokeType.valueOf(type.uppercase()),
  category = MoveCategory.valueOf(category.uppercase()),
  basePower = bp,
  hitCount = hitRange?.let { (min, max) -> if (min == max) HitCount.Fixed(min) else HitCount.Variable(min, max) }
    ?: HitCount.Once,
  makesContact = makesContact,
  isSound = isSound,
  isBullet = isBullet,
  isPulse = isPulse,
  isBite = isBite,
  isPunch = isPunch,
  isSlice = isSlice,
  isSpread = isSpread,
  isOHKO = isOHKO,
  hasSecondaryEffect = hasSecondaryEffect,
  ignoresBurn = ignoresBurn,
  ignoresDefenseBoosts = ignoresDefenseBoosts,
  ignoresScreens = ignoresScreens,
  dealsPhysicalDamage = dealsPhysicalDamage,
  alwaysCrits = alwaysCrit,
  hasEscalatingPower = isTripleHit,
  canBePowerDoubled = canDouble,
  hasPriority = isPriority,
  hasRecoil = recoilHP != null || hasCrash,
)

/** Every move legal in the Champions format, keyed by normalized name, parsed once on first access. */
internal val ALL_MOVES: Map<String, Move> by lazy {
  Json.decodeFromString<Map<String, MoveDto>>(MOVE_JSON).entries.associate { (name, dto) ->
    PokemonNormalizer.normalize(name) to dto.toDomain(name)
  }
}
