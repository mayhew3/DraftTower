package com.mayhew3.drafttower.shared;

import java.util.Comparator;

/**
 * Player column values.
 */
public enum PlayerColumn {
  NAME("Name", "Name", "Player", false),
  MLB("Tm", "MLB Team", "MLBTeam", false),
  ELIG("Elig", "Eligible Positions", "Eligibility", false),
  G("G", "Games Played", "G", true),
  AB("AB", "At Bats", "AB", true),
  OBP("OBP", "On-Base Percentage", "OBP", true),
  SLG("SLG", "Slugging Percentage", "SLG", true),
  RHR("R-", "Runs - Home Runs", "RHR", true),
  RBI("RBI", "Runs Batted In", "RBI", true),
  HR("HR", "Home Runs", "HR", true),
  SBCS("SB-", "Stolen Bases - Caught Stealing", "SBC", true),
  INN("INN", "Innings Pitched", "INN", true),
  ERA("ERA", "Earned Run Average", "ERA", true),
  WHIP("WHIP", "Walks and Hits per Inning Pitched", "WHIP", true),
  WL("W-", "Wins - Losses", "WL", true),
  K("K", "Strikeouts (Pitcher)", "K", true),
  S("S", "Saves", "S", true),
  RANK("Rank", "Rank", "Rank", true),
  DRAFT("Draft", "Average Position in CBS Drafts", "Draft", true),
  WIZARD("Wizard", "Wizard", "Wizard", true),
  MYRANK("MyRank", "MyRank", "MyRank", true);

  private final String shortName;
  private final String longName;
  private final String columnName;
  private final boolean sortAsNumber;

  PlayerColumn(String shortName, String longName, String columnName, boolean sortAsNumber) {
    this.shortName = shortName;
    this.longName = longName;
    this.columnName = columnName;
    this.sortAsNumber = sortAsNumber;
  }

  public String getShortName() {
    return shortName;
  }

  public String getLongName() {
    return longName;
  }

  public String getColumnName() {
    return columnName;
  }

  public Comparator<Player> getComparator(final boolean ascending) {
    return new Comparator<Player>() {
      @Override
      public int compare(Player p1, Player p2) {
        int rtn;
        String p1Value = p1.getColumnValues().get(PlayerColumn.this);
        String p2Value = p2.getColumnValues().get(PlayerColumn.this);
        if (sortAsNumber) {
          rtn = Float.compare(p1Value == null ? 0 : Float.parseFloat(p1Value),
              p2Value == null ? 0 : Float.parseFloat(p2Value));
        } else {
          rtn = p1Value.compareTo(p2Value);
        }
        return ascending ? rtn : -rtn;
      }
    };
  }
}
