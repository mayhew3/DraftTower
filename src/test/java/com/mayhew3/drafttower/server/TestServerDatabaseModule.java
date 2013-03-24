package com.mayhew3.drafttower.server;

import com.google.inject.AbstractModule;

/**
 * Test versions of database dependencies.
 */
public class TestServerDatabaseModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(PlayerDataSource.class).to(TestPlayerDataSource.class);
    bind(TeamDataSource.class).to(TestTeamDataSource.class);
  }
}