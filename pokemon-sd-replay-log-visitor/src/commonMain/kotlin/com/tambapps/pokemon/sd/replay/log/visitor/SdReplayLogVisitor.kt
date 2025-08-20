package com.tambapps.pokemon.sd.replay.log.visitor

interface SdReplayLogVisitor {

  companion object {
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
    
    private val RATING_LOG_REGEX = Regex("(.*?)'s rating: (\\d+) .*?&rarr;.*?<strong>(\\d+)</strong>")
    private val NEXT_BATTLE_REGEX = Regex("href=\"([^\"]+)\"")
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
      LOG_JOIN -> visitJoinLog(tokens[2])
      LOG_HTML -> visitHtmlLog(tokens.drop(2).joinToString("|"))
      LOG_UHTML -> {
        val name = tokens[2]
        val content = tokens.drop(3).joinToString("|")
        val match = NEXT_BATTLE_REGEX.find(content)
        if (match != null) {
          var nextBattle = match.groupValues[1]
          if (nextBattle.contains("-gen")) {
            nextBattle = nextBattle.substring(nextBattle.indexOf("-gen") + 1)
          }
          visitNextBattleUhtmlLog(name, content, nextBattle)
        } else {
          visitUhtmlLog(name, content)
        }
      }
      LOG_TIME -> visitTimeLog(tokens[2])
      LOG_GAMETYPE -> visitGameTypeLog(tokens[2])
      LOG_PLAYER -> {
        val playerSlot = tokens[2]
        val playerName = tokens[3]
        val avatar = tokens.getOrNull(4)
        val rating = tokens.getOrNull(5)
        visitPlayerLog(playerSlot, playerName, avatar, rating)
      }
      LOG_TEAMSIZE -> {
        val playerSlot = tokens[2]
        val teamSize = tokens[3].toInt()
        visitTeamSizeLog(playerSlot, teamSize)
      }
      LOG_GEN -> visitGenLog(tokens[2])
      LOG_TIER -> visitTierLog(tokens[2])
      LOG_RATED -> visitRatedLog()
      LOG_RULE -> visitRuleLog(tokens[2])
      LOG_CLEARPOKE -> visitClearPokeLog()
      LOG_POKE -> {
        val playerSlot = tokens[2]
        val pokemonInfo = tokens[3]
        val pokemonDetails = pokemonInfo.split(", ")
        val pokemonName = formatPokemonName(pokemonDetails[0])
        val level = pokemonDetails.getOrNull(1)?.substring(1)?.toIntOrNull()
        val gender = pokemonDetails.getOrNull(2)
        visitPokeLog(playerSlot, pokemonName, level, gender)
      }
      LOG_TEAMPREVIEW -> visitTeamPreviewLog(tokens[2].toInt())
      LOG_SHOW_TEAM -> visitShowTeamLog(tokens[2], tokens.drop(3).joinToString("|"))
      LOG_INACTIVE -> visitInactiveLog(tokens.drop(2).joinToString("|"))
      LOG_START -> visitStartLog()
      LOG_SWITCH -> {
        val pokemonSlot = tokens[2].split(':')[0]
        val pokemonName = formatPokemonName(tokens[3].split(',')[0])
        val hpInfo = tokens.getOrNull(4)
        val hpValue: Int? = hpInfo?.let { parseHpPercentage(it) }
        visitSwitchLog(pokemonSlot, pokemonName, hpValue)
      }
      LOG_DRAG -> {
        val pokemonSlot = tokens[2].split(':')[0]
        val pokemonName = formatPokemonName(tokens[3].split(',')[0])
        val hpInfo = tokens.getOrNull(4)
        val hpPercentage: Int? = hpInfo?.let { parseHpPercentage(it) }
        visitDragLog(pokemonSlot, pokemonName, hpPercentage)
      }
      LOG_FIELDSTART -> {
        val field = tokens[2]
        val source = tokens.getOrNull(3)
        val from = tokens.getOrNull(4)
        visitFieldStartLog(field, source, from)
      }
      LOG_ENDITEM -> {
        val pokemonSlot = tokens[2].split(':')[0]
        val item = tokens[3]
        visitEndItemLog(pokemonSlot, item)
      }
      LOG_ACTIVATE -> {
        val pokemonSlot = tokens[2].split(':')[0]
        val ability = tokens[3]
        val details = tokens.drop(4).joinToString("|")
        visitActivateLog(pokemonSlot, ability, details)
      }
      LOG_START_STATUS -> {
        val pokemonSlot = tokens[2].split(':')[0]
        val status = tokens[3]
        visitStartStatusLog(pokemonSlot, status)
      }
      LOG_TURN -> visitTurnLog(tokens[2].toInt())
      LOG_TERASTALLIZE -> {
        val pokemonSlot = tokens[2].split(':')[0]
        val teraType = tokens[3]
        visitTerastallizeLog(pokemonSlot, teraType)
      }
      LOG_MOVE -> {
        val sourceFields = tokens[2].split(':')
        val sourcePokemonSlot = sourceFields.first()
        val sourcePokemonName = formatPokemonName(sourceFields.last().trim())
        val moveName = formatPokemonTrait(tokens[3].trim())

        val targetInfo = tokens.getOrNull(4)
        val targetPokemonSlot: String?
        val targetPokemonName: String?
        
        if (targetInfo.isNullOrBlank()) {
          targetPokemonSlot = null
          targetPokemonName = null
        } else {
          val targetFields = targetInfo.split(':')
          targetPokemonSlot = targetFields.firstOrNull()
          targetPokemonName = if (targetFields.size > 1) formatPokemonName(targetFields.last().trim()) else null
        }

        val additionalInfo = tokens.drop(5).joinToString("|")
        val isSpread = additionalInfo.contains("[spread]")
        val isStill = additionalInfo.contains("[still]")
        visitMoveLog(sourcePokemonSlot, sourcePokemonName, moveName, targetPokemonSlot, targetPokemonName, isSpread, isStill, additionalInfo)
      }
      LOG_DAMAGE -> {
        val pokemonSlot = tokens[2].split(':')[0]
        val hpStatus = tokens[3]
        val source = if (tokens.size > 4) tokens.drop(4).joinToString("|") else null
        visitDamageLog(pokemonSlot, hpStatus, source)
      }
      LOG_SUPEREFFECTIVE -> {
        val pokemonSlot = tokens[2].split(':')[0]
        visitSuperEffectiveLog(pokemonSlot)
      }
      LOG_FAINT -> {
        val pokemonSlot = tokens[2].split(':')[0]
        visitFaintLog(pokemonSlot)
      }
      LOG_HEAL -> {
        val pokemonSlot = tokens[2].split(':')[0]
        val hpStatus = tokens[3]
        val source = if (tokens.size > 4) tokens.drop(4).joinToString("|") else null
        visitHealLog(pokemonSlot, hpStatus, source)
      }
      LOG_UPKEEP -> visitUpkeepLog()
      LOG_ABILITY -> {
        val pokemonSlot = tokens[2].split(':')[0]
        val ability = tokens[3]
        visitAbilityLog(pokemonSlot, ability)
      }
      LOG_SIDESTART -> {
        val side = tokens[2]
        val condition = tokens[3]
        visitSideStartLog(side, condition)
      }
      LOG_FAIL -> {
        val pokemonSlot = tokens[2].split(':')[0]
        visitFailLog(pokemonSlot)
      }
      LOG_CRIT -> {
        val pokemonSlot = tokens[2].split(':')[0]
        visitCritLog(pokemonSlot)
      }
      LOG_BOOST -> {
        val pokemonSlot = tokens[2].split(':')[0]
        val stat = tokens[3]
        val amount = tokens[4].toInt()
        visitBoostLog(pokemonSlot, stat, amount)
      }
      LOG_SINGLETURN -> {
        val pokemonSlot = tokens[2].split(':')[0]
        val move = tokens[3]
        visitSingleTurnLog(pokemonSlot, move)
      }
      LOG_UNBOOST -> {
        val pokemonSlot = tokens[2].split(':')[0]
        val stat = tokens[3]
        val amount = tokens[4].toInt()
        visitUnboostLog(pokemonSlot, stat, amount)
      }
      LOG_END -> {
        val pokemonSlot = tokens[2].split(':')[0]
        val condition = tokens[3]
        val details = tokens.drop(4).joinToString("|")
        visitEndLog(pokemonSlot, condition, details)
      }
      LOG_RESISTED -> {
        val pokemonSlot = tokens[2].split(':')[0]
        visitResistedLog(pokemonSlot)
      }
      LOG_SIDEEND -> {
        val side = tokens[2]
        val condition = tokens[3]
        visitSideEndLog(side, condition)
      }
      LOG_FIELDEND -> {
        val field = tokens[2]
        visitFieldEndLog(field)
      }
      LOG_MESSAGE -> visitMessageLog(tokens.drop(2).joinToString("|"))
      LOG_WIN -> visitWinLog(tokens[2])
      LOG_RAW -> {
        val content = tokens.drop(2).joinToString("|")
        val match = RATING_LOG_REGEX.find(content)
        if (match != null) {
          val playerName = match.groupValues[1]
          val beforeElo = match.groupValues[2].toInt()
          val afterElo = match.groupValues[3].toInt()
          visitRatingUpdateRawLog(content, playerName, beforeElo, afterElo)
        } else {
          visitRawLog(content)
        }
      }
      LOG_LEAVE -> visitLeaveLog(tokens[2])
    }
  }

  fun visitJoinLog(playerName: String) {}
  fun visitHtmlLog(content: String) {}
  fun visitUhtmlLog(name: String, content: String) {}
  fun visitNextBattleUhtmlLog(name: String, content: String, nextBattle: String) {}
  fun visitTimeLog(timestamp: String) {}
  fun visitGameTypeLog(gameType: String) {}
  fun visitPlayerLog(playerSlot: String, playerName: String, avatar: String?, rating: String?) {}
  fun visitTeamSizeLog(playerSlot: String, teamSize: Int) {}
  fun visitGenLog(generation: String) {}
  fun visitTierLog(tier: String) {}
  fun visitRatedLog() {}
  fun visitRuleLog(rule: String) {}
  fun visitClearPokeLog() {}
  fun visitPokeLog(playerSlot: String, pokemonName: String, level: Int?, gender: String?) {}
  fun visitTeamPreviewLog(teamSize: Int) {}
  fun visitShowTeamLog(playerSlot: String, teamData: String) {}
  fun visitInactiveLog(message: String) {}
  fun visitStartLog() {}
  fun visitSwitchLog(pokemonSlot: String, pokemonName: String, hpPercentage: Int?) {}
  fun visitDragLog(pokemonSlot: String, pokemonName: String, hpPercentage: Int?) {}
  fun visitFieldStartLog(field: String, source: String?, from: String?) {}
  fun visitEndItemLog(pokemonSlot: String, item: String) {}
  fun visitActivateLog(pokemonSlot: String, ability: String, details: String) {}
  fun visitStartStatusLog(pokemonSlot: String, status: String) {}
  fun visitTurnLog(turnNumber: Int) {}
  fun visitTerastallizeLog(pokemonSlot: String, teraType: String) {}
  fun visitMoveLog(sourcePokemonSlot: String, sourcePokemonName: String, moveName: String, targetPokemonSlot: String?, targetPokemonName: String?, isSpread: Boolean, isStill: Boolean, additionalInfo: String) {}
  fun visitDamageLog(pokemonSlot: String, hpStatus: String, source: String?) {}
  fun visitSuperEffectiveLog(pokemonSlot: String) {}
  fun visitFaintLog(pokemonSlot: String) {}
  fun visitHealLog(pokemonSlot: String, hpStatus: String, source: String?) {}
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
  fun visitRatingUpdateRawLog(content: String, playerName: String, beforeElo: Int, afterElo: Int) {}
  fun visitLeaveLog(playerName: String) {}

  fun formatPokemonName(name: String): String = name

  fun formatPokemonTrait(name: String): String = name
  
  private fun parseHpPercentage(hpInfo: String): Int? {
    if (hpInfo.contains("/")) {
      val parts = hpInfo.split("/")
      if (parts.size == 2) {
        return parts[0].toIntOrNull()
      }
    }
    return null
  }
}