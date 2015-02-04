package com.mayhew3.drafttower.server;

import com.google.gwt.core.client.GWT;
import com.google.gwt.inject.client.AbstractGinModule;
import com.mayhew3.drafttower.shared.CurrentTimeProvider;
import com.mayhew3.drafttower.shared.CurrentTimeProvider.FakeCurrentTimeProvider;

import javax.inject.Singleton;

/**
 * Test versions of dependencies.
 */
public class TestServerModule extends AbstractGinModule {
  @Override
  protected void configure() {
    bind(CurrentTimeProvider.class).to(FakeCurrentTimeProvider.class);
    bind(DraftTimer.class).to(TestDraftTimer.class).in(Singleton.class);
    bind(PredictionModel.class).to(TestPredictionModel.class);
    if (GWT.isClient()) {
      bind(PlayerDataSource.class).to(TestPlayerDataSourceClient.class).in(Singleton.class);
    } else {
      bind(PlayerDataSource.class).to(TestPlayerDataSourceServer.class).in(Singleton.class);
    }
    bind(TeamDataSource.class).to(TestTeamDataSource.class).in(Singleton.class);
  }
}