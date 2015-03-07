package com.mayhew3.drafttower.client.players;

import com.mayhew3.drafttower.client.OpenPositions;
import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.Position;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Comparator;
import java.util.EnumSet;

/**
 * Position filter for unfilled positions.
 */
@Singleton
public class UnfilledPositionsFilter implements PositionFilter {

  private final OpenPositions openPositions;

  @Inject
  public UnfilledPositionsFilter(OpenPositions openPositions) {
    this.openPositions = openPositions;
  }

  @Override
  public String getName() {
    return "Unfilled";
  }

  @Override
  public boolean apply(Player player, EnumSet<Position> excludedPositions) {
    EnumSet<Position> positions = EnumSet.copyOf(getPositions());
    positions.removeAll(excludedPositions);
    if (positions.isEmpty()) {
      return Position.apply(player, Position.REAL_POSITIONS);
    } else {
      return Position.apply(player, positions);
    }
  }

  @Override
  public boolean showExcludeCheckboxWhenSelected(Position position) {
    return getPositions().contains(position);
  }

  @Override
  public Position getPositionForExcludeCheckbox() {
    return null;
  }

  @Override
  public boolean isPitcherFilter() {
    return Position.isPitcherFilter(getPositions());
  }

  @Override
  public boolean isPitchersAndBattersFilter() {
    return Position.isPitchersAndBattersFilter(getPositions());
  }

  @Override
  public Comparator<Player> getWizardComparator(boolean ascending) {
    return PlayerColumn.getWizardComparator(ascending, getPositions());
  }

  @Override
  public String getWizard(Player player) {
    return PlayerColumn.getWizard(player, getPositions());
  }

  private EnumSet<Position> getPositions() {
    return openPositions.get();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof UnfilledPositionsFilter;
  }
}