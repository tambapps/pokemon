# pokemon-champions-calculator

Kotlin Multiplatform damage-calculation engine for the Pokémon Champions
format. Ported from the calculation logic of
[NCP-VGC-Damage-Calculator](https://github.com/nerd-of-now/NCP-VGC-Damage-Calculator)
(the `gen == 10` / Champions scope specifically — not the other generations
that calculator also supports).

Library-only: no app modules, no UI. Nested under the `pokemon` root the
same way `pokemon-sd:pokemon-sd-replay-parser` is.

## Modules

- **champions-data** — Pokémon/move/type/nature definitions for the
  Champions format, plus the `Ability` and `Item` enums (216 and 166
  entries). Generated from `POKEDEX_CHAMPIONS` / `MOVES_CHAMPIONS` /
  `TYPE_CHART_SV` / `ABILITIES_CHAMPIONS` / `ITEMS_CHAMPIONS` by
  [`tools/generate_data.js`](tools/generate_data.js) — kept separate from
  the engine because this data changes with every Regulation update,
  independently of the (much more stable) damage formula. Depends on
  `pokemon-core` for `PokemonName`, `MoveName`, `PokeType`, `PokeStats`,
  `Nature` and `Gender` rather than redefining them.
- **champions-engine** — the damage formula, stat calculation, and
  KO-chance computation. Ported from `damage_MASTER.js` / `damage_SV.js` /
  `stat_data.js` / `ko_chance.js`. Depends on `champions-data`, never the
  other way around.

## Reusing `pokemon-core`, and why `Ability`/`Item` are still their own enums

Species/move identity (`PokemonName`/`MoveName`), `Nature`, `Gender`,
`PokeType` and stat computation (`PokeStats.compute(..., legacySystem =
false)`, which is exactly Champions' level-50/Stat-Points formula) all come
straight from `pokemon-core` — no reimplementation, no drift.

Abilities and held items are the one place this module keeps its own
representation. `BattlePokemon.ability`/`.item` are the general-purpose
`AbilityName`/`ItemName` types everywhere else in the project uses (so a
value straight from a parsed pokepaste plugs in with no conversion step),
but the damage engine internally resolves them once, lazily
(`BattlePokemon.resolvedAbility`/`.resolvedItem`), into this module's
closed `Ability`/`Item` enums. That resolution is why every one of the
~150 ability/item checks across the modifier-chain code is a compile-time
exhaustive match rather than a string comparison a typo or a since-removed
Regulation entry could silently no-op — see the doc comments on `Ability`
and `Item` for the full rationale.

The two enums differ slightly on how "not recognized" is represented,
because the two concepts aren't symmetric: a Pokémon can genuinely hold no
item, but it always has *some* ability. `Item.from(...)` returns `Item?`,
with `null` meaning "no item held or not one Champions recognizes."
`Ability.from(...)` returns a non-null `Ability`, with an unrecognized name
(including an empty/unset one) resolving to the `Ability.NO_ABILITY`
sentinel instead of `null` — so `BattlePokemon.resolvedAbility` is never
optional, matching the fact that every Pokémon has an ability slot. Either
way the engine's modifier checks behave the same: `NO_ABILITY` never
matches a real ability comparison, exactly like `null` never did.

## Usage

```kotlin
val garchomp = BattlePokemon(
  species = ChampionsDex.species(PokemonName("Garchomp")),
  ability = AbilityName("Rough Skin"),
  nature = Nature.ADAMANT,
  statPoints = PokeStats(hp = 20, attack = 20, defense = 8, specialAttack = 0, specialDefense = 8, speed = 10),
)
val toxapex = BattlePokemon(
  species = ChampionsDex.species(PokemonName("Toxapex")),
  ability = AbilityName("Merciless"),
  nature = Nature.BOLD,
  statPoints = PokeStats(hp = 20, attack = 0, defense = 20, specialAttack = 0, specialDefense = 16, speed = 6),
)

val result = DamageCalculator.calculateSingleHit(
  attacker = garchomp,
  defender = toxapex,
  moveUse = MoveUse(ChampionsDex.move(MoveName("Earthquake"))),
  field = Battlefield(), // Doubles by default, no weather/terrain/screens
)
// result.rolls: the 16 damage values for the games' 85%-100% roll, ascending
```

For KO-chance math, feed a roll list into `KoChanceCalculator`:

```kotlin
val chance = KoChanceCalculator.minimumHitsToKo(result.rolls, targetHp = toxapex.hp)
// KoChanceResult(hits = 2, chance = 1.0) -> "guaranteed 2HKO"
```

For a whole move rather than a single hit, `DamageCalculator.calculateMove` returns every hit of one
use (a multi-hit move's hits, each Triple Axel hit with its own power, Parental Bond's two hits),
defaulting to the source calculator's hit count (`defaultHitCount`: 3 for 2-5 hit moves, 5 with Skill
Link...), and `KoChanceCalculator.minimumUsesToKo` gives the KO chance in uses of that move. Like the
source, the hits after the first account for what the previous ones changed: a resist berry
consumed or Multiscale broken by the first hit, Weak Armor, Stamina, Gooey (and Defiant/Competitive),
Spicy Spray (and a Rawst/Lum Berry), Parental Bond's second hit after its move's stat change.

Every calc first applies what the source's setup pass does before any move: Trace copying the
target's ability, Cloud Nine suppressing the weather, Forecast and Mimicry changing types, terrain
seeds (consumed for their +1), Intimidate (with Contrary, Guard Dog, Clear Body, Mirror Armor,
Simple, Defiant, Competitive, Rattled...), Supersweet Syrup, Infiltrator going through screens, and
Heavy/Light Metal. The abilities the source toggles apply when `BattlePokemon.abilityIsActive` is
true: Intimidate, Trace and Supersweet Syrup, on top of Flash Fire, Plus/Minus, Stakeout,
Electromorphosis and Protean/Libero. Moves the source doesn't run through the damage formula are
handled like it: Seismic Toss, Night Shade, Super Fang, Endeavor, Final Gambit, OHKO moves, Pain
Split, and Counter, Mirror Coat, Metal Burst and Comeuppance, which return `MoveUse.counteredMove`
(the defender's move, calculated against the attacker).

A `DamageResult` also reports which stats the hit rolled off of (`attackStat`, `defenseStat`):
Body Press reports the attacker's Defense, Foul Play the defender's Attack, Psyshock the
defender's Defense.

Both results carry the source calculator's exact calc description, the text before its damage
numbers, e.g. `+1 32+ Atk Life Orb Tough Claws Mega Charizard X Flare Blitz vs. 32 HP  / 0 Def
Incineroar in Sun through Reflect` (the double space after HP is the source's own). It is a port
of the source's `buildDescription`: each modifier records what it should mention (an ability,
an item, the weather, a screen, a changed base power...) only when it actually applies, exactly
where the source does. `MoveDamageResult.description` is the one to show for a whole move: it
adds the number of hits (`(3 hits)`) and Parental Bond to the first hit's `DamageResult.description`.
The stat investments are written the way the source's "Display results with" setting does,
picked with the `statDisplay` parameter of the `DamageCalculator` functions: Champions' stat
points by default like the source (`20+ Atk`), or `StatDisplay.EVS` (`156+ Atk`) or
`StatDisplay.STATS` (the stat values, `187 Atk`).

The facts behind that text are also exposed, typed, as `facts` (a `CalcFacts`) on both results
(package `com.tambapps.pokemon.champions.engine.description`): the attacker's/defender's
ability and item that applied (`Ability?`/`Item?`), the weather and terrain that applied, the
`Screen` hit through, the changed base power and type, the boosts, the stat investments, the
number of hits... e.g. to show them in a richer UI than a sentence. `CalcFacts.format` is the
reference formatter: `description` is exactly `facts.format(statDisplay)`.

For user input, `ChampionsDex.speciesOrNull`/`moveOrNull` return null instead of throwing on a
name Champions doesn't know, and `PokemonSpecies.defaultAbility` is the ability the source
calculator pre-selects for a species (not an exhaustive list of its legal abilities).

Species that switch forms in battle (megas, Aegislash's stances) list them in
`PokemonSpecies.forms`, and `ChampionsDex.formsOf` resolves them; `ChampionsDex.pickableSpecies`
leaves out the entries that only exist as a form (`isAlternateForm`), for a species picker. Each
mega carries the `megaStone` needed to mega evolve into it, derived from the source's
`MEGA_STONE_USER_LOOKUP` and the stone's X/Y/Z suffix.

## Correctness: cross-validated against the source calculator, plus direct unit coverage

Two layers of tests, for two different jobs:

- **Full-pipeline, cross-validated against the real JS.** Every scenario in
  [`tools/scenarios.json`](tools/scenarios.json) is run through the actual
  `damage_MASTER.js`/`damage_SV.js` (via [`tools/oracle.js`](tools/oracle.js),
  bypassing its jQuery/DOM UI) to get ground-truth expected rolls and
  descriptions, which are hand-copied into
  [`DamageCalculatorCrossValidationTest`](champions-engine/src/commonTest/kotlin/com/tambapps/pokemon/champions/engine/DamageCalculatorCrossValidationTest.kt),
  [`SourceParityCrossValidationTest`](champions-engine/src/commonTest/kotlin/com/tambapps/pokemon/champions/engine/SourceParityCrossValidationTest.kt)
  and [`DamageCalculatorTest`](champions-engine/src/commonTest/kotlin/com/tambapps/pokemon/champions/engine/DamageCalculatorTest.kt).
  Every scenario asserts the exact description text, never a hand-written one,
  and every hit of a multi-hit move.
  Covers STAB, type effectiveness, critical hits, weather (sun, rain, sand,
  snow, Mega Sol), terrains, type-boosting items, screens (Reflect, Light
  Screen, Aurora Veil), Life Orb, burn and Guts, boosts on both sides,
  Unaware on both sides, Body Press/Foul Play's stat swaps, base power
  changes (Gyro Ball, Knock Off, Hex, Solar Beam, Stored Power, Last
  Respects, Water Spout, Weather Ball, Terrain Pulse, Acrobatics, Facade),
  Expert Belt, Friend Guard, Helping Hand/Power Spot/Battery/ally Steely
  Spirit/Charge, Rivalry, Supreme Overlord, Sand Force, Hustle, Scrappy,
  Liquid Voice, Pixilate, Libero, defensive abilities (Thick Fat, Dry Skin,
  Fluffy, Solid Rock, Multiscale), resist berries, Gravity, the spread-move
  penalty, multi-hit moves (Bullet Seed, Triple Axel, Dragon Darts), Sturdy
  vs. OHKO moves, ability/item/terrain immunities, status moves, Struggle,
  Parental Bond, Piercing Drill vs. Protect, Glaive Rush, the three stat
  display modes, and that `AbilityName` resolution is case/spacing-insensitive
  with an unrecognized ability degrading to "no ability" rather than erroring.
  Plus everything the source handles beyond the plain formula: its setup pass
  (terrain seeds, Intimidate and the abilities reacting to it, Trace, Cloud
  Nine, Forecast, Mimicry, Supersweet Syrup, Infiltrator, Heavy/Light Metal),
  Mold Breaker, Battle/Shell Armor, Long Reach and physical Shell Side Arm
  contact, Gale Wings priority, Expanding Force, Weather Ball with Mega Sol,
  Aura Wheel, doubled Payback/Lash Out, Normal Gem, Grav Apple, Misty
  Explosion, Fling (and when it fails), Meteor Beam (and Contrary), Gyro/Electro
  Ball with final speeds, Knock Off vs. mega stones and Klutz, Acrobatics with
  Klutz, Iron Ball, the hits of multi-hit moves (resist berry, Multiscale, Weak
  Armor, Stamina, Gooey, Spicy Spray, Parental Bond), the fixed-damage moves
  and Counter-like moves, and Pain Split.
- **Direct unit tests for every internal resolver** (`ImmunityChecker`,
  `TypeEffectivenessCalculator`, `Grounded`, `SpeedCalculator`,
  `StabResolver`, `BasePowerResolver`/`BasePowerMods`, `AttackStatResolver`/
  `AttackStatMods`, `DefenseStatResolver`/`DefenseStatMods`, `FinalMods`,
  `EffectiveMoveType`/`EffectiveMoveCategory`), with hand-verified expected
  values — these exercise ability/item/terrain/weather branches the
  full-pipeline scenarios don't happen to combine, and they're what caught
  the one real bug this port had: Scrappy's Ghost-immunity bypass was keyed
  off the *defender's* ability instead of the *attacker's* (Scrappy is an
  offensive ability), silently never triggering because no cross-validation
  scenario had ever exercised it.

## `tools/`

Two independent scripts. Both read a **local checkout** of
`NCP-VGC-Damage-Calculator` off disk (`fs.readFileSync` into a Node `vm`
sandbox, never the network) and run its *actual* code there rather than
working from a hand-read of the source — so what they produce is exactly
what the real calculator would compute, not a reimplementation of it.

### `generate_data.js` — produces champions-data's content

1. Loads `pokedex.js`, `move_data.js`, `item_data.js`, `ability_data.js`,
   `type_data.js`, `stat_data.js`, `nature_data.js` in a sandbox (a stubbed
   `$`/`localStorage` so the unmodified JS runs), then reads the resulting
   `POKEDEX_CHAMPIONS` / `MOVES_CHAMPIONS` / `ABILITIES_CHAMPIONS` /
   `ITEMS_CHAMPIONS` / `TYPE_CHART_SV` objects — the same ones the
   calculator's own UI uses.
2. Strips each entry down to only what the damage engine reads (a move
   keeps `bp`/`type`/`category`/`makesContact`/... not accuracy or PP),
   drops one known UI-only placeholder move, and normalizes `hitRange` to
   always be `[min, max]`.
3. Writes two kinds of Kotlin source:
   - `generated/SpeciesJson.kt` / `MoveJson.kt` / `TypeChartJson.kt` — the
     stripped data, minified, embedded as `internal const val ...: String
     = """{...}"""`. Inert JSON text at this point, nothing more.
   - `Ability.kt` / `Item.kt` — one enum constant per Champions-legal
     ability/item name, plus their `.from(AbilityName)`/`.from(ItemName)`
     resolvers and doc comments.
4. The JSON blobs become real objects through hand-written (*not*
   generated) code sitting next to them — `SpeciesDex.kt`/`MoveDex.kt`/
   `TypeChartDex.kt` each hold a `@Serializable` DTO and an `internal val
   ALL_... by lazy { Json.decodeFromString(...).mapKeys { ... normalize
   ... } }` — decoded once, on first access, keyed by normalized name.

```bash
node tools/generate_data.js [path-to-NCP-VGC-Damage-Calculator]
```

Defaults to `../../../NCP-VGC-Damage-Calculator` (a sibling of the
`pokemon` checkout) if no path is given.

### `oracle.js` + `scenarios.json` — the correctness check, not part of the build

`oracle.js` also sandboxes the real JS, but calls `CALCULATE_ALL_MOVES_SV`
directly — the function the UI calls, setup pass then `GET_DAMAGE_SV`,
bypassing the jQuery UI — for each matchup listed in `scenarios.json`, and
prints each scenario's damage rolls and description (an array of rolls per
distinct hit for a multi-hit move whose hits differ). Its output is what's
hand-copied into the cross-validation tests as expected values: the ground
truth this port is checked against. Descriptions use the calculator UI's
default stat display (stat points); a scenario's `displayMode` (`"EVs"` or
`"raw"`) picks another one. A scenario's field can also set
`attackerTailwind`/`defenderTailwind`, and `counteredMove` is the defender's
move a Counter-like move returns. It isn't wired into the Gradle build; run it
by hand when adding scenarios or re-verifying after an upstream mechanic
changes.

```bash
node tools/oracle.js tools/scenarios.json [path-to-NCP-VGC-Damage-Calculator]
```

When requiring it from another script, give the calculator's path with the
`NCP_CALCULATOR_ROOT` environment variable instead.

### Regenerating for a new Regulation

1. Update the source checkout (`git pull` inside
   `NCP-VGC-Damage-Calculator`, or clone it as a sibling of `pokemon` if
   you don't have it).
2. `node tools/generate_data.js` — rewrites the generated data and
   `Ability.kt`/`Item.kt`.
3. `./gradlew build` from the `pokemon` root. If the update removed or
   renamed a Pokémon/move/ability/item `champions-engine` references by
   name, this is where it breaks — a compile error naming the exact spot,
   not a runtime surprise. That's the whole reason abilities and items are
   enums here rather than raw strings.
4. **Regeneration only pulls data, not mechanics.** A Regulation that adds
   a genuinely new ability/item/move *interaction* (not just a returning
   Pokémon) needs a human to read the diff of `damage_MASTER.js` /
   `damage_SV.js` / `item_data.js`'s lookup tables and hand-port the new
   behavior into the matching `champions-engine` file — the same way the
   existing ~150 cases were ported originally. A brand-new ability/item
   with no special-case code just falls through every check as "no
   effect," which is correct until someone adds the real behavior.
5. Re-run `oracle.js` (extending `scenarios.json` for anything new),
   update `DamageCalculatorCrossValidationTest` accordingly, and re-run
   `./gradlew build` to confirm everything still matches hit-for-hit.

## Known limitations

This targets the Champions format specifically (no Terastallization, no
Dynamax, no Z-moves exist there, which removes a lot of complexity the
source calculator carries for other generations). Within that scope, the
engine handles every damage case the source does; what's left:

- **The source's UI conveniences are the caller's.** The source's UI turns
  some ability toggles on by default (Intimidate, Protean, Libero,
  Supersweet Syrup) and sets the weather/terrain from abilities like Drought
  or Electric Surge. The engine takes `abilityIsActive` and the
  `Battlefield` as given: an app mirroring the source should apply the same
  defaults.
- **A few edge cases differ, all in combinations no real calc relies on:**
  a Klutz Pokémon failing to Fling is described without the source's
  "Klutz" item text; the hits after a Weak Armor/Gooey speed change use the
  final speeds (the source recomputes them without Choice Scarf, Tailwind...),
  which only matters for Analytic; a Counter-like move returning a
  multi-hit move uses its default hit count, where the source uses the one
  selected in its UI.
- **Beat Up isn't accurate, like in the source.** It's exposed as a
  multi-hit move, but real Beat Up rolls each hit off a *different*
  uninflicted ally's Attack stat; this port has no team-roster concept, so
  it uses the attacker's own stat for every hit instead, as the source does.
- **KO-chance math is damage-only.** `KoChanceCalculator` convolves the
  16-roll distribution across repeated hits, but doesn't account for
  between-turn effects the source calculator's `ko_chance.js` does:
  residual damage (weather, burn/poison, Leftovers, Leech Seed), hazards
  beyond an initial HP offset you supply yourself, or HP restored by a
  berry mid-KO-check.
- Accuracy, PP, and non-damage secondary effects (status chance, stat-drop
  chance, flinch...) are out of scope entirely — this is a damage
  calculator, not a battle simulator.
- Species base-stats dex is scoped to the ~346 Pokémon/forms legal in
  Champions specifically; it is not a general-purpose Pokédex (see
  `pokeapi-client` for that, though note it doesn't carry move flags or a
  notion of format legality either).
