package com.mayhew3.drafttower.server.database;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class InjuryUpdater {
  private SQLConnection connection;

  public InjuryUpdater(SQLConnection connection) {
    this.connection = connection;
  }

  public static void main(String... args) throws URISyntaxException, SQLException {
    InjuryUpdater injuryUpdater = new InjuryUpdater(new MySQLConnectionFactory().createConnection());
    injuryUpdater.updateDatabase();
  }

  public void updateDatabase() throws SQLException {
    connection.prepareAndExecuteStatementUpdate("update players p\n" +
        "inner join cbsids cbs\n" +
        " on cbs.cbs_id = p.cbs_id\n" +
        "set p.injury = cbs.injurynote,\n" +
        "    p.updatetime = NOW()\n" +
        "where not p.injury <=> cbs.injurynote");
  }

}
