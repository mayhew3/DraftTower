package com.mayhew3.drafttower.server;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.shared.BeanFactory;

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

  @Override
  protected void configure() {
    bind(DraftController.class).asEagerSingleton();
    bind(Map.class)
        .annotatedWith(TeamTokens.class)
        .toInstance(Maps.newHashMap());
  }
}