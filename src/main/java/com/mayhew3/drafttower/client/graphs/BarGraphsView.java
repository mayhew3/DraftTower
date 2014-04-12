package com.mayhew3.drafttower.client.graphs;

import com.mayhew3.drafttower.shared.PlayerColumn;

/**
 * Interface description...
 */
public interface BarGraphsView {
  void clear();

  void updateBar(PlayerColumn statColumn,
      Float myValue, Float avgValue);
}