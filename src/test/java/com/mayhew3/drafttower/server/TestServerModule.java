package com.mayhew3.drafttower.server;

import com.google.gwt.core.client.GWT;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.CurrentTimeProvider;
import com.mayhew3.drafttower.shared.CurrentTimeProvider.FakeCurrentTimeProvider;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Test versions of dependencies.
 */
@Module
public class TestServerModule {

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
  public static PlayerDataSource getPlayerDataSource(BeanFactory beanFactory) {
    if (GWT.isClient()) {
      return new TestPlayerDataSourceClient(beanFactory);
    } else {
      return new TestPlayerDataSourceServer(beanFactory);
    }
  }

  @Provides @Singleton
  public static TeamDataSource getTeamDataSource(TestTeamDataSource impl) {
    return impl;
  }
}