package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.shared.CurrentTimeProvider;
import com.mayhew3.drafttower.shared.CurrentTimeProvider.FakeCurrentTimeProvider;
import com.mysql.jdbc.Driver;
import dagger.Module;
import dagger.Provides;
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
@Module
public class TestServerDBModule {

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
  public static DataSource getDatabase() {
    String driverClassName = Driver.class.getName();
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName(driverClassName);
    dataSource.setUrl(URL);
    try {
      initDB(dataSource);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
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

  @Provides
  public static CurrentTimeProvider getCurrentTimeProvider(FakeCurrentTimeProvider impl) {
    return impl;
  }

  @Provides @Singleton
  public static DraftTimer getDraftTimer(TestDraftTimer impl) {
    return impl;
  }

  @Provides
  public static PredictionModel getPredictionModel(TestPredictionModel impl) {
    return impl;
  }

  @Provides @Singleton
  public static PlayerDataSource getPlayerDataSource(PlayerDataSourceImpl impl) {
    return impl;
  }

  @Provides @Singleton
  public static TeamDataSource getTeamDataSource(TeamDataSourceImpl impl) {
    return impl;
  }
}