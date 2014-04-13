package com.mayhew3.drafttower.client.filledpositions;

import com.mayhew3.drafttower.shared.Position;

import java.util.Map;

/**
 * Interface for filled positions chart.
 */
public interface FilledPositionsView {
  void setCounts(Map<Position, Integer> counts);
}