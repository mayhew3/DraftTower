package com.mayhew3.drafttower.server;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.mayhew3.drafttower.shared.CurrentTimeProvider;
import com.mayhew3.drafttower.shared.CurrentTimeProvider.FakeCurrentTimeProvider;
import com.mysql.jdbc.Driver;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.inject.Singleton;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Test versions of dependencies, but with live in-memory database.
 */
public class TestServerDBModule extends AbstractGinModule {

  public static final String URL = "jdbc:mysql:mxj:///test";
  public static final String RESET_USERS =
      "update users " +
          "inner join teams on teams.userid=users.user " +
          "set user=cast(teams.id as char), pword=cast(teams.id as char) " +
          ";" +
      "update userrole " +
          "inner join teams on teams.userid=userrole.user " +
          "set user=cast(teams.id as char) " +
          ";" +
      "update teams set userid=cast(id as char);" +
      "";

  @Provides @Singleton
  public DataSource getDatabase() throws SQLException {
    String driverClassName = Driver.class.getName();
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName(driverClassName);
    dataSource.setUrl(URL);
    initDB(dataSource);
    return dataSource;
  }

  public static void resetDB(DataSource dataSource) throws SQLException {
    initDB(dataSource);
  }

  private static void initDB(DataSource dataSource) throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection,
          new EncodedResource(new FileSystemResource("database/UnchartedData2015.sql")),
          false, false, ScriptUtils.DEFAULT_COMMENT_PREFIX, ScriptUtils.DEFAULT_STATEMENT_SEPARATOR,
          "!@#$%^",  // nonsense block comment start delimiter to preserve block comments
          ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER);
      ScriptUtils.executeSqlScript(connection, new ByteArrayResource(RESET_USERS.getBytes()));
    }
  }

  @Override
  protected void configure() {
    bind(CurrentTimeProvider.class).to(FakeCurrentTimeProvider.class);
    bind(DraftTimer.class).to(TestDraftTimer.class).in(Singleton.class);
    bind(PredictionModel.class).to(TestPredictionModel.class);
    bind(PlayerDataSource.class).to(PlayerDataSourceImpl.class).in(Singleton.class);
    bind(TeamDataSource.class).to(TeamDataSourceImpl.class).in(Singleton.class);
  }
}