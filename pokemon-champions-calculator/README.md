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
and `Item` for the full rationale. An unrecognized name resolves to `null`,
which the engine already treats as "no special ability"/"no held item
effect" — by design, not by accident.

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

## Correctness: cross-validated against the source calculator

Every mechanic here is checked against **actual output from the real
`damage_MASTER.js`/`damage_SV.js`**, run directly in Node (bypassing its
jQuery/DOM UI) — not just reasoned about by reading the source. See
[`tools/oracle.js`](tools/oracle.js) and
[`DamageCalculatorCrossValidationTest`](champions-engine/src/commonTest/kotlin/com/tambapps/pokemon/champions/engine/DamageCalculatorCrossValidationTest.kt),
which asserts this port's output matches the JS engine hit-for-hit across
STAB, type effectiveness, critical hits, weather, type-boosting items,
screens, Life Orb, burn, Body Press/Foul Play's stat swaps, Gyro Ball,
Expert Belt, Friend Guard, Rivalry, resist berries, Multiscale, the
spread-move penalty, Acrobatics, Facade, multi-hit base power, Sturdy vs.
OHKO moves, ability-granted type immunity, and (new to this module) that
`AbilityName` resolution is case/spacing-insensitive and that an
unrecognized ability degrades to "no ability" rather than erroring.

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

`oracle.js` also sandboxes the real JS, but calls `GET_DAMAGE_SV(attacker,
defender, move, field)` directly — the actual damage function, bypassing
the jQuery UI — for each matchup listed in `scenarios.json`. Its output is
what's hand-copied into `DamageCalculatorCrossValidationTest` as expected
values: the ground truth this port is checked against. It isn't wired into
the Gradle build; run it by hand when adding scenarios or re-verifying
after an upstream mechanic changes.

```bash
node tools/oracle.js tools/scenarios.json [path-to-NCP-VGC-Damage-Calculator]
```

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
source calculator carries for other generations). Within that scope, a few
mechanics are deliberately not implemented, the same way the source
calculator itself documents features it hasn't built:

- **Battle-state pre-processing isn't modeled.** `BattlePokemon.ability`,
  `.boosts`, and `.status` are taken as already-resolved facts about the
  moment being calculated for. Things like Intimidate lowering an
  opponent's Attack on switch-in, or Trace copying an ability, aren't
  simulated automatically — if you want to calculate around them, set the
  resulting boost/ability yourself.
- **Multi-hit moves assume constant state across hits.** Bullet Seed,
  Icicle Spear, etc. are supported (call `calculateSingleHit` once per
  hit), but abilities/items that change state *between* hits of the same
  multi-hit move (Weak Armor, Stamina, Gooey, Kee/Maranga Berry, a resist
  berry or Multiscale being consumed/broken after the first hit) aren't
  modeled — every hit uses the attacker/defender's starting state.
- **Counter-move mechanics aren't implemented**: Counter, Mirror Coat,
  Metal Burst, Comeuppance need the defender's incoming move as extra
  context this API doesn't thread through.
- **Beat Up isn't accurate.** It's exposed as a multi-hit move, but real
  Beat Up rolls each hit off a *different* uninflicted ally's Attack stat;
  this port has no team-roster concept, so it uses the attacker's own stat
  for every hit instead.
- **KO-chance math is damage-only.** `KoChanceCalculator` convolves the
  16-roll distribution across repeated hits, but doesn't account for
  between-turn effects the source calculator's `ko_chance.js` does:
  residual damage (weather, burn/poison, Leftovers, Leech Seed), hazards
  beyond an initial HP offset you supply yourself, or HP restored by a
  berry mid-KO-check.
- **Fling's and Natural Gift's power tables aren't ported** (Natural Gift
  isn't in Champions' movepool at all; Fling's move-power lookup by held
  item is a large table this port skips).
- Accuracy, PP, and non-damage secondary effects (status chance, stat-drop
  chance, flinch...) are out of scope entirely — this is a damage
  calculator, not a battle simulator.
- Species base-stats dex is scoped to the ~346 Pokémon/forms legal in
  Champions specifically; it is not a general-purpose Pokédex (see
  `pokeapi-client` for that, though note it doesn't carry move flags or a
  notion of format legality either).
