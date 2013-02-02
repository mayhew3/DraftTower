package com.mayhew3.drafttower.server;

import com.google.common.collect.Maps;
import com.google.inject.*;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.shared.BeanFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
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

  @Override
  protected void configure() {
    bind(DraftController.class).asEagerSingleton();
    bind(new TypeLiteral<Map<String, Integer>>() {})
        .annotatedWith(TeamTokens.class)
        .toInstance(Maps.<String, Integer>newHashMap());
  }
}