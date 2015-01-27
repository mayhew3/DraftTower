package com.mayhew3.drafttower.shared;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * Player column values.
 */
public enum PlayerColumn {
  NAME("Name", "Name", "Player", false, true),
  MLB("Tm", "MLB Team", "MLBTeam", false, true),
  ELIG("Elig", "Eligible Positions", "Eligibility", false, true),
  G("G", "Games Played", "G", true, false),
  AB("AB", "At Bats", "AB", true, false),
  OBP("OBP", "On-Base Percentage", "OBP", true, false),
  SLG("SLG", "Slugging Percentage", "SLG", true, false),
  RHR("R-", "Runs - Home Runs", "RHR", true, false),
  RBI("RBI", "Runs Batted In", "RBI", true, false),
  HR("HR", "Home Runs", "HR", true, false),
  SBCS("SB-", "Stolen Bases - Caught Stealing", "SBC", true, false),
  INN("INN", "Innings Pitched", "INN", true, false),
  ERA("ERA", "Earned Run Average", "ERA", true, true),
  WHIP("WHIP", "Walks and Hits per Inning Pitched", "WHIP", true, true),
  WL("W-", "Wins - Losses", "WL", true, false),
  K("K", "Strikeouts (Pitcher)", "K", true, false),
  S("S", "Saves", "S", true, false),
  PTS("PTS", "Points", "PTS", true, true),
  RANK("Rank", "Rank", "Rank", true, true),
  DRAFT("Draft", "Average Position in CBS Drafts", "Draft", true, true),
  WIZARD("Wizard", "Wizard", "Wizard", true, false),
  MYRANK("MyRank", "MyRank", "MyRank", true, true);

  private final String shortName;
  private final String longName;
  private final String columnName;
  private final boolean sortAsNumber;
  private final boolean defaultSortAscending;
  private final int nullValue;

  PlayerColumn(String shortName, String longName, String columnName, boolean sortAsNumber, boolean defaultSortAscending) {
    this.shortName = shortName;
    this.longName = longName;
    this.columnName = columnName;
    this.sortAsNumber = sortAsNumber;
    this.defaultSortAscending = defaultSortAscending;
    nullValue = defaultSortAscending ? Integer.MAX_VALUE : Integer.MIN_VALUE;
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
        String p1Value = get(p1);
        String p2Value = get(p2);
        if (sortAsNumber) {
          rtn = Float.compare(p1Value == null ? nullValue : Float.parseFloat(p1Value),
              p2Value == null ? nullValue : Float.parseFloat(p2Value));
        } else {
          rtn = p1Value.compareTo(p2Value);
        }
        return ascending ? rtn : -rtn;
      }
    };
  }

  public static Comparator<Player> getWizardComparator(
      final boolean ascending, final EnumSet<Position> positions) {
    return new Comparator<Player>() {
      @Override
      public int compare(Player p1, Player p2) {
        int rtn;
        String p1Value = getWizard(p1, positions);
        String p2Value = getWizard(p2, positions);
        rtn = Float.compare(p1Value == null ? Float.MIN_VALUE : Float.parseFloat(p1Value),
            p2Value == null ? Float.MIN_VALUE : Float.parseFloat(p2Value));
        return ascending ? rtn : -rtn;
      }
    };
  }

  public boolean isDefaultSortAscending() {
    return defaultSortAscending;
  }

  public String get(Player player) {
    switch (this) {
      case NAME:
        return player.getName();
      case MLB:
        return player.getTeam();
      case ELIG:
        return player.getEligibility();
      case G:
        return player.getG();
      case AB:
        return player.getAB();
      case OBP:
        return player.getOBP();
      case SLG:
        return player.getSLG();
      case RHR:
        return player.getRHR();
      case RBI:
        return player.getRBI();
      case HR:
        return player.getHR();
      case SBCS:
        return player.getSBCS();
      case INN:
        return player.getINN();
      case ERA:
        return player.getERA();
      case WHIP:
        return player.getWHIP();
      case WL:
        return player.getWL();
      case K:
        return player.getK();
      case S:
        return player.getS();
      case PTS:
        return player.getPoints();
      case RANK:
        return player.getRank();
      case DRAFT:
        return player.getDraft();
      case MYRANK:
        return player.getMyRank();
      default:
        throw new IllegalArgumentException();
    }
  }

  public void set(Player player, String value) {
    switch (this) {
      case NAME:
        player.setName(value);
        break;
      case MLB:
        player.setTeam(value);
        break;
      case ELIG:
        player.setEligibility(value);
        break;
      case G:
        player.setG(value);
        break;
      case AB:
        player.setAB(value);
        break;
      case OBP:
        player.setOBP(value);
        break;
      case SLG:
        player.setSLG(value);
        break;
      case RHR:
        player.setRHR(value);
        break;
      case RBI:
        player.setRBI(value);
        break;
      case HR:
        player.setHR(value);
        break;
      case SBCS:
        player.setSBCS(value);
        break;
      case INN:
        player.setINN(value);
        break;
      case ERA:
        player.setERA(value);
        break;
      case WHIP:
        player.setWHIP(value);
        break;
      case WL:
        player.setWL(value);
        break;
      case K:
        player.setK(value);
        break;
      case S:
        player.setS(value);
        break;
      case PTS:
        player.setPoints(value);
        break;
      case RANK:
        player.setRank(value);
        break;
      case DRAFT:
        player.setDraft(value);
        break;
      case MYRANK:
        player.setMyRank(value);
        break;
      default:
        throw new IllegalArgumentException();
    }
  }

  public static String getWizard(Player player, EnumSet<Position> positions) {
    if (positions.isEmpty()) {
      return getWizard(player, Position.REAL_POSITIONS);
    }
    return getMax(
        positions.contains(Position.P) ? player.getWizardP() : null,
        positions.contains(Position.C) ? player.getWizardC() : null,
        positions.contains(Position.FB) ? player.getWizard1B() : null,
        positions.contains(Position.SB) ? player.getWizard2B() : null,
        positions.contains(Position.TB) ? player.getWizard3B() : null,
        positions.contains(Position.SS) ? player.getWizardSS() : null,
        positions.contains(Position.OF) ? player.getWizardOF() : null,
        positions.contains(Position.DH) ? player.getWizardDH() : null);
  }

  private static String getMax(String... values) {
    Float max = null;
    for (String value : values) {
      if (value != null) {
        float parsedValue = Float.parseFloat(value);
        if (max == null || parsedValue > max) {
          max = parsedValue;
        }
      }
    }
    return max == null ? null : max.toString();
  }

  public static void setWizard(Player player, String value, Position position) {
    switch (position) {
      case P:
        player.setWizardP(value);
        break;
      case C:
        player.setWizardC(value);
        break;
      case FB:
        player.setWizard1B(value);
        break;
      case SB:
        player.setWizard2B(value);
        break;
      case TB:
        player.setWizard3B(value);
        break;
      case SS:
        player.setWizardSS(value);
        break;
      case OF:
        player.setWizardOF(value);
        break;
      case DH:
        player.setWizardDH(value);
        break;
      default:
        throw new IllegalArgumentException();
    }
  }
}
