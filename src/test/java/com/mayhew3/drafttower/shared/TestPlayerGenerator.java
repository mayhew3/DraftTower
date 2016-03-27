package com.mayhew3.drafttower.shared;

import com.google.common.base.Strings;

import javax.inject.Inject;

import static com.mayhew3.drafttower.shared.PlayerColumn.*;
import static com.mayhew3.drafttower.shared.Position.P;

/**
 * Generates {@link Player} objects for tests.
 */
public class TestPlayerGenerator {
  private final BeanFactory beanFactory;

  @Inject
  public TestPlayerGenerator(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  public Player generatePlayer(int playerId, Position position, int i) {
    Player player = beanFactory.createPlayer().as();
    player.setName(Strings.repeat(Integer.toString(playerId), 10));
    player.setTeam("XXX" + (playerId % 30));
    player.setEligibility(position.getShortName());
    if (i == 5) {
      player.setInjury("busted wang");
    }
    player.setCBSId(playerId);
    player.setPlayerId(playerId);
    if (position == P) {
      INN.set(player, Integer.toString(i + 10));
      K.set(player, Integer.toString(i + 5));
      if (i % 2 == 1) {
        S.set(player, Integer.toString((i / 3) + 1));
        GS.set(player, "0");
      } else {
        S.set(player, "0");
        GS.set(player, Integer.toString(i / 5 + 1));
      }
      G.set(player, Integer.toString(i / 5 + 1));
      ERA.set(player, Float.toString(2 + i / 100f));
      WHIP.set(player, Float.toString(1 + i / 100f));
      if (Scoring.CATEGORIES) {
        WL.set(player, Integer.toString(i));
      } else {
        W.set(player, Integer.toString(i / 6));
        L.set(player, Integer.toString(i / 6));
        HA.set(player, Integer.toString(i * 2));
        HRA.set(player, Integer.toString(i / 4));
        BBI.set(player, Integer.toString(i));
      }
    } else {
      AB.set(player, Integer.toString(i * 20));
      HR.set(player, Integer.toString(i));
      RBI.set(player, Integer.toString(i * 3));
      OBP.set(player, Float.toString(.25f + i / 100f));
      SLG.set(player, Float.toString(.4f + i / 50f));
      if (Scoring.CATEGORIES) {
        RHR.set(player, Integer.toString(i * 3));
        SBCS.set(player, Integer.toString(i));
      } else {
        G.set(player, Integer.toString(i * 3));
        H.set(player, Integer.toString(i * 5));
        R.set(player, Integer.toString(i * 3));
        SB.set(player, Integer.toString(i));
        BB.set(player, Integer.toString(i * 2));
        KO.set(player, Integer.toString(i + 5));
        BA.set(player, Float.toString(.2f + i / 100f));
      }
    }
    if (Scoring.POINTS) {
      PTS.set(player, Integer.toString(20 * i));
    }
    player.setDraft(Integer.toString(playerId + 1));
    player.setRank(Integer.toString(playerId + 1));
    player.setMyRank(Integer.toString(playerId + 1));
    return player;
  }
}