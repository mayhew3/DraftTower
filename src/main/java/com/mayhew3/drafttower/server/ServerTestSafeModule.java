package com.mayhew3.drafttower.server;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mayhew3.drafttower.server.BindingAnnotations.*;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.PlayerDataSet;
import com.mayhew3.drafttower.shared.QueueEntry;
import dagger.Module;
import dagger.Provides;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dependency bindings which can be used as-is in tests.
 */
@Module
public class ServerTestSafeModule {

  public static class EagerSingletons {
    @Inject DraftController draftController;
    @Inject public EagerSingletons() {}
  }


  @Provides @Singleton @Keepers
  public static ListMultimap<TeamDraftOrder, Integer> getKeepers(PlayerDataProvider playerDataProvider) {
    try {
      return playerDataProvider.getAllKeepers();
    } catch (DataSourceException e) {
      throw new RuntimeException(e);
    }
  }

  @Provides @Singleton @Queues
  public static ListMultimap<TeamDraftOrder, QueueEntry> getQueues() {
    return ArrayListMultimap.create();
  }

  @Provides @Singleton @AutoPickWizards
  public static Map<TeamDraftOrder, PlayerDataSet> getAutoPickWizardTables(TeamDataSource teamDataSource) {
    return teamDataSource.getAutoPickWizards();
  }

  @Provides @Singleton @MinClosers
  public static Map<TeamDraftOrder, Integer> getMinClosers(TeamDataSource teamDataSource) {
    return teamDataSource.getMinClosers();
  }

  @Provides @Singleton @MaxClosers
  public static Map<TeamDraftOrder, Integer> getMaxClosers(TeamDataSource teamDataSource) {
    return teamDataSource.getMaxClosers();
  }

  @Provides @Singleton
  public static DraftStatus getDraftStatus(BeanFactory beanFactory) {
    return beanFactory.createDraftStatus().as();
  }

  @Provides @Singleton @TeamTokens
  public static Map<String, TeamDraftOrder> getTeamTokens() {
    return new ConcurrentHashMap<>();
  }

  @Provides @Singleton
  public static DraftController getDraftController(DraftControllerImpl impl) {
    return impl;
  }
}