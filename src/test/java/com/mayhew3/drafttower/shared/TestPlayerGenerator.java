package com.mayhew3.drafttower.shared;

import com.google.common.base.Strings;
import com.google.inject.Inject;

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
      G.set(player, Integer.toString(i / 5 + 1));
      INN.set(player, Integer.toString(i + 10));
      K.set(player, Integer.toString(i + 5));
      ERA.set(player, Float.toString(2 + i / 100f));
      WHIP.set(player, Float.toString(1 + i / 100f));
      S.set(player, Integer.toString(i));
      WL.set(player, Integer.toString(i));
    } else {
      AB.set(player, Integer.toString(i * 20));
      HR.set(player, Integer.toString(i));
      RBI.set(player, Integer.toString(i * 3));
      RHR.set(player, Integer.toString(i * 3));
      OBP.set(player, Float.toString(.25f + i / 100f));
      SLG.set(player, Float.toString(.4f + i / 50f));
      SBCS.set(player, Integer.toString(i));
      if (Scoring.CATEGORIES) {
        player.setWizardDH(Float.toString(-3 + i / 20f));
      } else {
        player.setWizardDH(Float.toString(20f * i));
      }
    }
    PlayerColumn.setWizard(player, Float.toString(-3 + i / 20f), position);
    if (Scoring.POINTS) {
      PTS.set(player, Float.toString(20f * i));
    }
    player.setDraft(Integer.toString(playerId + 1));
    player.setRank(Integer.toString(playerId + 1));
    player.setMyRank(Integer.toString(playerId + 1));
    return player;
  }
}