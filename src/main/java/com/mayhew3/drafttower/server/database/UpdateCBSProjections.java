package com.mayhew3.drafttower.server.database;


import com.google.common.collect.Lists;

import java.sql.ResultSet;
import java.util.List;

public class UpdateCBSProjections extends DatabaseUtility {
  public static void main(String[] args) {
    List<String> numberColumns = Lists.newArrayList("Rank", "1B", "2B", "3B", "AB", "BB", "CS", "G", "H", "HR", "KO", "R", "RBI", "SB");
    List<String> stringColumns = Lists.newArrayList("FirstName", "LastName", "MLBTeam", "Position", "PlayerString");

    String sourceSQL = "SELECT * FROM tmp_cbsbatting";
    ResultSet sourceResults = executeQuery(sourceSQL);

    while (hasMoreElements(sourceResults)) {
      int playerID = getInt(sourceResults, "PlayerID");
      String playerSQL = "SELECT * FROM Players WHERE ID = ?";

      ResultSet playerResult = prepareAndExecuteStatementFetch(playerSQL, playerID);
      hasMoreElements(playerResult);

//      String updateSQL = "UPDATE Players "
    }
  }
}
