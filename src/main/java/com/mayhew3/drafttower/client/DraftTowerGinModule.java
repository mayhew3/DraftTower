package com.mayhew3.drafttower.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.user.client.Window;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.ServletEndpoints;
import com.mayhew3.drafttower.shared.SharedModule;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Dependency injection module for client-side dependencies.
 */
public class DraftTowerGinModule extends AbstractGinModule {

  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  public static @interface LoginUrl {}

  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  public static @interface DraftSocketUrl {}

  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  public static @interface UnclaimedPlayerInfoUrl {}

  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  public static @interface ChangePlayerRankUrl {}

  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  public static @interface SetAutoPickTableSpecUrl {}

  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  public static @interface QueuesUrl {}

  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  public static @interface GraphsUrl {}

  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  public static @interface QueueAreaTop {}

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

  @Provides @SetAutoPickTableSpecUrl
  public String getSetAutoPickTableSpecUrl() {
    return Window.Location.createUrlBuilder()
        .setPath(Window.Location.getPath()
            + ServletEndpoints.SET_AUTOPICK_TABLE_SPEC_ENDPOINT)
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
  }
}