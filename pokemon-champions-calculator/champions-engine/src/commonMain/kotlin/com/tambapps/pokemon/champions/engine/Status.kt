package com.tambapps.pokemon.champions.engine

enum class Status {
  HEALTHY, BURNED, PARALYZED, POISONED, BADLY_POISONED, ASLEEP, FROZEN;

  val isNonHealthy: Boolean get() = this != HEALTHY
}
