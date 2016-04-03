package com.mayhew3.drafttower.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.mayhew3.drafttower.client.GinBindingAnnotations.CurrentTime;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.SharedModule;
import dagger.Module;
import dagger.Provides;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Dependency injection module for test-safe client-side dependencies.
 */
@Module(includes = SharedModule.class)
public class DraftTowerClientTestSafeModule {

  public static class EagerSingletons {
    @Inject DraftSocketHandler draftSocketHandler;
    @Inject public EagerSingletons() {}
  }

  @Provides @CurrentTime
  public static Double getCurrentTime() {
    return com.google.gwt.core.client.Duration.currentTimeMillis();
  }

  @Provides @Singleton
  public static EventBus getEventBus() {
    return new SimpleEventBus();
  }

  @Provides @Singleton
  public static BeanFactory getBeanFactory() {
    return GWT.create(BeanFactory.class);
  }
}