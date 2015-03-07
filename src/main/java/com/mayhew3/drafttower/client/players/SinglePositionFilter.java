package com.mayhew3.drafttower.client.players;

import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.Position;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * Option in the position filters bar for a single position.
 */
public class SinglePositionFilter implements PositionFilter {
  private final Position position;

  public SinglePositionFilter(Position position) {
    this.position = position;
  }

  @Override
  public String getName() {
    return position.getShortName();
  }

  @Override
  public boolean apply(Player player, EnumSet<Position> excludedPositions) {
    return Position.apply(player, EnumSet.of(position));
  }

  public Position getPosition() {
    return position;
  }

  @Override
  public boolean showExcludeCheckboxWhenSelected(Position position) {
    return false;
  }

  @Override
  public Position getPositionForExcludeCheckbox() {
    return position;
  }

  @Override
  public boolean isPitcherFilter() {
    return position == Position.P;
  }

  @Override
  public boolean isPitchersAndBattersFilter() {
    return false;
  }

  @Override
  public Comparator<Player> getWizardComparator(boolean ascending) {
    return PlayerColumn.getWizardComparator(ascending, EnumSet.of(position));
  }

  @Override
  public String getWizard(Player player) {
    return PlayerColumn.getWizard(player, EnumSet.of(position));
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof SinglePositionFilter && ((SinglePositionFilter) obj).position == position;
  }
}