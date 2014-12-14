package com.mayhew3.drafttower.server;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.server.BindingAnnotations.AutoPickWizards;
import com.mayhew3.drafttower.server.BindingAnnotations.Keepers;
import com.mayhew3.drafttower.server.BindingAnnotations.Queues;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.PlayerDataSet;
import com.mayhew3.drafttower.shared.QueueEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * Dependency bindings which can be used as-is in tests.
 */
public class ServerTestSafeModule extends AbstractGinModule {

  @Provides @Singleton @Keepers
  public ListMultimap<TeamDraftOrder, Integer> getKeepers(PlayerDataSource playerDataSource) throws DataSourceException {
    return playerDataSource.getAllKeepers();
  }

  @Provides @Singleton @Queues
  public ListMultimap<TeamDraftOrder, QueueEntry> getQueues() {
    return ArrayListMultimap.create();
  }

  @Provides @Singleton @AutoPickWizards
  public Map<TeamDraftOrder, PlayerDataSet> getAutoPickWizardTables(TeamDataSource teamDataSource) {
    return teamDataSource.getAutoPickWizards();
  }

  @Provides @Singleton
  public DraftStatus getDraftStatus(BeanFactory beanFactory) {
    return beanFactory.createDraftStatus().as();
  }

  @Provides @Singleton @TeamTokens
  public Map<String, TeamDraftOrder> getTeamTokens() {
    return new HashMap<>();
  }

  @Override
  protected void configure() {
    bind(DraftController.class).to(DraftControllerImpl.class).asEagerSingleton();
  }
}