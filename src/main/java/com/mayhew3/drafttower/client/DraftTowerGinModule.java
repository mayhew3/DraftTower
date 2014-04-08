package com.mayhew3.drafttower.client;

import com.google.gwt.inject.client.AbstractGinModule;

/**
 * Dependency injection module for production client-side dependencies.
 */
public class DraftTowerGinModule extends AbstractGinModule {
  @Override
  protected void configure() {
    install(new DraftTowerTestSafeGinModule());
    install(new DraftTowerLiveGinModule());
  }
}