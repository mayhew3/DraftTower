package com.mayhew3.drafttower.server.database;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateCBSProjections extends DatabaseUtility {

  private static final Logger logger = Logger.getLogger(UpdateCBSProjections.class.getName());

  public static void main(String[] args) throws SQLException {
    initConnection();

//    updateAllPlayersInTable("tmp_cbsbatting");
//    updateAllPlayersInTable("tmp_cbspitching");
//    updateAllPlayersInTable("tmp_cbspitchers_adv");
    updateAllPlayersInTable("tmp_eligibility");
//    updateAllPlayersInTable("cbs_draftaverages");
  }

  private static void updateAllPlayersInTable(String tableName) throws SQLException {
    String sourceSQL = "SELECT * FROM " + tableName;
    ResultSet sourceResults = executeQuery(sourceSQL);

    PreparedStatement updateStatement = getPreparedStatement("UPDATE " + tableName + " SET PlayerString = ? WHERE id = ?");

    while (hasMoreElements(sourceResults)) {
      updatePlayer(sourceResults, updateStatement);
    }

    updateStatement.close();
  }

  private static void updatePlayer(ResultSet sourceResults, PreparedStatement updateStatement) {
    Integer id = getInt(sourceResults, "id");
    String playerString = getString(sourceResults, "PlayerString");

    logger.log(Level.INFO, "Updating player '" + playerString + "' (" + id + ")");

    String replaced = playerString.replace(" |", "");
    replaced = replaced.replace(" \\*", "");
    replaced = replaced.trim();

    if (!replaced.equals(playerString)) {
      executePreparedUpdateWithParamsWithoutClose(updateStatement, replaced, id);
      logger.log(Level.INFO, "Replaced and updated: '" + replaced + "'");
    }
  }
}
