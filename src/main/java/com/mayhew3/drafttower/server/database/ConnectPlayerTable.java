package com.mayhew3.drafttower.server.database;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectPlayerTable {
  private SQLConnection connection;
  private Integer scoringSystemYear;

  public ConnectPlayerTable(SQLConnection connection) {
    this.connection = connection;

    // only needs to be updated when scoring system changes, and new row is added to 'scoringbatting' and 'scoringpitching'.
    this.scoringSystemYear = 2015;
  }

  public static void main(String... args) throws URISyntaxException, SQLException, IOException {
    SQLConnection connection = new MySQLConnectionFactory().createConnection();
    ConnectPlayerTable connectPlayerTable = new ConnectPlayerTable(connection);
    connectPlayerTable.updateDatabase();
  }

  public void updateDatabase() throws SQLException {
    updateBattingProjections();
    updatePitchingProjections();
    updateFPTS();
  }

  private void updateBattingProjections() throws SQLException {
    final String tmp_cbsbatting = "tmp_cbsbatting";

    Date statDate = getLatestStatDate(tmp_cbsbatting);

    updatePlayerIDs(tmp_cbsbatting, statDate);
    updateBatterEligibilityColumn(statDate);

    deletePartialProjections("projectionsBatting");
    checkPlayerIDs(tmp_cbsbatting, statDate);
    checkNumProjectionsReasonable(tmp_cbsbatting, 2500, 4000, statDate);

    insertProjectionsBatting(statDate);
    updateBatterCalculatedStats();

    redoBattingAveragesTable();
    redoBattingAveragesProjectionRows();
  }

  private void updatePitchingProjections() throws SQLException {
    final String tmp_cbspitching = "tmp_cbspitching";

    Date statDate = getLatestStatDate(tmp_cbspitching);

    updatePlayerIDs(tmp_cbspitching, statDate);
    updatePitcherEligibilityColumn();

    deletePartialProjections("projectionsPitching");
    checkPlayerIDs(tmp_cbspitching, statDate);
    checkNumProjectionsReasonable(tmp_cbspitching, 1700, 3000, statDate);

    insertProjectionsPitching(statDate);
    updatePitcherCalculatedStats(15, 20);

    redoPitchingAveragesTable();
    redoPitchingAveragesProjectionRows();
  }

  private void redoPitchingAveragesProjectionRows() throws SQLException {
    connection.prepareAndExecuteStatementUpdate("delete from projectionsPitching where DataSource = (select id from data_sources where name = ?)", "Averages");
    connection.prepareAndExecuteStatementUpdate("insert into projectionsPitching (DataSource, PlayerID, APP, BBI, BS, CG, ER, GS, HA, HB, HRA, IRS, K, L, OUTS, QS, S, SO, SOP, W)\n" +
        "select (select id from data_sources where name = ?), PlayerID, APP, BBI, BS, CG, ER, GS, HA, HB, HRA, IRS, K, L, OUTS, QS, S, SO, SOP, W\n" +
        "from tmp_projavgpitching", "Averages");
  }

  private void redoPitchingAveragesTable() throws SQLException {
    connection.prepareAndExecuteStatementUpdate("truncate table tmp_projavgpitching");
    connection.prepareAndExecuteStatementUpdate("alter table tmp_projavgpitching auto_increment = 1");
    connection.prepareAndExecuteStatementUpdate("INSERT INTO `uncharted`.`tmp_projavgpitching`\n" +
        " (`PlayerID`,`Rating`,`Rank`,`NumProjections`,`APP`,`BBI`, `BS`, `CG`,`ER`,`GS`,`HA`,`HB`,`HRA`,`IRS`,`K`,`L`,`OUTS`,`QS`,`S`,`SO`,`SOP`,`W`,`WL`,`INN`)\n" +
        "SELECT `PlayerID`,AVG(`Rating`),AVG(`Rank`),COUNT(1),AVG(`APP`),AVG(`BBI`), AVG(`BS`),AVG(`CG`),AVG(`ER`),AVG(`GS`),AVG(`HA`),AVG(`HB`),AVG(`HRA`),\n" +
        "        AVG(`IRS`),AVG(`K`),AVG(`L`),AVG(`OUTS`),AVG(`QS`),AVG(`S`),AVG(`SO`),AVG(`SOP`),AVG(`W`),AVG(`WL`),AVG(`INN`)\n" +
        "FROM projectionsPitching\n" +
        "GROUP BY PlayerID");
  }

  private void updatePitcherCalculatedStats(final int gamesStarted, final int saves) throws SQLException {
    connection.prepareAndExecuteStatementUpdate(
        "update projectionsPitching\n" +
        "set WL = (W-L),\n" +
        "    INN = OUTS/3,\n" +
        "    ERA = ER/OUTS*27,\n" +
        "    WHIP = (HA+BBI)/(OUTS/3),\n" +
        "    TeamERA = (ER+467)/(OUTS/3+1038)*9,\n" +
        "    TeamWHIP = ((HA+1023)+(BBI+329)) / (OUTS/3+1038)");
    connection.prepareAndExecuteStatementUpdate(
        "UPDATE projectionsPitching\n" +
        "SET Role = ?\n" +
        "WHERE GS > ?", "Starter", gamesStarted);
    connection.prepareAndExecuteStatementUpdate(
        "UPDATE projectionsPitching\n" +
        "SET Role = ?\n" +
        "WHERE S > ?", "Closer", saves);
  }

  private void updateFPTS() throws SQLException {

    connection.prepareAndExecuteStatementUpdate("UPDATE projectionsBatting\n" +
        "SET FPTS = (SELECT (\n" +
        " projectionsBatting.`1B` * sb.`1B` +\n" +
        " projectionsBatting.`2B` * sb.`2B` +\n" +
        " projectionsBatting.`3B` * sb.`3B` +\n" +
        " projectionsBatting.`AB` * sb.`AB` +\n" +
        " projectionsBatting.`BB` * sb.`BB` +\n" +
        " projectionsBatting.`CS` * sb.`CS` +\n" +
        " projectionsBatting.`HR` * sb.`HR` +\n" +
        " projectionsBatting.`KO` * sb.`KO` +\n" +
        " projectionsBatting.`R` * sb.`R` +\n" +
        " projectionsBatting.`RBI` * sb.`RBI` +\n" +
        " projectionsBatting.`SB` * sb.`SB` \n" +
        " ) FROM scoringBatting sb\n" +
        "   WHERE Year = ?)", scoringSystemYear);

    connection.prepareAndExecuteStatementUpdate(
        "UPDATE projectionsPitching\n" +
            "SET FPTS = (SELECT (\n" +
            " projectionsPitching.`BBI` * sp.`BBI` +\n" +
            " projectionsPitching.`BS` * sp.`BS` +\n" +
            " projectionsPitching.`ER` * sp.`ER` +\n" +
            " projectionsPitching.CG * sp.CG +\n" +
            " projectionsPitching.`HA` * sp.`HA` +\n" +
            " (projectionsPitching.`OUTS` /3) * sp.`INN` +\n" +
            " projectionsPitching.`K` * sp.`K` +\n" +
            " projectionsPitching.`L` * sp.`L` +\n" +
            " projectionsPitching.`S` * sp.`S` +\n" +
            " projectionsPitching.`SO` * sp.`SO` +\n" +
            " projectionsPitching.`W` * sp.`W`\n" +
            " ) FROM scoringPitching sp\n" +
            "   WHERE Year = ?)", scoringSystemYear);
  }

  private void insertProjectionsPitching(Date statDate) throws SQLException {
    connection.prepareAndExecuteStatementUpdate("insert into projectionsPitching (DataSource, PlayerID, APP, BBI, BS, CG, ER, GS, HA, HRA, K, L, OUTS, QS, S, SO, W)\n" +
        "select (select id from data_sources where name = ?), PlayerID, APP, BBI, BS, CG, ER, GS, HA, HRA, K, L, OUTS, QS, S, SO, W\n" +
        "from tmp_cbspitching " +
        "where StatDate = ?", "CBSSports", statDate);
  }


  private void redoBattingAveragesProjectionRows() throws SQLException {
    connection.prepareAndExecuteStatementUpdate("delete from projectionsBatting where DataSource = (select id from data_sources where name = ?)", "Averages");
    connection.prepareAndExecuteStatementUpdate("insert into projectionsBatting (DataSource, PlayerID, `1B`, `2B`, `3B`, AB, BB, CS, G, H, HP, HR, KO, R, RBI, SB)\n" +
        "select (select id from data_sources where name = ?), PlayerID, `1B`, `2B`, `3B`, AB, BB, CS, G, H, HP, HR, KO, R, RBI, SB\n" +
        "from tmp_projavgbatting", "Averages");
  }


  private void redoBattingAveragesTable() throws SQLException {
    connection.prepareAndExecuteStatementUpdate("truncate table tmp_projavgbatting");
    connection.prepareAndExecuteStatementUpdate("alter table tmp_projavgbatting auto_increment = 1");
    connection.prepareAndExecuteStatementUpdate("INSERT INTO `uncharted`.`tmp_projavgbatting`\n" +
        " (`PlayerID`,`Rating`,`Rank`,`NumProjections`,`1B`,`2B`,`3B`,`AB`,`BB`,`CS`,`G`,`H`,`HP`,`HR`,`KO`,`R`,`RBI`,`SB`,`OBP`,`SLG`,`SBC`,`RHR`,`TB`)\n" +
        "SELECT PlayerID, AVG(`Rating`),AVG(`Rank`),COUNT(1),AVG(`1B`),AVG(`2B`),AVG(`3B`),AVG(`AB`),AVG(`BB`),AVG(`CS`),AVG(`G`),AVG(`H`),AVG(`HP`),AVG(`HR`),\n" +
        "        AVG(`KO`),AVG(`R`),AVG(`RBI`),AVG(`SB`),AVG(`OBP`),AVG(`SLG`),AVG(`SBC`),AVG(`RHR`),AVG(`TB`)\n" +
        "FROM projectionsBatting\n" +
        "GROUP BY PlayerID");
  }

  private void updateBatterCalculatedStats() throws SQLException {
    connection.prepareAndExecuteStatementUpdate("update projectionsBatting " +
        "set H = 1B+2B+3B+HR, " +
        "    OBP = (H+BB+IFNULL(HP, 0))/(AB+BB+IFNULL(HP, 0)), " +
        "    TB = (1B+2*2B+3*3B+4*HR), " +
        "    SBC = (SB-CS), " +
        "    RHR = (R-HR), " +
        "    TeamOBP = (H+1216+BB+489+IFNULL(HP, 0))/(AB+4284+BB+489+IFNULL(HP, 0))");

    connection.prepareAndExecuteStatementUpdate("update projectionsBatting " +
        "set SLG = TB/AB, " +
        "    TeamSLG = (TB+2036)/(AB+4284)");
  }

  private void insertProjectionsBatting(Date statDate) throws SQLException {
    connection.prepareAndExecuteStatementUpdate("insert into projectionsBatting (DataSource, PlayerID, `1B`, `2B`, `3B`, AB, BB, CS, G, HR, KO, R, RBI, SB) " +
        "select (select id from data_sources where name = ?), PlayerID, `1B`, `2B`, `3B`, AB, BB, CS, G, HR, KO, R, RBI, SB " +
        "from tmp_cbsbatting " +
        "where StatDate = ?", "CBSSports", statDate);
  }

  private Date getLatestStatDate(final String tableName) throws SQLException {
    ResultSet resultSet = connection.prepareAndExecuteStatementFetch("select max(StatDate) as maxDate from " + tableName);
    resultSet.next();
    Date maxDate = resultSet.getDate("maxDate");
    return maxDate;
  }

  private void deletePartialProjections(final String tableName) throws SQLException {
    connection.prepareAndExecuteStatementUpdate("delete from " + tableName + " where DataSource IN (select id from data_sources where name in (?, ?))", "CBSSports", "Averages");
  }

  private void checkPlayerIDs(final String tableName, Date statDate) throws SQLException {
    ResultSet resultSet = connection.prepareAndExecuteStatementFetch("select count(1) as rowCount from " + tableName + " where PlayerID IS NULL AND StatDate = ?", statDate);
    resultSet.next();
    int rowCount = resultSet.getInt("rowCount");
    if (rowCount > 0) {
      throw new IllegalStateException("Expected " + tableName + ".PlayerID to be populated for all rows.");
    }
  }

  private void checkNumProjectionsReasonable(final String tableName, Integer min, Integer max, Date statDate) throws SQLException {
    ResultSet resultSet = connection.prepareAndExecuteStatementFetch("select count(1) as rowCount from " + tableName + " where StatDate = ?", statDate);
    resultSet.next();
    int rowCount = resultSet.getInt("rowCount");
    if (rowCount < min || rowCount > max) {
      throw new IllegalStateException("Expected total rows with StatDate '" + statDate + "' to be between " + min + " and " + max + ". Was: " + rowCount);
    }
  }

  private void updatePlayerIDs(final String tableName, Date statDate) throws SQLException {
    connection.prepareAndExecuteStatementUpdate(
        "UPDATE " + tableName + " cbs " +
            "SET cbs.PlayerID = (SELECT p.ID FROM players p WHERE cbs.Player = p.PlayerString) " +
            "WHERE cbs.StatDate = ?", statDate);
  }

  private void updateBatterEligibilityColumn(Date statDate) throws SQLException {
    connection.prepareAndExecuteStatementUpdate(
        "UPDATE players p " +
            "SET p.Eligibility = (SELECT e.Eligible " +
            "FROM tmp_eligibility e " +
            "WHERE e.Player = p.PlayerString " +
            "AND e.StatDate = ?)", statDate
    );
  }
  private void updatePitcherEligibilityColumn() throws SQLException {
    connection.prepareAndExecuteStatementUpdate(
        "UPDATE players p " +
            "SET p.Eligibility = ? " +
            "WHERE p.Position IN (?, ?, ?) " +
            "AND p.Eligibility IS NULL", "P", "P", "SP", "RP"
    );
  }
}
