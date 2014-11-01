package com.mayhew3.drafttower.client.players;

import com.mayhew3.drafttower.shared.Position;

import java.util.EnumSet;

/**
 * Option in the position filters bar.
 */
public class PositionFilter {
  private final String name;
  private final EnumSet<Position> positions;

  public PositionFilter(String name, EnumSet<Position> positions) {
    this.name = name;
    this.positions = positions;
  }

  public PositionFilter(Position singlePosition) {
    this.name = singlePosition.getShortName();
    this.positions = EnumSet.of(singlePosition);
  }

  public String getName() {
    return name;
  }

  public EnumSet<Position> getPositions() {
    return positions;
  }
}