package com.mayhew3.drafttower.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.client.GinBindingAnnotations.CurrentTime;
import com.mayhew3.drafttower.client.players.PlayerDragController;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.SharedModule;

/**
 * Dependency injection module for test-safe client-side dependencies.
 */
public class DraftTowerTestSafeGinModule extends AbstractGinModule {

  @Provides @CurrentTime
  public Double getCurrentTime() {
    return com.google.gwt.core.client.Duration.currentTimeMillis();
  }

  @Override
  protected void configure() {
    install(new SharedModule());
    bind(BeanFactory.class).in(Singleton.class);
    bind(DraftSocketHandler.class).asEagerSingleton();
    bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);
    bind(PlayerDragController.class).in(Singleton.class);
  }
}