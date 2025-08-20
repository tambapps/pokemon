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
    private const val LOG_JOIN = "j"
    private const val LOG_HTML = "html"
    private const val LOG_TIME = "t"
    private const val LOG_GAMETYPE = "gametype"
    private const val LOG_PLAYER = "player"
    private const val LOG_TEAMSIZE = "teamsize"
    private const val LOG_GEN = "gen"
    private const val LOG_TIER = "tier"
    private const val LOG_RATED = "rated"
    private const val LOG_RULE = "rule"
    private const val LOG_CLEARPOKE = "clearpoke"
    private const val LOG_TEAMPREVIEW = "teampreview"
    private const val LOG_INACTIVE = "inactive"
    private const val LOG_START = "start"
    private const val LOG_FIELDSTART = "-fieldstart"
    private const val LOG_ENDITEM = "-enditem"
    private const val LOG_ACTIVATE = "-activate"
    private const val LOG_START_STATUS = "-start"
    private const val LOG_TURN = "turn"
    private const val LOG_DAMAGE = "-damage"
    private const val LOG_SUPEREFFECTIVE = "-supereffective"
    private const val LOG_FAINT = "faint"
    private const val LOG_HEAL = "-heal"
    private const val LOG_UPKEEP = "upkeep"
    private const val LOG_ABILITY = "-ability"
    private const val LOG_SIDESTART = "-sidestart"
    private const val LOG_FAIL = "-fail"
    private const val LOG_CRIT = "-crit"
    private const val LOG_BOOST = "-boost"
    private const val LOG_SINGLETURN = "-singleturn"
    private const val LOG_UNBOOST = "-unboost"
    private const val LOG_END = "-end"
    private const val LOG_RESISTED = "-resisted"
    private const val LOG_ENDITEM_STATUS = "-enditem"
    private const val LOG_SIDEEND = "-sideend"
    private const val LOG_FIELDEND = "-fieldend"
    private const val LOG_MESSAGE = "-message"
    private const val LOG_LEAVE = "l"
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
      LOG_JOIN -> visitJoinLog(tokens.getOrNull(2) ?: "")
      LOG_HTML -> visitHtmlLog(tokens.drop(2).joinToString("|"))
      LOG_UHTML -> visitUhtmlLog(tokens.getOrNull(2) ?: "", tokens.drop(3).joinToString("|"))
      LOG_TIME -> visitTimeLog(tokens.getOrNull(2) ?: "")
      LOG_GAMETYPE -> visitGameTypeLog(tokens.getOrNull(2) ?: "")
      LOG_PLAYER -> {
        val playerSlot = tokens.getOrNull(2) ?: ""
        val playerName = tokens.getOrNull(3) ?: ""
        val avatar = tokens.getOrNull(4) ?: ""
        val rating = tokens.getOrNull(5) ?: ""
        visitPlayerLog(playerSlot, playerName, avatar, rating)
      }
      LOG_TEAMSIZE -> {
        val playerSlot = tokens.getOrNull(2) ?: ""
        val teamSize = tokens.getOrNull(3)?.toIntOrNull() ?: 0
        visitTeamSizeLog(playerSlot, teamSize)
      }
      LOG_GEN -> visitGenLog(tokens.getOrNull(2) ?: "")
      LOG_TIER -> visitTierLog(tokens.getOrNull(2) ?: "")
      LOG_RATED -> visitRatedLog()
      LOG_RULE -> visitRuleLog(tokens.getOrNull(2) ?: "")
      LOG_CLEARPOKE -> visitClearPokeLog()
      LOG_POKE -> {
        val playerSlot = tokens.getOrNull(2) ?: ""
        val pokemonInfo = tokens.getOrNull(3) ?: ""
        visitPokeLog(playerSlot, pokemonInfo)
      }
      LOG_TEAMPREVIEW -> visitTeamPreviewLog(tokens.getOrNull(2)?.toIntOrNull() ?: 0)
      LOG_SHOW_TEAM -> visitShowTeamLog(tokens.getOrNull(2) ?: "", tokens.drop(3).joinToString("|"))
      LOG_INACTIVE -> visitInactiveLog(tokens.drop(2).joinToString("|"))
      LOG_START -> visitStartLog()
      LOG_SWITCH -> {
        val pokemonSlot = tokens.getOrNull(2) ?: ""
        val pokemonInfo = tokens.getOrNull(3) ?: ""
        visitSwitchLog(pokemonSlot, pokemonInfo)
      }
      LOG_DRAG -> {
        val pokemonSlot = tokens.getOrNull(2) ?: ""
        val pokemonInfo = tokens.getOrNull(3) ?: ""
        visitDragLog(pokemonSlot, pokemonInfo)
      }
      LOG_FIELDSTART -> {
        val field = tokens.getOrNull(2) ?: ""
        val source = tokens.getOrNull(3) ?: ""
        val from = tokens.getOrNull(4) ?: ""
        visitFieldStartLog(field, source, from)
      }
      LOG_ENDITEM -> {
        val pokemonSlot = tokens.getOrNull(2) ?: ""
        val item = tokens.getOrNull(3) ?: ""
        visitEndItemLog(pokemonSlot, item)
      }
      LOG_ACTIVATE -> {
        val pokemonSlot = tokens.getOrNull(2) ?: ""
        val ability = tokens.getOrNull(3) ?: ""
        val details = tokens.drop(4).joinToString("|")
        visitActivateLog(pokemonSlot, ability, details)
      }
      LOG_START_STATUS -> {
        val pokemonSlot = tokens.getOrNull(2) ?: ""
        val status = tokens.getOrNull(3) ?: ""
        visitStartStatusLog(pokemonSlot, status)
      }
      LOG_TURN -> visitTurnLog(tokens.getOrNull(2)?.toIntOrNull() ?: 0)
      LOG_TERASTALLIZE -> {
        val pokemonSlot = tokens.getOrNull(2) ?: ""
        val teraType = tokens.getOrNull(3) ?: ""
        visitTerastallizeLog(pokemonSlot, teraType)
      }
      LOG_MOVE -> {
        val sourceFields = tokens[2].split(':')
        val sourcePokemonSlot = sourceFields.first()
        val sourcePokemonName = formatPokemonName(sourceFields.last().trim())
        val moveName = formatPokemonTrait(tokens[3].trim())

        val targetInfo = tokens.getOrNull(4) ?: ""
        val targetFields = targetInfo.split(':')
        val targetPokemonSlot = targetFields.firstOrNull() ?: ""
        val targetPokemonName = if (targetFields.size > 1) formatPokemonName(targetFields.last().trim()) else ""

        val additionalInfo = tokens.drop(5).joinToString("|")
        val isSpread = additionalInfo.contains("[spread]")
        val isStill = additionalInfo.contains("[still]")
        visitMoveLog(sourcePokemonSlot, sourcePokemonName, moveName, targetPokemonSlot, targetPokemonName, isSpread, isStill, additionalInfo)
      }
      LOG_DAMAGE -> {
        val pokemonSlot = tokens.getOrNull(2) ?: ""
        val hpStatus = tokens.getOrNull(3) ?: ""
        val source = tokens.getOrNull(4) ?: ""
        visitDamageLog(pokemonSlot, hpStatus, source)
      }
      LOG_SUPEREFFECTIVE -> {
        val pokemonSlot = tokens.getOrNull(2) ?: ""
        visitSuperEffectiveLog(pokemonSlot)
      }
      LOG_FAINT -> {
        val pokemonSlot = tokens.getOrNull(2) ?: ""
        visitFaintLog(pokemonSlot)
      }
      LOG_HEAL -> {
        val pokemonSlot = tokens.getOrNull(2) ?: ""
        val hpStatus = tokens.getOrNull(3) ?: ""
        val source = tokens.getOrNull(4) ?: ""
        visitHealLog(pokemonSlot, hpStatus, source)
      }
      LOG_UPKEEP -> visitUpkeepLog()
      LOG_ABILITY -> {
        val pokemonSlot = tokens.getOrNull(2) ?: ""
        val ability = tokens.getOrNull(3) ?: ""
        visitAbilityLog(pokemonSlot, ability)
      }
      LOG_SIDESTART -> {
        val side = tokens.getOrNull(2) ?: ""
        val condition = tokens.getOrNull(3) ?: ""
        visitSideStartLog(side, condition)
      }
      LOG_FAIL -> {
        val pokemonSlot = tokens.getOrNull(2) ?: ""
        visitFailLog(pokemonSlot)
      }
      LOG_CRIT -> {
        val pokemonSlot = tokens.getOrNull(2) ?: ""
        visitCritLog(pokemonSlot)
      }
      LOG_BOOST -> {
        val pokemonSlot = tokens.getOrNull(2) ?: ""
        val stat = tokens.getOrNull(3) ?: ""
        val amount = tokens.getOrNull(4)?.toIntOrNull() ?: 0
        visitBoostLog(pokemonSlot, stat, amount)
      }
      LOG_SINGLETURN -> {
        val pokemonSlot = tokens.getOrNull(2) ?: ""
        val move = tokens.getOrNull(3) ?: ""
        visitSingleTurnLog(pokemonSlot, move)
      }
      LOG_UNBOOST -> {
        val pokemonSlot = tokens.getOrNull(2) ?: ""
        val stat = tokens.getOrNull(3) ?: ""
        val amount = tokens.getOrNull(4)?.toIntOrNull() ?: 0
        visitUnboostLog(pokemonSlot, stat, amount)
      }
      LOG_END -> {
        val pokemonSlot = tokens.getOrNull(2) ?: ""
        val condition = tokens.getOrNull(3) ?: ""
        val details = tokens.drop(4).joinToString("|")
        visitEndLog(pokemonSlot, condition, details)
      }
      LOG_RESISTED -> {
        val pokemonSlot = tokens.getOrNull(2) ?: ""
        visitResistedLog(pokemonSlot)
      }
      LOG_SIDEEND -> {
        val side = tokens.getOrNull(2) ?: ""
        val condition = tokens.getOrNull(3) ?: ""
        visitSideEndLog(side, condition)
      }
      LOG_FIELDEND -> {
        val field = tokens.getOrNull(2) ?: ""
        visitFieldEndLog(field)
      }
      LOG_MESSAGE -> visitMessageLog(tokens.drop(2).joinToString("|"))
      LOG_WIN -> visitWinLog(tokens.getOrNull(2) ?: "")
      LOG_RAW -> visitRawLog(tokens.drop(2).joinToString("|"))
      LOG_LEAVE -> visitLeaveLog(tokens.getOrNull(2) ?: "")
    }
  }

  fun visitJoinLog(playerName: String) {}
  fun visitHtmlLog(content: String) {}
  fun visitUhtmlLog(name: String, content: String) {}
  fun visitTimeLog(timestamp: String) {}
  fun visitGameTypeLog(gameType: String) {}
  fun visitPlayerLog(playerSlot: String, playerName: String, avatar: String, rating: String) {}
  fun visitTeamSizeLog(playerSlot: String, teamSize: Int) {}
  fun visitGenLog(generation: String) {}
  fun visitTierLog(tier: String) {}
  fun visitRatedLog() {}
  fun visitRuleLog(rule: String) {}
  fun visitClearPokeLog() {}
  fun visitPokeLog(playerSlot: String, pokemonInfo: String) {}
  fun visitTeamPreviewLog(teamSize: Int) {}
  fun visitShowTeamLog(playerSlot: String, teamData: String) {}
  fun visitInactiveLog(message: String) {}
  fun visitStartLog() {}
  fun visitSwitchLog(pokemonSlot: String, pokemonInfo: String) {}
  fun visitDragLog(pokemonSlot: String, pokemonInfo: String) {}
  fun visitFieldStartLog(field: String, source: String, from: String) {}
  fun visitEndItemLog(pokemonSlot: String, item: String) {}
  fun visitActivateLog(pokemonSlot: String, ability: String, details: String) {}
  fun visitStartStatusLog(pokemonSlot: String, status: String) {}
  fun visitTurnLog(turnNumber: Int) {}
  fun visitTerastallizeLog(pokemonSlot: String, teraType: String) {}
  fun visitMoveLog(sourcePokemonSlot: String, sourcePokemonName: String, moveName: String, targetPokemonSlot: String, targetPokemonName: String, isSpread: Boolean, isStill: Boolean, additionalInfo: String) {}
  fun visitDamageLog(pokemonSlot: String, hpStatus: String, source: String) {}
  fun visitSuperEffectiveLog(pokemonSlot: String) {}
  fun visitFaintLog(pokemonSlot: String) {}
  fun visitHealLog(pokemonSlot: String, hpStatus: String, source: String) {}
  fun visitUpkeepLog() {}
  fun visitAbilityLog(pokemonSlot: String, ability: String) {}
  fun visitSideStartLog(side: String, condition: String) {}
  fun visitFailLog(pokemonSlot: String) {}
  fun visitCritLog(pokemonSlot: String) {}
  fun visitBoostLog(pokemonSlot: String, stat: String, amount: Int) {}
  fun visitSingleTurnLog(pokemonSlot: String, move: String) {}
  fun visitUnboostLog(pokemonSlot: String, stat: String, amount: Int) {}
  fun visitEndLog(pokemonSlot: String, condition: String, details: String) {}
  fun visitResistedLog(pokemonSlot: String) {}
  fun visitSideEndLog(side: String, condition: String) {}
  fun visitFieldEndLog(field: String) {}
  fun visitMessageLog(message: String) {}
  fun visitWinLog(winner: String) {}
  fun visitRawLog(content: String) {}
  fun visitLeaveLog(playerName: String) {}

  fun formatPokemonName(name: String): String = name

  fun formatPokemonTrait(name: String): String = name
}