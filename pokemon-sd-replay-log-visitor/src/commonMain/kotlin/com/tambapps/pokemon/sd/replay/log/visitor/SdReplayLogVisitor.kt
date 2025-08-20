package com.tambapps.pokemon.sd.replay.log.visitor

interface SdReplayLogVisitor {

  companion object {
    const val PLAYER_1 = "p1"
    const val PLAYER_2 = "p2"

    private const val LOG_MOVE = "move"
    private const val LOG_POKE = "poke"
    private const val LOG_UHTML = "uhtml"
    private const val LOG_RAW = "raw"
    private const val LOG_DRAG = "drag"
    private const val LOG_SWITCH = "switch"
    private const val LOG_TERASTALLIZE = "-terastallize"
    private const val LOG_WIN = "win"
    private const val LOG_SHOW_TEAM = "showteam"
  }

  fun visitAll(logs: String) = visitAll(logs.trim().lines())

  fun visitAll(logs: List<String>) {
    for (log in logs) {
      visit(log)
    }
  }
  fun visit(log: String) {
    val tokens = log.split("|")
    if (tokens.size < 2) return
    when (tokens[1]) {
      LOG_MOVE -> {
        val sourceFields = tokens[2].split(':')
        val sourcePokemonSlot = sourceFields.first()
        val sourcePokemonName = formatPokemonName(sourceFields.last().trim())
        val moveName = formatPokemonTrait(tokens[3].trim())

        val targetFields = tokens[4].split(':')
        val targetPokemonSlot = targetFields.first()
        val targetPokemonName = formatPokemonName(targetFields.last().trim())

        val isSpread = tokens.size >= 6 && tokens[5].startsWith("[spread]")
        visitMoveLog(sourcePokemonSlot, sourcePokemonName, moveName, targetPokemonSlot, targetPokemonName, isSpread)
      }
    }
  }

  fun visitMoveLog(sourcePokemonSlot: String, sourcePokemonName: String, moveName: String, targetPokemonSlot: String, targetPokemonName: String, isSpread: Boolean) {}

  fun formatPokemonName(name: String): String = name

  fun formatPokemonTrait(name: String): String = name
}