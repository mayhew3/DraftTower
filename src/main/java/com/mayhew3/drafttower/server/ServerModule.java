package com.mayhew3.drafttower.server;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.shared.BeanFactory;

/**
 * Server-side dependency module.
 */
public class ServerModule extends AbstractModule {

  @Provides @Singleton
  public BeanFactory getBeanFactory() {
    return AutoBeanFactorySource.create(BeanFactory.class);
  }

  @Override
  protected void configure() {
    bind(DraftController.class).asEagerSingleton();
  }
}