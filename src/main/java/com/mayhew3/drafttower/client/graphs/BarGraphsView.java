package com.mayhew3.drafttower.client.graphs;

import com.mayhew3.drafttower.shared.PlayerColumn;

/**
 * Interface for bar graphs.
 */
public interface BarGraphsView {
  void clear();

  void updateBar(PlayerColumn statColumn, Float... values);
  void updatePitchingPointsBar(Float... values);
  void updateBattingPointsBar(Float... values);
}