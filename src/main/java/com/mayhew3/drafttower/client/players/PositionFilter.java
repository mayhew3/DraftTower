package com.mayhew3.drafttower.client.players;

import com.mayhew3.drafttower.shared.Position;

import java.util.EnumSet;

/**
 * Option in the position filters bar.
 */
class PositionFilter {
  private final String name;
  private final EnumSet<Position> positions;

  PositionFilter(String name, EnumSet<Position> positions) {
    this.name = name;
    this.positions = positions;
  }

  PositionFilter(Position singlePosition) {
    this.name = singlePosition.getShortName();
    this.positions = EnumSet.of(singlePosition);
  }

  String getName() {
    return name;
  }

  EnumSet<Position> getPositions() {
    return positions;
  }
}