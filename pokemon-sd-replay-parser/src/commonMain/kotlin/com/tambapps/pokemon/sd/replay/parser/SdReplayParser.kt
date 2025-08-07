package com.tambapps.pokemon.sd.replay.parser

import com.tambapps.pokemon.PokeType
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json


class SdReplayParser(
    val formatPokemonName: (String) -> String = { it },
    val formatPokemonTrait: (String) -> String = { it },
) {

    companion object {
        const val PARSER_VERSION = "0.1.0"
        private const val LOG_MOVE = "move"
        private const val LOG_POKE = "poke"
        private const val LOG_UHTML = "uhtml"
        private const val LOG_RAW = "raw"
        private const val LOG_DRAG = "drag"
        private const val LOG_SWITCH = "switch"
        private const val LOG_TERASTALLIZE = "-terastallize"
        private const val LOG_WIN = "win"
        private const val LOG_SHOW_TEAM = "showteam"

        private val RATING_LOG_REGEX = Regex("(.*?)'s rating: (\\d+) .*?&rarr;.*?<strong>(\\d+)</strong>")
        private val NEXT_BATTLE_REGEX = Regex("href=\"([^\"]+)\"")

    }
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(jsonString: String) = try {
        json.decodeFromString<RawSdReplay>(jsonString)
    } catch (_: SerializationException) {
        throw SdReplayParseException("Invalid JSON format")
    } catch (_: IllegalArgumentException) {
        throw SdReplayParseException("Invalid replay")
    }

    fun parse(replayData: RawSdReplay): SdReplay {
        if (replayData.players.size != 2) {
            throw SdReplayParseException("Invalid number of players. Expected 2, got ${replayData.players.size}")
        }
        val playerBuilders = replayData.players.map { PlayerBuilder(it) }

        var winner: String? = null
        var nextBattle: String? = null
        for (log in replayData.logs.lines()) {
            val tokens = log.split("|")
            if (tokens.size < 2) continue
            val playerBuilder = playerBuilders[
                if (tokens.size > 2 && tokens[2].startsWith("p2")) 1 else 0
            ]
            when (tokens[1]) {
                LOG_MOVE -> {
                    val pokemonName = formatPokemonName(tokens[2].split(':').last().trim())
                    val moveName = formatPokemonTrait(tokens[3].trim())
                    playerBuilder.moveUsage(pokemonName, moveName)
                }
                LOG_POKE -> {
                    val (rawPokemonName, levelStr) = tokens[3].split(',')
                    playerBuilder.teamPreviewPokemon(TeamPreviewPokemon(formatPokemonName(rawPokemonName), levelStr.trim().substring(1).toInt()))
                }
                LOG_UHTML -> {
                    val match = NEXT_BATTLE_REGEX.find(log.substring(5))
                    if (match != null) {
                        nextBattle = match.groupValues[1]
                        if (nextBattle.contains("-gen")) {
                            nextBattle = nextBattle.substring(nextBattle.indexOf("-gen") + 1);
                        }
                    }
                }
                LOG_RAW -> {
                    val match = RATING_LOG_REGEX.find(log.substring(5))
                    if (match != null) {
                        val playerName = match.groupValues[1]
                        val beforeElo = match.groupValues[2].toInt()
                        val afterElo = match.groupValues[3].toInt()
                        val playerData = playerBuilders.first { it.name == playerName }
                        playerData.beforeElo = beforeElo
                        playerData.afterElo = afterElo
                    }
                }
                LOG_DRAG, LOG_SWITCH -> {
                    val pokemon = formatPokemonName(tokens[3].split(',')[0])
                    playerBuilder.selection(pokemon)
                }
                LOG_TERASTALLIZE -> {
                    playerBuilder.terastallization = Terastallization(
                        pokemon = formatPokemonName(tokens[2].split(':').last().trim()),
                        type = PokeType.valueOf(tokens[3].uppercase()) // TODO handle error
                    )
                }
                LOG_WIN -> winner = tokens[2]
                LOG_SHOW_TEAM -> {
                    val teamLog = log.substring(13)
                    val pokemonLogs = teamLog.split("]")
                    playerBuilder.ots = OpenTeamSheet(
                        pokemons = pokemonLogs.map(this::parsePokemonOts)
                    )
                }
            }
        }
        return SdReplay(
            players = playerBuilders.map { it.build() },
            uploadTime = replayData.uploadTime,
            format = replayData.formatId,
            rating = replayData.rating,
            winner = winner,
            nextBattle = nextBattle,
            parserVersion = PARSER_VERSION
        )
    }

    private fun parsePokemonOts(log: String): OtsPokemon {
        val fields = log.split("|")
        val teraType =
            if (fields.size > 11 && fields[11].isNotBlank()) PokeType.valueOf(fields[11].split(",").last().uppercase())
            else null
        return OtsPokemon(
            name = formatPokemonName(fields[0]),
            item = formatPokemonTrait(fields[2]),
            ability = formatPokemonTrait(fields[3]),
            moves = fields[4].split(",").map(formatPokemonTrait),
            level = fields[10].toInt(),
            teraType = teraType
        )
    }
}



class SdReplayParseException(message: String): Exception(message)