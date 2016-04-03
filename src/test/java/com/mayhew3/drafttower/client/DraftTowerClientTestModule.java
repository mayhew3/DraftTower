package com.mayhew3.drafttower.client;

import com.mayhew3.drafttower.client.GinBindingAnnotations.PlayerPopupUrlPrefix;
import com.mayhew3.drafttower.client.GinBindingAnnotations.TtsUrlPrefix;
import com.mayhew3.drafttower.client.graphs.BarGraphsApi;
import com.mayhew3.drafttower.client.graphs.TestBarGraphsApi;
import com.mayhew3.drafttower.client.serverrpc.ServerRpc;
import com.mayhew3.drafttower.client.websocket.Websocket;
import com.mayhew3.drafttower.server.BindingAnnotations.DraftTimerListenerList;
import com.mayhew3.drafttower.server.*;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Dependency injection module for test client-side dependencies.
 */
@Module(includes = {
    ServerTestSafeModule.class,
    TestServerModule.class,
    DraftTowerClientTestSafeModule.class,
})
public class DraftTowerClientTestModule {

  @Provides @DraftTimerListenerList
  public static List<DraftTimer.Listener> getDraftTimerListenerList() {
    return new ArrayList<>();
  }

  @Provides @TtsUrlPrefix
  public static String getTtsUrlPrefix() {
    return "#";
  }

  @Provides @PlayerPopupUrlPrefix
  public static String getPlayerPopupUrlPrefix() {
    return "#";
  }

  @Provides
  public static BarGraphsApi getBarGraphsApi(TestBarGraphsApi impl) {
    return impl;
  }

  @Provides
  public static DraftTowerWebSocket getDraftTowerWebSocket(TestDraftTowerWebSocket impl) {
    return impl;
  }

  @Provides
  public static Lock getLock(ClientTestLock impl) {
    return impl;
  }

  @Provides @Singleton
  public static PlayerDataProvider getPlayerDataProvider(TestPlayerDataProvider impl) {
    return impl;
  }

  @Provides @Singleton
  public static SchedulerWrapper getSchedulerWrapper(TestScheduler impl) {
    return impl;
  }

  @Provides @Singleton
  public static ServerRpc getServerRpc(TestServerRpc impl) {
    return impl;
  }

  @Provides
  public static TokenGenerator getTokenGenerator(ClientTestTokenGenerator impl) {
    return impl;
  }

  @Provides
  public static Websocket getWebsocket(TestDraftTowerWebSocket impl) {
    return impl;
  }
}