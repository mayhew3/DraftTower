package com.mayhew3.drafttower.shared;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;

/** Data sources for player stats. */
public enum PlayerDataSet {
  CBSSPORTS("CBSSports"),
  ROTOWIRE("RotoWire"),
  GURU("GURU"),
  AVERAGES("Averages");

  private final String displayName;

  PlayerDataSet(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public static Optional<PlayerDataSet> getDataSetWithName(final String displayName) {
    ArrayList<PlayerDataSet> playerColumns = Lists.newArrayList(PlayerDataSet.values());

    return Iterables.tryFind(playerColumns, new Predicate<PlayerDataSet>() {
      @Override
      public boolean apply(PlayerDataSet input) {
        return displayName.equalsIgnoreCase(input.getDisplayName());
      }
    });
  }
}
