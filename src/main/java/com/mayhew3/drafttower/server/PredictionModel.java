package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.Position;

/**
 * Model which provides predicted probability of players being selected.
 */
public interface PredictionModel {
  float getPrediction(Position position, int rank, int pickNum, Integer... numFilled);

  PlayerColumn getSortCol();

  boolean getSortColAscending();
}