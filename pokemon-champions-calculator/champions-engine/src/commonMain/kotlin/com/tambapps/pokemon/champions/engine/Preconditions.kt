package com.tambapps.pokemon.champions.engine

import com.tambapps.pokemon.champions.data.ChampionsCalcException

/** Like [require], but throws a [ChampionsCalcException] instead of a generic IllegalArgumentException. */
internal inline fun requireValid(condition: Boolean, lazyMessage: () -> String) {
  if (!condition) throw ChampionsCalcException(lazyMessage())
}
