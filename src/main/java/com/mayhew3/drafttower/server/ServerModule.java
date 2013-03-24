package com.mayhew3.drafttower.server;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.gwt.inject.rebind.adapter.GinModuleAdapter;
import com.google.inject.*;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.shared.*;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Server-side dependency module.
 */
public class ServerModule extends AbstractModule {

  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  public static @interface TeamTokens {}

  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  public static @interface Keepers {}

  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  public static @interface Queues {}

  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  public static @interface AutoPickTableSpecs {}

  @Provides @Singleton
  public BeanFactory getBeanFactory() {
    return AutoBeanFactorySource.create(BeanFactory.class);
  }

  @Provides
  public DataSource getDatabase() throws NamingException {
    Context initCtx = new InitialContext();
    Context envCtx = (Context) initCtx.lookup("java:comp/env");
    return (DataSource) envCtx.lookup("jdbc/MySQL");
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
    install(new GinModuleAdapter(new SharedModule()));
    bind(DraftController.class).asEagerSingleton();
    bind(new TypeLiteral<Map<String, Integer>>() {})
        .annotatedWith(TeamTokens.class)
        .toInstance(Maps.<String, Integer>newHashMap());
  }
}