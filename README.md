# Pokemon

A Kotlin Multiplatform library collection for parsing and working with Pokémon competitive battle data.

## Overview

This project provides a comprehensive set of tools for parsing various Pokémon data formats used in competitive battling, including team compositions, battle replays, and statistical information. All modules are built using Kotlin Multiplatform, making them compatible with JVM, JavaScript, iOS, and Android platforms.

## Modules

### pokemon-core
The core module containing shared data structures and models used across all parsers:

- **Pokemon data class**: Represents a complete Pokémon with all battle-relevant information (name, nature, stats, moves, etc.)
- **Nature enum**: All 25 Pokémon natures with their stat modifiers (e.g., `ADAMANT` boosts Attack, lowers Special Attack)
- **PokeType enum**: All Pokémon types including Tera types
- **PokeStats**: IV/EV stat management with builder pattern support
- **Gender enum**: Pokémon gender representation

### pokepaste-parser
A parser for [PokePaste](https://pokepast.es/) format - the standard text format used for sharing Pokémon teams:

- Parses team data from PokePaste text format
- Supports all competitive features: abilities, items, moves, EVs/IVs, natures, Tera types
- Handles team metadata like nicknames, levels, happiness, shiny status
- Customizable formatting functions for Pokémon names and traits
- Compatible with Showdown team export format

### pokemon-sd-replay-parser
A parser for Pokémon Showdown battle replay data:

- Parses JSON replay files from Pokémon Showdown battles
- Extracts detailed battle information: player teams, move usage statistics, Terastallization data
- Supports rating changes, battle outcomes, and player selection data
- Handles Open Team Sheet (OTS) information when available
- Tracks move usage statistics for competitive analysis
- Customizable name formatting for consistent data representation

