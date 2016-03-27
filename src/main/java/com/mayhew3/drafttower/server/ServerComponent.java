package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.server.ServerTestSafeModule.EagerSingletons;
import com.mayhew3.drafttower.shared.SharedModule;
import dagger.Component;

import javax.inject.Singleton;

/**
 * Server-side dependency component.
 */
@Component(modules = {
    SharedModule.class,
    ServerTestSafeModule.class,
    ServerProductionModule.class,
    ServletModule.class,
})
@Singleton
public interface ServerComponent {
  EagerSingletons injectEager();

  ServletMapProvider servletMapProvider();

  LoginServlet loginServlet();
  DraftTowerWebSocketServlet webSocketServlet();
  UnclaimedPlayerLookupServlet unclaimedPlayerLookupServlet();
  AddOrRemoveFavoritePlayerServlet addOrRemoveFavoritePlayerServlet();
  ChangePlayerRankServlet changePlayerRankServlet();
  CopyAllPlayerRanksServlet copyAllPlayerRanksServlet();
  SetAutoPickWizardServlet setAutoPickWizardServlet();
  SetCloserLimitServlet setCloserLimitServlet();
  QueueServlet queueServlet();
  GraphsServlet graphsServlet();
}