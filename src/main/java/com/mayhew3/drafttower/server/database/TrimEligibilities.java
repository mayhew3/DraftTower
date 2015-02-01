package com.mayhew3.drafttower.server.database;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.sql.ResultSet;
import java.util.List;

public class TrimEligibilities extends DatabaseUtility {

  public static void main(String[] arge) {
    initConnection();

    String sql = "SELECT * \n" +
        "FROM Players \n" +
        "WHERE Eligibility NOT IN ('P', 'DH'); " ;
    ResultSet resultSet = prepareAndExecuteStatementFetch(sql);

    while (hasMoreElements(resultSet)) {
      int playerID = getInt(resultSet, "ID");
      String eligibility = getString(resultSet, "Eligibility");

      List<String> allEm = Lists.newArrayList(eligibility.split(","));
      boolean replaced = allEm.remove("DH");

      if (replaced) {
        String shorterEligibilityString = Joiner.on(",").join(allEm);
        prepareAndExecuteStatementUpdate("UPDATE Players SET Eligibility = ? WHERE ID = ?", shorterEligibilityString, playerID);
      }
    }
  }
}
