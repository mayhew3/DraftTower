package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.PlayerSelectedEvent.Handler;

/**
 * Event fired when a player is selected from the main table.
 */
public class PlayerSelectedEvent extends GwtEvent<Handler> {

  public static interface Handler extends EventHandler {
    void onPlayerSelected(PlayerSelectedEvent event);
  }

  public static final Type<Handler> TYPE = new Type<Handler>();

  private final Long playerId;
  private final String playerName;

  public PlayerSelectedEvent(long playerId, String playerName) {
    this.playerId = playerId;
    this.playerName = playerName;
  }

  public Long getPlayerId() {
    return playerId;
  }

  public String getPlayerName() {
    return playerName;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onPlayerSelected(this);
  }
}