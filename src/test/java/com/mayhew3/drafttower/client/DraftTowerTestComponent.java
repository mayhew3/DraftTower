package com.mayhew3.drafttower.client;

import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.server.*;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.CurrentTimeProvider.FakeCurrentTimeProvider;
import com.mayhew3.drafttower.shared.DraftStatus;
import dagger.Component;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * Dependency injector for test client-side dependencies.
 */
@Component(modules = DraftTowerClientTestModule.class)
@Singleton
public interface DraftTowerTestComponent {

  class EagerSingletons {
    @Inject ServerTestSafeModule.EagerSingletons serverEagerSingletons;
    @Inject DraftTowerClientTestSafeModule.EagerSingletons clientEagerSingletons;
    @Inject public EagerSingletons() {}
  }

  EagerSingletons injectEager();

  MainPageWidget mainPageWidget();

  BeanFactory beanFactory();

  TestDraftTowerWebSocket webSocket();
  TestScheduler scheduler();
  DraftController draftController();
  PlayerDataSource playerDataSource();
  FakeCurrentTimeProvider currentTimeProvider();

  DraftStatus draftStatus();
  @TeamTokens Map<String, TeamDraftOrder> teamTokens();
}