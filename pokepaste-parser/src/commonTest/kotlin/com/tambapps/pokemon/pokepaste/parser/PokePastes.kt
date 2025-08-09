package com.tambapps.pokemon.pokepaste.parser

object PokePastes {
  const val BASIC = """
    Miraidon @ Choice Specs  
Ability: Hadron Engine  
Level: 50  
Tera Type: Fairy  
EVs: 252 SpA / 4 SpD / 252 Spe  
Modest Nature  
- Volt Switch  
- Draco Meteor  
- Electro Drift  
- Dazzling Gleam  

Entei @ Choice Band  
Ability: Inner Focus  
Level: 50  
Tera Type: Normal  
EVs: 140 HP / 252 Atk / 116 SpD  
Adamant Nature  
- Sacred Fire  
- Crunch  
- Stomping Tantrum  
- Extreme Speed  

Chien-Pao @ Focus Sash  
Ability: Sword of Ruin  
Level: 50  
Tera Type: Stellar  
EVs: 252 Atk / 252 Spe  
Jolly Nature  
- Icicle Crash  
- Sucker Punch  
- Sacred Sword  
- Protect  

Iron Hands @ Assault Vest  
Ability: Quark Drive  
Level: 50  
Tera Type: Water  
EVs: 252 Atk / 20 Def / 236 SpD  
Brave Nature  
IVs: 0 Spe  
- Drain Punch  
- Low Kick  
- Heavy Slam  
- Fake Out  

Whimsicott @ Covert Cloak  
Ability: Prankster  
Level: 50  
Tera Type: Dark  
EVs: 252 SpA / 252 Spe  
Modest Nature  
IVs: 0 Atk  
- Moonblast  
- Encore  
- Light Screen  
- Tailwind  

Ogerpon-Cornerstone (F) @ Cornerstone Mask  
Ability: Sturdy  
Level: 50  
Tera Type: Rock  
EVs: 252 Atk / 252 Spe  
Adamant Nature  
- Ivy Cudgel  
- Horn Leech  
- Spiky Shield  
- Follow Me  
  """

  const val WITH_SURNAMES = """
Dondochakka (Miraidon) @ Choice Specs  
Ability: Hadron Engine  
Level: 50  
Tera Type: Electric  
EVs: 252 SpA / 252 Spe  
Modest Nature  
- Electro Drift  
- Draco Meteor  
- Volt Switch  
- Discharge  

Sophie (Farigiraf) (F) @ Electric Seed  
Ability: Armor Tail  
Level: 54  
Tera Type: Ground  
EVs: 180 HP / 236 Def / 92 SpD  
Bold Nature  
IVs: 0 Atk  
- Dazzling Gleam  
- Foul Play  
- Trick Room  
- Helping Hand  

Genius (Landorus) @ Life Orb  
Ability: Sheer Force  
Level: 70  
Tera Type: Steel  
EVs: 252 SpA / 100 SpD / 156 Spe  
Timid Nature  
IVs: 0 Atk  
- Earth Power  
- Sludge Bomb  
- Taunt  
- Protect  

Whimsicott (M) @ Focus Sash  
Ability: Prankster  
Level: 50  
Tera Type: Ghost  
EVs: 4 HP / 44 Def / 204 SpA / 4 SpD / 252 Spe  
Timid Nature  
IVs: 0 Atk  
- Moonblast  
- Tailwind  
- Encore  
- Protect  

Rockpon (Ogerpon-Cornerstone) @ Cornerstone Mask  
Ability: Sturdy  
Tera Type: Rock  
EVs: 4 HP / 252 Atk / 252 Spe  
Adamant Nature  
- Ivy Cudgel  
- Horn Leech  
- Follow Me  
- Spiky Shield  

Cat (Incineroar)  
Ability: Intimidate  
Level: 50  
Tera Type: Grass  
EVs: 252 HP / 196 Def / 60 SpD  
Impish Nature  
IVs: 29 Spe  
- Flare Blitz  
- Knock Off  
- Fake Out  
- U-turn  

  """

  const val INCOMPLETE_WITHOUT_MOVES = """
Pikachu @ Light Ball  
Ability: Static  
Level: 50  
Tera Type: Electric  
EVs: 252 SpA / 4 SpD / 252 Spe  
Modest Nature  

Charizard @ Choice Specs  
Ability: Solar Power  
Level: 50  
Tera Type: Fire  
EVs: 252 SpA / 252 Spe  
Timid Nature  
IVs: 0 Atk  

  """
}