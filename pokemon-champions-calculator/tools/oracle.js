// Cross-validation oracle: runs the REAL NCP-VGC-Damage-Calculator engine (damage_MASTER.js +
// damage_SV.js) directly, bypassing the jQuery/DOM UI layer, so we can compare its output
// against the Kotlin port for a curated set of scenarios.
//
// Usage: node tools/oracle.js tools/scenarios.json [path-to-NCP-VGC-Damage-Calculator]
// The calculator path defaults to ../../../NCP-VGC-Damage-Calculator relative to this file,
// same convention as generate_data.js.
const fs = require('fs');
const path = require('path');
const vm = require('vm');

const CALCULATOR_ROOT = process.argv[3] || path.join(__dirname, '..', '..', '..', 'NCP-VGC-Damage-Calculator');
const SRC = path.join(CALCULATOR_ROOT, 'script_res');

function deepExtend(t, ...s) {
  for (const src of s) {
    if (!src) continue;
    for (const k of Object.keys(src)) {
      const v = src[k];
      if (Array.isArray(v)) t[k] = deepExtend(Array.isArray(t[k]) ? t[k] : [], v);
      else if (v && typeof v === 'object') t[k] = deepExtend(t[k] && typeof t[k] === 'object' ? t[k] : {}, v);
      else t[k] = v;
    }
  }
  return t;
}
function jqExtend(...a) {
  let deep = false, start = 0;
  if (a[0] === true) { deep = true; start = 1; }
  const target = a[start];
  const sources = a.slice(start + 1);
  return deep ? deepExtend(target, ...sources) : Object.assign(target, ...sources);
}
// Fake jQuery: every selector returns a chainable stub whose checkboxes read as unchecked.
function fakeJQuery() {
  const stub = {
    is: () => false,
    prop: () => false,
    val: () => undefined,
    find: () => stub,
    text: () => '',
  };
  const $ = () => stub;
  $.extend = jqExtend;
  $.isEmptyObject = (o) => !o || Object.keys(o).length === 0;
  return $;
}

const sandbox = {
  console,
  $: fakeJQuery(),
  localStorage: { getItem: () => null },
  gen: 10,
  resultDisplayMode: 'raw',
  mechanicsTests: {},
  isCustomMods: false,
};
vm.createContext(sandbox);
function load(file) {
  vm.runInContext(fs.readFileSync(path.join(SRC, file), 'utf8'), sandbox, { filename: file });
}
load('pokedex.js');
load('stat_data.js');
load('type_data.js');
load('nature_data.js');
load('ability_data.js');
load('item_data.js');
load('move_data.js');
load('damage_MASTER.js');
load('damage_SV.js');

const { POKEDEX_CHAMPIONS, MOVES_CHAMPIONS, NATURES, STATS_GSC, TYPE_CHART_SV } = sandbox;
sandbox.STATS = STATS_GSC;
sandbox.typeChart = TYPE_CHART_SV;
sandbox.moves = MOVES_CHAMPIONS;
sandbox.pokedex = POKEDEX_CHAMPIONS;
const [AT, DF, SA, SD, SP] = STATS_GSC;

function calcRawStat(base, statKey, statPoints, nature) {
  if (statKey === 'hp') {
    if (base === 1) return 1;
    return Math.floor((base * 2 + 31) * 50 / 100) + 50 + 10 + statPoints;
  }
  const natureMult = NATURES[nature][0] === statKey ? 1.1 : NATURES[nature][1] === statKey ? 0.9 : 1.0;
  return Math.floor((Math.floor((base * 2 + 31) * 50 / 100) + 5 + statPoints) * natureMult);
}

/**
 * spec: { name, ability, item, nature, statPoints:{hp,at,df,sa,sd,sp}, boosts:{at,df,sa,sd,sp}, status, curHpFraction }
 */
function buildPokemon(spec) {
  const mon = POKEDEX_CHAMPIONS[spec.name];
  if (!mon) throw new Error('unknown species ' + spec.name);
  const statPoints = spec.statPoints || {};
  const boosts = spec.boosts || {};
  const rawStats = {};
  rawStats[AT] = calcRawStat(mon.bs.at, 'at', statPoints.at || 0, spec.nature);
  rawStats[DF] = calcRawStat(mon.bs.df, 'df', statPoints.df || 0, spec.nature);
  rawStats[SA] = calcRawStat(mon.bs.sa, 'sa', statPoints.sa || 0, spec.nature);
  rawStats[SD] = calcRawStat(mon.bs.sd, 'sd', statPoints.sd || 0, spec.nature);
  rawStats[SP] = calcRawStat(mon.bs.sp, 'sp', statPoints.sp || 0, spec.nature);
  const maxHP = calcRawStat(mon.bs.hp, 'hp', statPoints.hp || 0, spec.nature);

  const p = {
    name: spec.name,
    type1: mon.t1,
    type2: mon.t2 || '',
    tera_type: '',
    level: 50,
    maxHP,
    curHP: spec.curHpFraction !== undefined ? Math.round(maxHP * spec.curHpFraction) : maxHP,
    HPEVs: 0, HPIVs: 31, HPraw: maxHP, HPSPs: statPoints.hp || 0,
    isDynamax: false, gmax_factor: false, isTerastalize: false, isChild: false,
    rawStats,
    boosts: { [AT]: boosts.at || 0, [DF]: boosts.df || 0, [SA]: boosts.sa || 0, [SD]: boosts.sd || 0, [SP]: boosts.sp || 0 },
    stats: {},
    sps: { [AT]: statPoints.at || 0, [DF]: statPoints.df || 0, [SA]: statPoints.sa || 0, [SD]: statPoints.sd || 0, [SP]: statPoints.sp || 0 },
    evs: { [AT]: 0, [DF]: 0, [SA]: 0, [SD]: 0, [SP]: 0 },
    ivs: { [AT]: 31, [DF]: 31, [SA]: 31, [SD]: 31, [SP]: 31 },
    nature: spec.nature,
    ability: spec.ability || '',
    abilityOn: !!spec.abilityOn,
    supremeOverlord: spec.supremeOverlord || 0,
    rivalryGender: spec.rivalryGender || '',
    highestStat: -1,
    item: spec.item || '',
    status: spec.status || 'Healthy',
    toxicCounter: 0,
    moves: [null, null, null, null],
    glaiveRushMod: !!spec.glaiveRushMod,
    weight: mon.w,
    canEvolve: !!mon.canEvolve,
    isTransformed: false,
    hasType: function (t1, t2) {
      if (t2 !== undefined) return this.type1 === t1 || this.type1 === t2 || this.type2 === t1 || this.type2 === t2;
      return this.type1 === t1 || this.type2 === t1;
    },
  };
  for (const s of [AT, DF, SA, SD, SP]) p.stats[s] = sandbox.getModifiedStat(p.rawStats[s], p.boosts[s]);
  return p;
}

function buildMove(name, overrides) {
  const base = MOVES_CHAMPIONS[name];
  if (!base) throw new Error('unknown move ' + name);
  return Object.assign({}, base, {
    name,
    isCrit: false, isZ: false, hits: base.hitRange ? (Array.isArray(base.hitRange) ? base.hitRange[0] : base.hitRange) : 1,
    isDouble: 0, combinePledge: 0, timesAffected: 0, usedOppMoveIndex: 0,
    getsStellarBoost: false, isPlusMove: false,
  }, overrides || {});
}

function buildField(overrides) {
  return Object.assign({
    format: 'Doubles',
    weather: '', terrain: '', isGravity: false, isCharge: false,
    isForesight: false, isProtect: false, isNeutralizingGas: false,
    isFriendGuard: false, isBattery: false, isPowerSpot: false, isSteelySpirit: false,
    isHelpingHand: false, isReflect: false, isLightScreen: false, isAuroraVeil: false,
    isSR: false, isSteelsurge: false, spikes: 0, isIngrain: false,
  }, overrides || {});
}

function calc(attackerSpec, defenderSpec, moveName, moveOverrides, fieldOverrides) {
  const attacker = buildPokemon(attackerSpec);
  const defender = buildPokemon(defenderSpec);
  const move = buildMove(moveName, moveOverrides);
  const field = buildField(fieldOverrides);
  const result = sandbox.GET_DAMAGE_SV(attacker, defender, move, field);
  return result;
}

module.exports = { calc, sandbox };

if (require.main === module) {
  const scenarios = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'));
  const results = scenarios.map((s) => {
    const r = calc(s.attacker, s.defender, s.move, s.moveOverrides, s.field);
    return { id: s.id, damage: r.damage, description: r.description };
  });
  console.log(JSON.stringify(results, null, 2));
}
