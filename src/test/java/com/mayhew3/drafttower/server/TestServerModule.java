package com.mayhew3.drafttower.server;

import com.google.inject.AbstractModule;

import javax.inject.Singleton;

/**
 * Test versions of dependencies.
 */
public class TestServerModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(DraftTimer.class).to(TestDraftTimer.class).in(Singleton.class);
    bind(PlayerDataSource.class).to(TestPlayerDataSource.class);
    bind(TeamDataSource.class).to(TestTeamDataSource.class);
  }
}