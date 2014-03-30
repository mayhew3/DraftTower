package com.mayhew3.drafttower.server;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Singleton;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Live dependency bindings.
 */
public class ServerProductionModule extends AbstractModule {
  @Provides
  public DataSource getDatabase() throws NamingException {
    Context initCtx = new InitialContext();
    Context envCtx = (Context) initCtx.lookup("java:comp/env");
    return (DataSource) envCtx.lookup("jdbc/MySQL");
  }

  @Override
  protected void configure() {
    bind(DraftTimer.class).to(DraftTimerImpl.class).in(Singleton.class);
    bind(PlayerDataSource.class).to(PlayerDataSourceImpl.class);
    bind(TeamDataSource.class).to(TeamDataSourceImpl.class);
  }
}