package com.mayhew3.drafttower.client;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.mayhew3.drafttower.client.websocket.Websocket;
import com.mayhew3.drafttower.server.BindingAnnotations.DraftTimerListenerList;
import com.mayhew3.drafttower.server.*;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Dependency injection module for test client-side dependencies.
 */
public class DraftTowerTestGinModule extends AbstractGinModule {

  @Provides @DraftTimerListenerList
  public List<DraftTimer.Listener> getDraftTimerListenerList() {
    return new ArrayList<>();
  }

  @Override
  protected void configure() {
    install(new ServerTestSafeModule());
    install(new TestServerModule());
    install(new DraftTowerTestSafeGinModule());
    bind(Lock.class).to(ClientTestLock.class);
    bind(ServerRpc.class).to(TestServerRpc.class).in(Singleton.class);
    bind(TokenGenerator.class).to(ClientTestTokenGenerator.class);
    bind(Websocket.class).to(TestDraftTowerWebSocket.class).in(Singleton.class);
  }
}