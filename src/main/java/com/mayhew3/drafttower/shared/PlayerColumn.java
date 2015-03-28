package com.mayhew3.drafttower.shared;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Player column values.
 */
public enum PlayerColumn {
  NAME("Name", "Name", "Player", false, true),
  MLB("Tm", "MLB Team", "MLBTeam", false, true),
  ELIG("Elig", "Eligible Positions", "Eligibility", false, true),
  G("G", "Games Played", "G", true, false),
  GS("GS", "Games Started", "GS", true, false),
  AB("AB", "At Bats", "AB", true, false),
  BA("BA", "Batting Average", "BA", true, false),
  OBP("OBP", "On-Base Percentage", "OBP", true, false),
  SLG("SLG", "Slugging Percentage", "SLG", true, false),
  RHR("R-", "Runs - Home Runs", "RHR", true, false),
  R("R", "Runs", "R", true, false),
  RBI("RBI", "Runs Batted In", "RBI", true, false),
  H("H", "Hits", "H", true, false),
  HR("HR", "Home Runs", "HR", true, false),
  SBCS("SB-", "Stolen Bases - Caught Stealing", "SBC", true, false),
  INN("INN", "Innings Pitched", "INN", true, false),
  ERA("ERA", "Earned Run Average", "ERA", true, true),
  WHIP("WHIP", "Walks and Hits per Inning Pitched", "WHIP", true, true),
  WL("W-", "Wins - Losses", "WL", true, false),
  W("W", "Wins", "W", true, false),
  L("L", "Losses", "L", true, true),
  K("K", "Strikeouts", "K", true, false),
  KO("K", "Strikeouts", "KO", true, true),
  S("S", "Saves", "S", true, false),
  SB("SB", "Stolen Bases", "SB", true, false),
  BB("BB", "Walks", "BB", true, false),
  BBI("BB", "Walks Issued", "BBI", true, true),
  HA("HA", "Hits Allowed", "HA", true, true),
  HRA("HR", "Home Runs Allowed", "HRA", true, true),
  PTS("PTS", "Points", "FPTS", true, false),
  RANK("Rank", "Average of CBS Experts' Top 300 Rankings", "Rank", true, true),
  DRAFT("Draft", "Average Position in CBS Drafts", "Draft", true, true),
  WIZARD("Wizard", "Wizard", "Wizard", true, false),
  MYRANK("MyRank", "My Rank", "MyRank", true, true);

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
        if (rtn == 0) {
          return PTS.getComparator(ascending).compare(p1, p2);
        }
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
      case GS:
        return player.getGS();
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
      case KO:
        return player.getKO();
      case SB:
        return player.getSB();
      case BB:
        return player.getBB();
      case W:
        return player.getW();
      case L:
        return player.getL();
      case BA:
        return player.getBA();
      case R:
        return player.getR();
      case H:
        return player.getH();
      case BBI:
        return player.getBBI();
      case HA:
        return player.getHA();
      case HRA:
        return player.getHRA();
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
      case GS:
        player.setGS(value);
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
      case KO:
        player.setKO(value);
        break;
      case SB:
        player.setSB(value);
        break;
      case BB:
        player.setBB(value);
        break;
      case W:
        player.setW(value);
        break;
      case L:
        player.setL(value);
        break;
      case BA:
        player.setBA(value);
        break;
      case R:
        player.setR(value);
        break;
      case H:
        player.setH(value);
        break;
      case BBI:
        player.setBBI(value);
        break;
      case HA:
        player.setHA(value);
        break;
      case HRA:
        player.setHRA(value);
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

  private static void setWizard(Player player, String value, Position position) {
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

  public static PlayerColumn[] valuesForScoring() {
    if (Scoring.CATEGORIES) {
      return new PlayerColumn[]{
          NAME, MLB, ELIG, G, AB, OBP, SLG, RHR, RBI, HR, SBCS, INN, ERA, WHIP, WL, K, S, RANK, DRAFT, WIZARD, MYRANK
      };
    } else {
      return new PlayerColumn[]{
          NAME, MLB, ELIG, G, GS, AB, H, HR, RBI, R, KO, SB, BB, BA, OBP, SLG, INN, K, ERA, W, L, S, WHIP, BBI, HA, HRA, PTS, RANK, DRAFT, WIZARD, MYRANK
      };
    }
  }

  public static void calculateWizardScores(Iterable<Player> players, Map<Long, Float> pickProbabilityPredictions) {
    for (final Position position : Position.REAL_POSITIONS) {
      Iterable<Player> positionPlayers = Iterables.filter(players, new Predicate<Player>() {
        @Override
        public boolean apply(Player player) {
          return Position.apply(player, EnumSet.of(position));
        }
      });
      int topPointsSum = 0;
      int topPointsSumSq = 0;
      for (Player player : Iterables.limit(positionPlayers, 10)) {
        topPointsSum += Integer.parseInt(PTS.get(player));
        topPointsSumSq += Math.pow(Integer.parseInt(PTS.get(player)), 2);
      }
      double stdDev = Math.sqrt((topPointsSumSq / 10f) - Math.pow(topPointsSum / 10f, 2));
      List<Player> playersByPtsAsc = Lists.reverse(Lists.newArrayList(Iterators.limit(
          Ordering.from(PTS.getComparator(false)).sortedCopy(positionPlayers).iterator(), 10)));
      Player prevPlayer = null;
      for (Player player : playersByPtsAsc) {
        double wizardScore = 0;
        if (prevPlayer != null) {
          wizardScore = Double.parseDouble(getWizard(prevPlayer, EnumSet.of(position)));
          int pointDiff = Integer.parseInt(PTS.get(player)) - Integer.parseInt(PTS.get(prevPlayer));
          double weight;
          if (pickProbabilityPredictions.containsKey(player.getPlayerId())) {
            weight = Math.max(.05f, pickProbabilityPredictions.get(player.getPlayerId()));
          } else {
            weight = .05f;
          }
          wizardScore += pointDiff / (stdDev / 3) * weight;
        }
        String wizardScoreFormatted = Double.toString(wizardScore);
        if (wizardScoreFormatted.contains(".")) {
          wizardScoreFormatted = wizardScoreFormatted.substring(0,
              Math.min(wizardScoreFormatted.indexOf('.') + 3, wizardScoreFormatted.length()));
        }
        setWizard(player, wizardScoreFormatted, position);
        prevPlayer = player;
      }
    }
  }
}
