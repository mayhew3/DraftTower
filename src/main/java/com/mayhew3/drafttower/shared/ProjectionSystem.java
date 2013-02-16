package com.mayhew3.drafttower.shared;

/** Projection data sources. */
public enum ProjectionSystem {
  CBS("CBS Sports"),
  ACCUSCORE("Accuscore"),
  CUSTOM("Custom");

  private final String displayName;

  ProjectionSystem(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
