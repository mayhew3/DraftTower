package com.mayhew3.drafttower.client;

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.server.DraftControllerImpl;
import com.mayhew3.drafttower.server.TeamDraftOrder;
import com.mayhew3.drafttower.server.TestDraftTowerWebSocket;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.CurrentTimeProvider.FakeCurrentTimeProvider;
import com.mayhew3.drafttower.shared.DraftStatus;

import java.util.Map;

/**
 * Dependency injector for test client-side dependencies.
 */
@GinModules(DraftTowerTestGinModule.class)
public interface DraftTowerTestGinjector extends Ginjector {
  MainPageWidget getMainPageWidget();

  BeanFactory getBeanFactory();

  TestDraftTowerWebSocket getWebSocket();
  TestScheduler getScheduler();
  DraftControllerImpl getDraftController();
  FakeCurrentTimeProvider getCurrentTimeProvider();

  DraftStatus getDraftStatus();
  @TeamTokens Map<String, TeamDraftOrder> getTeamTokens();
}