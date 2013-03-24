package com.mayhew3.drafttower.server;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Live database-related dependency bindings.
 */
public class ServerDatabaseModule extends AbstractModule {
  @Provides
  public DataSource getDatabase() throws NamingException {
    Context initCtx = new InitialContext();
    Context envCtx = (Context) initCtx.lookup("java:comp/env");
    return (DataSource) envCtx.lookup("jdbc/MySQL");
  }

  @Override
  protected void configure() {
    bind(PlayerDataSource.class).to(PlayerDataSourceImpl.class);
    bind(TeamDataSource.class).to(TeamDataSourceImpl.class);
  }
}