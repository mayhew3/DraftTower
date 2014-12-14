package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.gwt.cell.client.FieldUpdater;
import com.mayhew3.drafttower.shared.Player;

import static com.mayhew3.drafttower.shared.PlayerColumn.MYRANK;

/**
 * {@link FieldUpdater} for editing player rank.
 */
class MyRankFieldUpdater implements FieldUpdater<Player, String> {
  private final UnclaimedPlayerDataProvider presenter;

  public MyRankFieldUpdater(UnclaimedPlayerDataProvider presenter) {
    this.presenter = presenter;
  }

  @Override
  public void update(int index, Player player, String newRank) {
    String currentRank = MYRANK.get(player);
    if (!newRank.equals(currentRank)) {
      try {
        presenter.changePlayerRank(player,
            Integer.parseInt(newRank), Integer.parseInt(currentRank));
      } catch (NumberFormatException e) {
        // whatevs
      }
    }
  }
}