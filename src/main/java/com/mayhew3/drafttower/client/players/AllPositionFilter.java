package com.mayhew3.drafttower.client.players;

import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.Position;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * Filter for all positions.
 */
public class AllPositionFilter implements PositionFilter {
  @Override
  public String getName() {
    return "All";
  }

  @Override
  public boolean apply(Player player, EnumSet<Position> excludedPositions) {
    return true;
  }

  @Override
  public boolean showExcludeCheckboxWhenSelected(Position position) {
    return false;
  }

  @Override
  public Position getPositionForExcludeCheckbox() {
    return null;
  }

  @Override
  public boolean isPitcherFilter() {
    return false;
  }

  @Override
  public boolean isPitchersAndBattersFilter() {
    return true;
  }

  @Override
  public Comparator<Player> getWizardComparator(boolean ascending) {
    return PlayerColumn.getWizardComparator(ascending, EnumSet.allOf(Position.class));
  }

  @Override
  public String getWizard(Player player) {
    return PlayerColumn.getWizard(player, EnumSet.allOf(Position.class));
  }
}