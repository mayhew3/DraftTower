package com.mayhew3.drafttower.client;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.user.client.Window;
import com.google.inject.Provides;
import com.mayhew3.drafttower.client.GinBindingAnnotations.*;
import com.mayhew3.drafttower.client.graphs.BarGraphsApi;
import com.mayhew3.drafttower.client.graphs.LiveBarGraphsApi;
import com.mayhew3.drafttower.client.serverrpc.ServerRpc;
import com.mayhew3.drafttower.client.serverrpc.ServerRpcImpl;
import com.mayhew3.drafttower.client.websocket.Websocket;
import com.mayhew3.drafttower.client.websocket.WebsocketImpl;
import com.mayhew3.drafttower.shared.CurrentTimeProvider;
import com.mayhew3.drafttower.shared.CurrentTimeProvider.CurrentTimeProviderImpl;
import com.mayhew3.drafttower.shared.ServletEndpoints;

import javax.inject.Singleton;

/**
 * Dependency injection module for live client-side dependencies.
 */
public class DraftTowerLiveGinModule extends AbstractGinModule {

  @Provides @LoginUrl
  public String getLoginUrl() {
    return Window.Location.createUrlBuilder()
        .setPath(Window.Location.getPath()
            + ServletEndpoints.LOGIN_ENDPOINT)
        .buildString();
  }

  @Provides @DraftSocketUrl
  public String getDraftSocketUrl() {
    return Window.Location.createUrlBuilder()
        .setProtocol("ws")
        .setPath(Window.Location.getPath()
            + ServletEndpoints.DRAFT_SOCKET_ENDPOINT)
        .buildString();
  }

  @Provides @UnclaimedPlayerInfoUrl
  public String getUnclaimedPlayerInfoUrl() {
    return Window.Location.createUrlBuilder()
        .setPath(Window.Location.getPath()
            + ServletEndpoints.UNCLAIMED_PLAYERS_ENDPOINT)
        .buildString();
  }

  @Provides @ChangePlayerRankUrl
  public String getChangePlayerRankUrl() {
    return Window.Location.createUrlBuilder()
        .setPath(Window.Location.getPath()
            + ServletEndpoints.CHANGE_PLAYER_RANK_ENDPOINT)
        .buildString();
  }

  @Provides @CopyPlayerRanksUrl
  public String getUpdateAllPlayerRanksUrl() {
    return Window.Location.createUrlBuilder()
        .setPath(Window.Location.getPath()
            + ServletEndpoints.COPY_ALL_PLAYER_RANKS_ENDPOINT)
        .buildString();
  }

  @Provides @SetAutoPickWizardUrl
  public String getSetAutoPickWizardTableUrl() {
    return Window.Location.createUrlBuilder()
        .setPath(Window.Location.getPath()
            + ServletEndpoints.SET_AUTOPICK_WIZARD_ENDPOINT)
        .buildString();
  }

  @Provides @QueuesUrl
  public String getQueuesUrl() {
    return Window.Location.createUrlBuilder()
        .setPath(Window.Location.getPath()
            + ServletEndpoints.QUEUE_ENDPOINT)
        .buildString();
  }

  @Provides @GraphsUrl
  public String getGraphsUrl() {
    return Window.Location.createUrlBuilder()
        .setPath(Window.Location.getPath()
            + ServletEndpoints.GRAPHS_ENDPOINT)
        .buildString();
  }

  @Provides @TtsUrlPrefix
  public String getTtsUrlPrefix() {
    return "http://translate.google.com/translate_tts?tl=en&q=";
  }

  @Provides @PlayerPopupUrlPrefix
  public String getPlayerPopupUrlPrefix() {
    return "http://uncharted.baseball.cbssports.com/players/playerpage/snippet/";
  }

  @Override
  protected void configure() {
    bind(BarGraphsApi.class).to(LiveBarGraphsApi.class);
    bind(CurrentTimeProvider.class).to(CurrentTimeProviderImpl.class);
    bind(SchedulerWrapper.class).to(LiveScheduler.class);
    bind(ServerRpc.class).to(ServerRpcImpl.class).in(Singleton.class);
    bind(Websocket.class).to(WebsocketImpl.class);
  }
}