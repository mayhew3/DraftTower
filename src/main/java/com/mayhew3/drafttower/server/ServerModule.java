package com.mayhew3.drafttower.server;

import com.google.common.collect.*;
import com.google.gwt.inject.rebind.adapter.GinModuleAdapter;
import com.google.inject.*;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.QueueEntry;
import com.mayhew3.drafttower.shared.SharedModule;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
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

  @Provides @Keepers
  public Map<Integer, List<Integer>> getKeepers(PlayerDataSource playerDataSource) {
    // TODO(m3): real keeper list (or query) goes here.
    return ImmutableMap.<Integer, List<Integer>>builder()
        .put(1, Lists.newArrayList(9057, 9981))
        .put(2, Lists.newArrayList(8854))
        .put(3, Lists.newArrayList(7217, 11079, 9503))
        .build();
  }

  @Override
  protected void configure() {
    install(new GinModuleAdapter(new SharedModule()));
    bind(DraftController.class).asEagerSingleton();
    bind(new TypeLiteral<Map<String, Integer>>() {})
        .annotatedWith(TeamTokens.class)
        .toInstance(Maps.<String, Integer>newHashMap());
    bind(new TypeLiteral<ListMultimap<String, QueueEntry>>() {})
        .annotatedWith(Queues.class)
        .toInstance(ArrayListMultimap.<String, QueueEntry>create());
  }
}