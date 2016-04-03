package com.mayhew3.drafttower.client;

import com.google.gwt.user.client.Window;
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
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dependency injection module for live client-side dependencies.
 */
@Module
public class DraftTowerClientLiveModule {

  @Provides @LoginUrl
  public static String getLoginUrl() {
    return Window.Location.createUrlBuilder()
        .setPath(Window.Location.getPath()
            + ServletEndpoints.LOGIN_ENDPOINT)
        .buildString();
  }

  @Provides @DraftSocketUrl
  public static String getDraftSocketUrl(final TeamsInfo teamsInfo) {
    return Window.Location.createUrlBuilder()
        .setProtocol("ws")
        .setPort(teamsInfo.getWebSocketPort())
        .setPath(Window.Location.getPath()
            + ServletEndpoints.DRAFT_SOCKET_ENDPOINT)
        .buildString();
  }

  @Provides @UnclaimedPlayerInfoUrl
  public static String getUnclaimedPlayerInfoUrl() {
    return Window.Location.createUrlBuilder()
        .setPath(Window.Location.getPath()
            + ServletEndpoints.UNCLAIMED_PLAYERS_ENDPOINT)
        .buildString();
  }

  @Provides @ChangePlayerRankUrl
  public static String getChangePlayerRankUrl() {
    return Window.Location.createUrlBuilder()
        .setPath(Window.Location.getPath()
            + ServletEndpoints.CHANGE_PLAYER_RANK_ENDPOINT)
        .buildString();
  }

  @Provides @CopyPlayerRanksUrl
  public static String getUpdateAllPlayerRanksUrl() {
    return Window.Location.createUrlBuilder()
        .setPath(Window.Location.getPath()
            + ServletEndpoints.COPY_ALL_PLAYER_RANKS_ENDPOINT)
        .buildString();
  }

  @Provides @SetAutoPickWizardUrl
  public static String getSetAutoPickWizardTableUrl() {
    return Window.Location.createUrlBuilder()
        .setPath(Window.Location.getPath()
            + ServletEndpoints.SET_AUTOPICK_WIZARD_ENDPOINT)
        .buildString();
  }

  @Provides @SetCloserLimitsUrl
  public static String getSetCloserLimitsUrl() {
    return Window.Location.createUrlBuilder()
        .setPath(Window.Location.getPath()
            + ServletEndpoints.SET_CLOSER_LIMITS_ENDPOINT)
        .buildString();
  }

  @Provides @AddOrRemoveFavoriteUrl
  public static String getAddOrRemoveFavoriteUrl() {
    return Window.Location.createUrlBuilder()
        .setPath(Window.Location.getPath()
            + ServletEndpoints.ADD_OR_REMOVE_FAVORITE_ENDPOINT)
        .buildString();
  }

  @Provides @QueuesUrl
  public static String getQueuesUrl() {
    return Window.Location.createUrlBuilder()
        .setPath(Window.Location.getPath()
            + ServletEndpoints.QUEUE_ENDPOINT)
        .buildString();
  }

  @Provides @GraphsUrl
  public static String getGraphsUrl() {
    return Window.Location.createUrlBuilder()
        .setPath(Window.Location.getPath()
            + ServletEndpoints.GRAPHS_ENDPOINT)
        .buildString();
  }

  @Provides @TtsUrlPrefix
  public static String getTtsUrlPrefix() {
    return "http://translate.google.com/translate_tts?tl=en&q=";
  }

  @Provides @PlayerPopupUrlPrefix
  public static String getPlayerPopupUrlPrefix() {
    return "http://uncharted.baseball.cbssports.com/players/playerpage/";
  }

  @Provides
  public static BarGraphsApi getBarGraphsApi(LiveBarGraphsApi impl) {
    return impl;
  }

  @Provides
  public static CurrentTimeProvider getCurrentTimeProvider(CurrentTimeProviderImpl impl) {
    return impl;
  }

  @Provides
  public static SchedulerWrapper getSchedulerWrapper(LiveScheduler impl) {
    return impl;
  }

  @Provides @Singleton
  public static ServerRpc getServerRpc(ServerRpcImpl impl) {
    return impl;
  }

  @Provides
  public static Websocket getWebsocket(WebsocketImpl impl) {
    return impl;
  }
}