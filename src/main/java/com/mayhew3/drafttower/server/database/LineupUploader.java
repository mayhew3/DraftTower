package com.mayhew3.drafttower.server.database;

import com.google.gwt.thirdparty.guava.common.collect.Lists;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class LineupUploader extends DatabaseUtility {

  static enum RosterPlace {ACTIVE("Active"), RESERVE("Reserve"), INJURED("Injured");
    private String dbString;

    RosterPlace(String dbString) {
      this.dbString = dbString;
    }

    public String getDbString() {
      return dbString;
    }
  }
  static enum PlayerType {BATTER, PITCHER}

  static List<String> battingPositions = Lists.newArrayList(
      "C",
      "1B",
      "2B",
      "3B",
      "SS",
      "OF",
      "DH"
  );

  static List<String> pitchingPositions = Lists.newArrayList(
      "P"
  );


  public static void main(String... args) throws IOException, ParseException, SQLException {
    LineupUploader utility = new LineupUploader();
    utility.runUpdate();
  }

  public void runUpdate() throws IOException, SQLException {
    initConnection();

    PreparedStatement battingStatement = getPreparedStatement("SELECT ID FROM weekly_batting WHERE Player = ? AND PeriodNumber = ?");
    PreparedStatement pitchingStatement = getPreparedStatement("SELECT ID FROM weekly_pitching WHERE Player = ? AND PeriodNumber = ?");

    PreparedStatement battingUpdate = getPreparedStatement("UPDATE weekly_batting " +
        "SET FantasyTeam = ?, FantasyStatus = ? " +
        "WHERE ID = ?");
    PreparedStatement pitchingUpdate = getPreparedStatement("UPDATE weekly_pitching " +
        "SET FantasyTeam = ?, FantasyStatus = ? " +
        "WHERE ID = ?");

    for (int weekNumber = 1; weekNumber <= 25; weekNumber++) {
      logger.log(Level.INFO, "Processing Lineups for Week " + weekNumber + "...");
      File file = new File("resources/Lineups/lineup(" + weekNumber + ").csv");

      FileReader fileReader = new FileReader(file);
      BufferedReader bufferedReader = new BufferedReader(fileReader);

      String line;

      String currentTeam = null;

      while ((line = bufferedReader.readLine()) != null) {
        List<String> allFields = Lists.newArrayList(line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"));

        String firstField = allFields.get(0);
        RosterPlace rosterPlace = getRosterPlace(firstField);

        if (rosterPlace == null) {
          if (firstField.endsWith(" Batters") || firstField.endsWith(" Pitchers")) {
            String teamName = firstField.replace(" Batters", "").replace(" Pitchers", "");
            if (!Objects.equals(teamName, currentTeam)) {
              logger.log(Level.INFO, "- " + teamName);
            }
            currentTeam = teamName;
          }
        } else {
          PlayerType playerType = getPlayerType(firstField, rosterPlace);
          PreparedStatement selectStatement = playerType == PlayerType.BATTER ? battingStatement : pitchingStatement;

          String playerString = allFields.get(1).replace("\"", "");

          List<String> potentialMatches = getPotentialMatches(playerString);

          Integer playerMatch = findPlayerAndPeriodMatch(potentialMatches, weekNumber, selectStatement);
          if (playerMatch == null) {
            throw new RuntimeException("Unfound player: " + playerString);
          }

          PreparedStatement updateStatement = playerType == PlayerType.BATTER ? battingUpdate : pitchingUpdate;

          executePreparedUpdateWithParamsWithoutClose(updateStatement, currentTeam, rosterPlace.getDbString(), playerMatch);
        }
      }
    }
  }

  private PlayerType getPlayerType(String firstColumn, RosterPlace rosterPlace) {
    String benchPosition = parsePosition(firstColumn, rosterPlace);
    if (battingPositions.contains(benchPosition)) {
      return PlayerType.BATTER;
    } else if (pitchingPositions.contains(benchPosition)) {
      return PlayerType.PITCHER;
    } else {
      throw new RuntimeException("Unrecognized position from: " + firstColumn);
    }
  }

  private String parsePosition(String firstColumn, RosterPlace rosterPlace) {
    if (rosterPlace == RosterPlace.RESERVE) {
      return firstColumn.replace("Bench (", "").replace(")", "");
    } else if (rosterPlace == RosterPlace.INJURED) {
      return firstColumn.replace("Injured (", "").replace(")", "");
    } else {
      return firstColumn;
    }
  }

  private Integer findPlayerAndPeriodMatch(List<String> potentialMatches, Integer weekNumber, PreparedStatement statement) throws SQLException {
    for (String potentialMatch : potentialMatches) {
      ResultSet resultSet = executePreparedStatementWithParams(statement, potentialMatch, weekNumber);
      if (resultSet.next()) {
        return resultSet.getInt("ID");
      }
    }
    return null;
  }

  private static RosterPlace getRosterPlace(String column) {
    if (battingPositions.contains(column) || pitchingPositions.contains(column)) {
      return RosterPlace.ACTIVE;
    } else if (column.startsWith("Bench")) {
      return RosterPlace.RESERVE;
    } else if (column.startsWith("Injured")) {
      return RosterPlace.INJURED;
    }
    return null;
  }

  private static List<String> getPotentialMatches(String playerString) {
    String[] split = playerString.split("\\(");

    String fullName = split[0];

    String[] split1 = split[1].split("\\)");

    String commaSeparatedPositions = split1[0];
    String teamAbbreviation = split1[1].trim();

    List<String> positions = Lists.newArrayList(commaSeparatedPositions.split(","));

    if (positions.contains("OF")) {
      positions.add("LF");
      positions.add("CF");
      positions.add("RF");
    }

    if (positions.contains("P")) {
      positions.add("SP");
      positions.add("RP");
    } else {
      positions.add("DH");
    }

    List<String> potentialMatches = new ArrayList<>();
    for (String position : positions) {
      potentialMatches.add(fullName + " " + position + " " + teamAbbreviation);
    }

    return potentialMatches;
  }


}
