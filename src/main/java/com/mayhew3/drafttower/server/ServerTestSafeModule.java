package com.mayhew3.drafttower.server;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.server.BindingAnnotations.Keepers;
import com.mayhew3.drafttower.server.BindingAnnotations.Queues;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.server.BindingAnnotations.AutoPickWizards;
import com.mayhew3.drafttower.shared.*;

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

  @Provides @Singleton @AutoPickWizards
  public Map<Integer, PlayerDataSet> getAutoPickWizardTables(TeamDataSource teamDataSource) {
    return teamDataSource.getAutoPickWizards();
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