package com.tambapps.pokemon.sd.replay.log.visitor

import kotlin.test.Test

class SdReplayLogVisitorTest {

  private companion object {
    const val P1A = "p1a"
    const val P1B = "p1b"
    const val P2A = "p2a"
    const val P2B = "p2b"
  }

  @Test
  fun visitMove() {
    assertMoveLog(
      "|move|p2b: Miraidon|Draco Meteor|p1b: Ogerpon",
      P2B, "Miraidon", "Draco Meteor", P1B, "Ogerpon", false
    )
  }

  @Test
  fun visitSpreadMove() {
    assertMoveLog(
      "|move|p1a: Calyrex|Astral Barrage|p2a: Whimsicott|[spread] p2a,p2b",
      P1A, "Calyrex", "Astral Barrage", P2A, "Whimsicott", true
    )
  }

  @Test
  fun visitMoveWithNoTarget() {
    assertMoveLog(
      "|move|p1b: Raging Bolt|Thunderclap||[still]",
      P1B, "Raging Bolt", "Thunderclap", null, null, false
    )
  }

  @Test
  fun visitJoin() {
    assertJoinLog("|j|☆SlimReaperVGC", "☆SlimReaperVGC")
  }

  @Test
  fun visitPlayer() {
    assertPlayerLog("|player|p1|SlimReaperVGC|n|1358", "p1", "SlimReaperVGC", "n", "1358")
  }

  @Test
  fun visitPlayerNoRating() {
    assertPlayerLog("|player|p2|jarmanvgc|burglar", "p2", "jarmanvgc", "burglar", null)
  }

  @Test
  fun visitPokeMale() {
    assertPokeLog("|poke|p2|Whimsicott, L50, M|", "p2", "Whimsicott", 50, "M")
  }

  @Test
  fun visitPokeFemale() {
    assertPokeLog("|poke|p1|Rillaboom, L50, F|", "p1", "Rillaboom", 50, "F")
  }

  @Test
  fun visitPokeNoGender() {
    assertPokeLog("|poke|p1|Calyrex-Shadow, L50|", "p1", "Calyrex-Shadow", 50, null)
  }

  @Test
  fun visitSwitchWithHp() {
    assertSwitchLog("|switch|p2b: Entei|Entei, L50|18/100", P2B, "Entei", 18)
  }

  @Test
  fun visitSwitchFullHp() {
    assertSwitchLog("|switch|p1a: Rillaboom|Rillaboom, L50, F|100/100", P1A, "Rillaboom", 100)
  }

  @Test
  fun visitTerastallizeElectric() {
    assertTerastallizeLog("|-terastallize|p1b: Raging Bolt|Electric", P1B, "Raging Bolt", "Electric")
  }

  @Test
  fun visitTerastallizeNormal() {
    assertTerastallizeLog("|-terastallize|p2b: Entei|Normal", P2B, "Entei", "Normal")
  }

  @Test
  fun visitDamage() {
    assertDamageLog("|-damage|p2b: Entei|12/100", P2B, "12/100", null)
  }

  @Test
  fun visitDamageWithSource() {
    assertDamageLog("|-damage|p2b: Entei|6/100|[from] Spiky Shield|[of] p1b: Ogerpon", P2B, "6/100", "[from] Spiky Shield|[of] p1b: Ogerpon")
  }

  @Test
  fun visitFaint() {
    assertFaintLog("|faint|p1a: Rillaboom", P1A)
  }

  @Test
  fun visitHeal() {
    assertHealLog("|-heal|p2b: Entei|18/100|[from] Grassy Terrain", P2B, "18/100", "[from] Grassy Terrain")
  }

  @Test
  fun visitAbilityAsOne() {
    assertAbilityLog("|-ability|p1a: Calyrex|As One", P1A, "As One")
  }

  @Test
  fun visitAbilityUnnerve() {
    assertAbilityLog("|-ability|p1a: Calyrex|Unnerve", P1A, "Unnerve")
  }

  @Test
  fun visitFieldStartElectricTerrain() {
    assertFieldStartLog("|-fieldstart|move: Electric Terrain|[from] ability: Hadron Engine|[of] p2a: Miraidon", "move: Electric Terrain", "[from] ability: Hadron Engine", "[of] p2a: Miraidon")
  }

  @Test
  fun visitFieldStartGrassyTerrain() {
    assertFieldStartLog("|-fieldstart|move: Grassy Terrain|[from] ability: Grassy Surge|[of] p1a: Rillaboom", "move: Grassy Terrain", "[from] ability: Grassy Surge", "[of] p1a: Rillaboom")
  }

  @Test
  fun visitEndItemBoosterEnergy() {
    assertEndItemLog("|-enditem|p1b: Raging Bolt|Booster Energy", P1B, "Booster Energy")
  }

  @Test
  fun visitActivateProtosynthesis() {
    assertActivateLog("|-activate|p1b: Raging Bolt|ability: Protosynthesis|[fromitem]", P1B, "ability: Protosynthesis", "[fromitem]")
  }

  @Test
  fun visitBoostSpa() {
    assertBoostLog("|-boost|p1a: Calyrex|spa|1", P1A, "spa", 1)
  }

  @Test
  fun visitUnboostSpa() {
    assertUnboostLog("|-unboost|p2b: Miraidon|spa|2", P2B, "spa", 2)
  }

  @Test
  fun visitSingleTurnProtect() {
    assertSingleTurnLog("|-singleturn|p1a: Calyrex|Protect", P1A, "Protect")
  }

  @Test
  fun visitSingleTurnFollowMe() {
    assertSingleTurnLog("|-singleturn|p1b: Ogerpon|move: Follow Me", P1B, "move: Follow Me")
  }

  @Test
  fun visitFailThunderclap() {
    assertFailLog("|-fail|p1b: Raging Bolt", P1B)
  }

  @Test
  fun visitCrit() {
    assertCritLog("|-crit|p2a: Whimsicott", P2A)
  }

  @Test
  fun visitSuperEffective() {
    assertSuperEffectiveLog("|-supereffective|p1a: Rillaboom", P1A)
  }

  @Test
  fun visitResisted() {
    assertResistedLog("|-resisted|p2a: Chien-Pao", P2A)
  }

  @Test
  fun visitSideStartLightScreen() {
    assertSideStartLog("|-sidestart|p2: jarmanvgc|move: Light Screen", "p2: jarmanvgc", "move: Light Screen")
  }

  @Test
  fun visitSideEndLightScreen() {
    assertSideEndLog("|-sideend|p2: jarmanvgc|move: Light Screen", "p2: jarmanvgc", "move: Light Screen")
  }

  @Test
  fun visitRatingUpdate() {
    assertRatingUpdateRawLog(
      "|raw|SlimReaperVGC's rating: 1358 &rarr; <strong>1332</strong><br />(-26 for losing)",
      "SlimReaperVGC's rating: 1358 &rarr; <strong>1332</strong><br />(-26 for losing)",
      "SlimReaperVGC", 1358, 1332
    )
  }

  @Test
  fun visitWin() {
    assertWinLog("|win|jarmanvgc", "jarmanvgc")
  }

}