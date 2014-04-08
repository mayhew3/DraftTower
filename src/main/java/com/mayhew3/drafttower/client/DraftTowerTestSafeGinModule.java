package com.mayhew3.drafttower.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.mayhew3.drafttower.server.GinBindingAnnotations.QueueAreaTop;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.SharedModule;

/**
 * Dependency injection module for test-safe client-side dependencies.
 */
public class DraftTowerTestSafeGinModule extends AbstractGinModule {

  @Provides @QueueAreaTop
  public int getQueueAreaTop(MainPageWidget mainPageWidget) {
    return mainPageWidget.getQueueAreaTop();
  }

  @Override
  protected void configure() {
    install(new SharedModule());
    bind(BeanFactory.class).in(Singleton.class);
    bind(DraftSocketHandler.class).asEagerSingleton();
    bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);
    bind(new TypeLiteral<AsyncDataProvider<Player>>() {})
        .to(CachingUnclaimedPlayerDataProvider.class).in(Singleton.class);
  }
}