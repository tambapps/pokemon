package com.tambapps.pokemon.champions.data.generated

import com.tambapps.pokemon.PokeType
import com.tambapps.pokemon.champions.data.TypeChart
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** The Generation IX+ type effectiveness matrix, parsed once on first access. Struggle (the only Typeless move) is handled separately by the engine, not through this chart. */
internal val CHAMPIONS_TYPE_CHART: TypeChart by lazy {
  val root = Json.parseToJsonElement(TYPE_CHART_JSON).jsonObject
  val multipliers = root.mapKeys { (type, _) -> PokeType.valueOf(type.uppercase()) }
    .mapValues { (_, row) ->
      row.jsonObject.mapKeys { (type, _) -> PokeType.valueOf(type.uppercase()) }
        .mapValues { (_, multiplier) -> multiplier.jsonPrimitive.double }
    }
  TypeChart(multipliers)
}
