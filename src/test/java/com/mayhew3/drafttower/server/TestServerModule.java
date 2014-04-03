package com.mayhew3.drafttower.server;

import com.google.gwt.inject.client.AbstractGinModule;

import javax.inject.Singleton;

/**
 * Test versions of dependencies.
 */
public class TestServerModule extends AbstractGinModule {
  @Override
  protected void configure() {
    bind(DraftTimer.class).to(TestDraftTimer.class).in(Singleton.class);
    bind(PlayerDataSource.class).to(TestPlayerDataSource.class);
    bind(TeamDataSource.class).to(TestTeamDataSource.class);
  }
}