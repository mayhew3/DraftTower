package com.mayhew3.drafttower.client;

import com.mayhew3.drafttower.client.DraftTowerClientTestSafeModule.EagerSingletons;
import dagger.Component;

import javax.inject.Singleton;

/**
 * Dependency injection component for production client-side dependencies.
 */
@Component(modules = {
    DraftTowerClientTestSafeModule.class,
    DraftTowerClientLiveModule.class,
})
@Singleton
public interface DraftTowerClientComponent {
  MainPageWidget mainPageWidget();
  EagerSingletons injectEager();
}