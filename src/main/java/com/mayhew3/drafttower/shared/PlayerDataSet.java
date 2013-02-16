package com.mayhew3.drafttower.shared;

/** Data sources for player stats. */
public enum PlayerDataSet {
  WIZARD("Wizard"),
  CBS("CBS Sports"),
  LAST_YEAR("2012"),
  CUSTOM("Custom");

  private final String displayName;

  PlayerDataSet(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
