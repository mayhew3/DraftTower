package com.mayhew3.drafttower.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.mayhew3.drafttower.client.players.unclaimed.UnclaimedPlayerDataProvider;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.SharedModule;

/**
 * Dependency injection module for test-safe client-side dependencies.
 */
public class DraftTowerTestSafeGinModule extends AbstractGinModule {

  @Override
  protected void configure() {
    install(new SharedModule());
    bind(BeanFactory.class).in(Singleton.class);
    bind(DraftSocketHandler.class).asEagerSingleton();
    bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);
    bind(new TypeLiteral<AsyncDataProvider<Player>>() {})
        .to(UnclaimedPlayerDataProvider.class).in(Singleton.class);
  }
}