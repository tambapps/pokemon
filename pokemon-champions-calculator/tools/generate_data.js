#!/usr/bin/env node
// Regenerates every generated file under champions-data from the real
// NCP-VGC-Damage-Calculator source: the Champions-scoped species/move/type-chart
// data (embedded as JSON string constants) and the Ability/Item enums.
//
// Usage: node tools/generate_data.js [path-to-NCP-VGC-Damage-Calculator]
// Defaults to ../../../NCP-VGC-Damage-Calculator relative to this file, i.e. a
// sibling of the `pokemon` checkout -- pass the path explicitly if it lives
// somewhere else.
//
// Run this again whenever the source calculator ships a new Regulation set, then
// re-run the tests (in particular tools/oracle.js's cross-validation scenarios,
// mirrored in champions-engine's DamageCalculatorCrossValidationTest) to catch any
// mechanic that changed underneath the port.
const fs = require('fs');
const path = require('path');
const vm = require('vm');

const CALCULATOR_ROOT = process.argv[2] || path.join(__dirname, '..', '..', '..', 'NCP-VGC-Damage-Calculator');
const SRC = path.join(CALCULATOR_ROOT, 'script_res');
const DATA_MAIN = path.join(__dirname, '..', 'champions-data', 'src', 'commonMain', 'kotlin', 'com', 'tambapps', 'pokemon', 'champions', 'data');
const GENERATED_DIR = path.join(DATA_MAIN, 'generated');

// Placeholder dropdown entries in the source UI that aren't real, usable moves.
const NON_MOVES = new Set(['(No Move)']);

function deepExtend(target, ...sources) {
  for (const src of sources) {
    if (!src) continue;
    for (const key of Object.keys(src)) {
      const srcVal = src[key];
      if (Array.isArray(srcVal)) target[key] = deepExtend(Array.isArray(target[key]) ? target[key] : [], srcVal);
      else if (srcVal && typeof srcVal === 'object') target[key] = deepExtend(target[key] && typeof target[key] === 'object' ? target[key] : {}, srcVal);
      else target[key] = srcVal;
    }
  }
  return target;
}
function jqExtend(...args) {
  let deep = false, start = 0;
  if (args[0] === true) { deep = true; start = 1; }
  const target = args[start];
  const sources = args.slice(start + 1);
  return deep ? deepExtend(target, ...sources) : Object.assign(target, ...sources);
}

const sandbox = { console, $: { extend: jqExtend }, localStorage: { getItem: () => null } };
vm.createContext(sandbox);
function load(file) {
  vm.runInContext(fs.readFileSync(path.join(SRC, file), 'utf8'), sandbox, { filename: file });
}
for (const f of ['pokedex.js', 'stat_data.js', 'type_data.js', 'nature_data.js', 'ability_data.js', 'item_data.js', 'move_data.js']) {
  load(f);
}

const { POKEDEX_CHAMPIONS, MOVES_CHAMPIONS, ITEMS_CHAMPIONS, ABILITIES_CHAMPIONS, TYPE_CHART_SV } = sandbox;

// --- species: name, types, base stats, weight. Ability legality is deliberately not
// modeled -- "ab" in the source dex is overwritten layer by layer and isn't reliable,
// and the calculator itself never restricts which ability a species can be given. ---
const species = {};
for (const [name, mon] of Object.entries(POKEDEX_CHAMPIONS)) {
  species[name] = {
    types: [mon.t1, mon.t2].filter(Boolean),
    baseStats: { hp: mon.bs.hp, atk: mon.bs.at, def: mon.bs.df, spa: mon.bs.sa, spd: mon.bs.sd, spe: mon.bs.sp },
    weightKg: mon.w,
  };
}

// --- moves: only the fields the damage engine actually reads (accuracy, PP, secondary
// effect chances etc. are out of scope -- this is a damage calculator, not a simulator).
// Struggle is the games' one Typeless move; it's normal-typed in this static data and
// retyped dynamically by the engine (see effectiveTypeOf), so no move here needs a
// Typeless PokeType value to exist. ---
const MOVE_FIELDS = [
  'bp', 'type', 'category', 'makesContact', 'isSound', 'isBullet', 'isPulse',
  'isBite', 'isPunch', 'isSlice', 'isSpread', 'isOHKO', 'hasSecondaryEffect',
  'ignoresBurn', 'ignoresDefenseBoosts', 'ignoresScreens', 'dealsPhysicalDamage',
  'hitRange', 'isTripleHit', 'alwaysCrit', 'canDouble', 'linearAddBP', 'isPriority',
  'recoilHP', 'hasCrash',
];
const moves = {};
for (const [name, mv] of Object.entries(MOVES_CHAMPIONS)) {
  if (NON_MOVES.has(name)) continue;
  const out = {};
  for (const f of MOVE_FIELDS) if (mv[f] !== undefined) out[f] = mv[f];
  // hitRange is either a plain number (a fixed hit count) or a [min, max] pair upstream;
  // normalize to always be [min, max] so the Kotlin side has one shape to parse.
  if (out.hitRange !== undefined && !Array.isArray(out.hitRange)) out.hitRange = [out.hitRange, out.hitRange];
  moves[name] = out;
}

// --- type chart: drop Stellar/???/Typeless (unreachable in Champions -- no
// Terastallization, no gen 1-3 support, and Struggle is handled by the engine directly). ---
const REAL_TYPES = ['Normal', 'Fire', 'Water', 'Electric', 'Grass', 'Ice', 'Fighting', 'Poison', 'Ground', 'Flying', 'Psychic', 'Bug', 'Rock', 'Ghost', 'Dragon', 'Dark', 'Steel', 'Fairy'];
const typeChart = {};
for (const atk of REAL_TYPES) {
  typeChart[atk] = {};
  for (const def of REAL_TYPES) typeChart[atk][def] = TYPE_CHART_SV[atk][def];
}

function writeJsonConst(fileName, constName, value) {
  const json = JSON.stringify(value).replace(/\$/g, "${'$'}");
  const content = `package com.tambapps.pokemon.champions.data.generated

// Generated by pokemon-champions-calculator/tools/generate_data.js from
// NCP-VGC-Damage-Calculator. Do not hand-edit -- regenerate instead.
internal const val ${constName}: String = """${json}"""
`;
  fs.writeFileSync(path.join(GENERATED_DIR, fileName), content);
}
writeJsonConst('SpeciesJson.kt', 'SPECIES_JSON', species);
writeJsonConst('MoveJson.kt', 'MOVE_JSON', moves);
writeJsonConst('TypeChartJson.kt', 'TYPE_CHART_JSON', typeChart);

// --- Ability/Item enums: one Kotlin constant per Champions-legal ability/item name. ---
function toEnumConst(name) {
  let id = name.toUpperCase().replace(/[^A-Z0-9]+/g, '_').replace(/^_+|_+$/g, '');
  if (/^[0-9]/.test(id)) id = 'N' + id;
  return id;
}
function writeEnum(fileName, className, wrapperType, names, doc) {
  const seen = new Map();
  const lines = names.map((name) => {
    const id = toEnumConst(name);
    if (seen.has(id)) throw new Error(`enum constant collision: ${id} from "${name}" and "${seen.get(id)}"`);
    seen.set(id, name);
    return `  ${id}("${name.replace(/"/g, '\\"')}"),`;
  });
  const content = `package com.tambapps.pokemon.champions.data

import com.tambapps.pokemon.${wrapperType}
import com.tambapps.pokemon.PokemonNormalizer

${doc}
enum class ${className}(val displayName: String) {
${lines.join('\n')}
  ;

  override fun toString() = displayName

  companion object {
    private val byNormalizedName: Map<String, ${className}> = entries.associateBy { PokemonNormalizer.normalize(it.displayName) }

    /** Resolves a general-purpose [${wrapperType}] to this closed set, or null if it isn't legal in Champions. */
    fun from(name: ${wrapperType}): ${className}? = byNormalizedName[name.normalized.value]
  }
}
`;
  fs.writeFileSync(path.join(DATA_MAIN, fileName), content);
}

const abilityDoc = `/**
 * Every ability legal in the Champions format, as a closed enum.
 *
 * Everywhere else in this project, an ability is an [com.tambapps.pokemon.AbilityName]: a
 * normalized wrapper around whatever string a pokepaste or replay log happens to contain,
 * because those sources can reference abilities this project doesn't otherwise model. The
 * damage engine needs something stricter: it exhaustively branches on "is this ability X" for
 * around ninety different mechanics, and a typo'd or since-removed ability name should fail to
 * compile when that logic is written, not silently do nothing. [from] bridges the two: given
 * the general-purpose [com.tambapps.pokemon.AbilityName] a caller actually has, resolve it to
 * this closed set once, up front. An unrecognized name resolves to null, which the engine
 * already treats as "no special ability" -- by design, not by accident.
 */`;
const itemDoc = `/**
 * Every held item legal in the Champions format, as a closed enum.
 *
 * Everywhere else in this project, a held item is an [com.tambapps.pokemon.ItemName]: a
 * normalized wrapper around whatever string a pokepaste or replay log happens to contain,
 * because those sources can reference items this project doesn't otherwise model. The damage
 * engine needs something stricter: it exhaustively branches on "is this item X" for a few dozen
 * mechanics, and a typo'd or since-removed item name should fail to compile when that logic is
 * written, not silently do nothing. [from] bridges the two: given the general-purpose
 * [com.tambapps.pokemon.ItemName] a caller actually has, resolve it to this closed set once, up
 * front. An unrecognized name resolves to null, which the engine already treats as "no held
 * item effect" -- by design, not by accident.
 */`;
writeEnum('Ability.kt', 'Ability', 'AbilityName', [...ABILITIES_CHAMPIONS].sort(), abilityDoc);
writeEnum('Item.kt', 'Item', 'ItemName', [...ITEMS_CHAMPIONS].sort(), itemDoc);

console.log('species:', Object.keys(species).length);
console.log('moves:', Object.keys(moves).length);
console.log('abilities:', ABILITIES_CHAMPIONS.length);
console.log('items:', ITEMS_CHAMPIONS.length);
console.log('type chart attacking types:', Object.keys(typeChart).length);
console.log('\nRegenerated. Re-run ./gradlew build and check for new compile errors --');
console.log('an unresolved Ability/Item reference means a mechanic the engine implements');
console.log("relies on a name that changed or is no longer in Champions' legal list.");
