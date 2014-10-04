package com.mayhew3.drafttower.shared;

import com.google.common.base.Strings;
import com.google.inject.Inject;

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
    player.setTeam("XXX");
    player.setEligibility(position.getShortName());
    if (i == 5) {
      player.setInjury("busted wang");
    }
    player.setCBSId(playerId);
    player.setPlayerId(playerId);
    if (position == P) {
      player.setG(Integer.toString(i / 5 + 1));
      player.setINN(Integer.toString(i + 10));
      player.setK(Integer.toString(i + 5));
      player.setERA(Float.toString(2 + i / 100f));
      player.setWHIP(Float.toString(1 + i / 100f));
      player.setS(Integer.toString(i));
      player.setWL(Integer.toString(i));
      player.setWizardP(Float.toString(-3 + i / 20f));
    } else {
      player.setAB(Integer.toString(i * 40));
      player.setHR(Integer.toString(i));
      player.setRBI(Integer.toString(i * 3));
      player.setRHR(Integer.toString(i * 3));
      player.setOBP(Float.toString(.25f + i / 100f));
      player.setSLG(Float.toString(.4f + i / 50f));
      player.setSBCS(Integer.toString(i));
      switch (position) {
        case C:
          player.setWizardC(Float.toString(-3 + i / 20f));
          break;
        case FB:
          player.setWizard1B(Float.toString(-3 + i / 20f));
          break;
        case SB:
          player.setWizard2B(Float.toString(-3 + i / 20f));
          break;
        case TB:
          player.setWizard3B(Float.toString(-3 + i / 20f));
          break;
        case SS:
          player.setWizardSS(Float.toString(-3 + i / 20f));
          break;
        case OF:
          player.setWizardOF(Float.toString(-3 + i / 20f));
          break;
      }
      player.setWizardDH(Float.toString(-3 + i / 20f));
    }
    player.setDraft(Integer.toString(playerId));
    player.setRank(Integer.toString(playerId));
    player.setMyRank(Integer.toString(playerId));
    return player;
  }
}