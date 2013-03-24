package com.mayhew3.drafttower.server;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.server.BindingAnnotations.AutoPickTableSpecs;
import com.mayhew3.drafttower.server.BindingAnnotations.Keepers;
import com.mayhew3.drafttower.server.BindingAnnotations.Queues;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.QueueEntry;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;
import com.mayhew3.drafttower.shared.TableSpec;

import javax.servlet.ServletException;
import java.util.Map;

/**
 * Dependency bindings which can be used as-is in tests.
 */
public class ServerTestSafeModule extends AbstractModule {

  @Provides @Singleton
  public BeanFactory getBeanFactory() {
    return AutoBeanFactorySource.create(BeanFactory.class);
  }

  @Provides @Singleton @Keepers
  public ListMultimap<Integer, Integer> getKeepers(PlayerDataSource playerDataSource) throws ServletException {
    return playerDataSource.getAllKeepers();
  }

  @Provides @Singleton @Queues
  public ListMultimap<Integer, QueueEntry> getQueues() {
    return ArrayListMultimap.create();
  }

  @Provides @Singleton @AutoPickTableSpecs
  public Map<Integer, TableSpec> getAutoPickTableSpecs(@NumTeams int numTeams, TeamDataSource teamDataSource) {
    return teamDataSource.getAutoPickTableSpecs(numTeams);
  }

  @Provides @Singleton
  public DraftStatus getDraftStatus(BeanFactory beanFactory) {
    return beanFactory.createDraftStatus().as();
  }

  @Override
  protected void configure() {
    bind(DraftController.class).asEagerSingleton();
    bind(new TypeLiteral<Map<String, Integer>>() {})
        .annotatedWith(TeamTokens.class)
        .toInstance(Maps.<String, Integer>newHashMap());
  }
}