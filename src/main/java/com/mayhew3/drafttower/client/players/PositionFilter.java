package com.mayhew3.drafttower.client.players;

import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.Position;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * Option in the position filters bar.
 */
public interface PositionFilter {
  String getName();

  boolean apply(Player player, EnumSet<Position> excludedPositions);

  boolean showExcludeCheckboxWhenSelected(Position position);

  Position getPositionForExcludeCheckbox();

  boolean isPitcherFilter();

  boolean isPitchersAndBattersFilter();

  Comparator<Player> getWizardComparator(boolean ascending);

  String getWizard(Player player);
}