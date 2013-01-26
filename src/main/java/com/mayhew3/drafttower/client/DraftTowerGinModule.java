package com.mayhew3.drafttower.client;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.shared.BeanFactory;

/**
 * Dependency injection module for client-side dependencies.
 */
public class DraftTowerGinModule extends AbstractGinModule {
  @Override
  protected void configure() {
    bind(BeanFactory.class).in(Singleton.class);
  }
}