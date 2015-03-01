package com.mayhew3.drafttower.client.players.unclaimed;

import com.mayhew3.drafttower.client.players.SinglePositionFilter;
import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.Position;

import java.util.EnumSet;

/**
 * Filter for starting pitchers.
 */
public class StarterPitcherFilter extends SinglePositionFilter {
  public StarterPitcherFilter() {
    super(Position.P);
  }

  @Override
  public String getName() {
    return "SP";
  }

  @Override
  public boolean apply(Player player, EnumSet<Position> excludedPositions) {
    if (!super.apply(player, excludedPositions)) {
      return false;
    }
    int starts = Integer.parseInt(PlayerColumn.GS.get(player));
    int saves = Integer.parseInt(PlayerColumn.S.get(player));
    return starts > saves;
  }

  @Override
  public Position getPositionForExcludeCheckbox() {
    return null;
  }
}