package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.Position;

import javax.inject.Inject;

/**
 * Prediction model which returns made-up probabilities via a basic formula.
 */
public class TestPredictionModel implements PredictionModel {

  @Inject
  public TestPredictionModel() {}

  @Override
  public float getPrediction(Position position, int rank, int pickNum, Integer... numFilled) {
    return .9f - rank * .1f + pickNum * .0001f + numFilled[0] * .01f;
  }

  @Override
  public PlayerColumn getSortCol() {
    return PlayerColumn.DRAFT;
  }

  @Override
  public boolean getSortColAscending() {
    return true;
  }
}