package com.mayhew3.drafttower.shared;

import java.util.EnumSet;

/**
 * Player position.
 */
public enum Position {
  P("P", "Pitcher"),
  C("C", "Catcher"),
  FB("1B", "First Base"),
  SB("2B", "Second Base"),
  TB("3B", "Third Base"),
  SS("SS", "Shortstop"),
  OF("OF", "Outfield"),
  DH("DH", "Designated Hitter"),

  // Misc:
  RS("RS", "Reserves");

  public static final EnumSet<Position> REAL_POSITIONS = EnumSet.of(P, C, FB, SB, TB, SS, OF, DH);

  private final String shortName;
  private final String longName;

  Position(String shortName, String longName) {
    this.shortName = shortName;
    this.longName = longName;
  }

  public static boolean isPitcherFilter(EnumSet<Position> positionFilter) {
    return positionFilter.equals(EnumSet.of(P));
  }

  public static boolean isPitchersAndBattersFilter(EnumSet<Position> positionFilter) {
    return positionFilter.isEmpty()
        || (positionFilter.contains(P) && positionFilter.size() > 1);
  }

  public String getShortName() {
    return shortName;
  }

  public String getLongName() {
    return longName;
  }

  public static Position fromShortName(String shortName) {
    for (Position position : values()) {
      if (position.getShortName().equals(shortName)) {
        return position;
      }
    }
    throw new IllegalArgumentException("No position " + shortName);
  }

  public static boolean apply(Player player, EnumSet<Position> positions) {
    if (positions.equals(EnumSet.of(DH))) {
      return !apply(player, EnumSet.of(P));
    }
    if (positions.contains(DH) && !apply(player, EnumSet.of(P))) {
      return true;
    }
    String eligibilities = PlayerColumn.ELIG.get(player);
    if (eligibilities == null) {
      // DAVID AARDSMA!!!!
      return false;
    }
    for (Position position : positions) {
      if (eligibilities.contains(position.getShortName())) {
        return true;
      }
    }
    return false;
  }
}
