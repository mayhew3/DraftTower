package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.PlayerSelectedEvent.Handler;
import com.mayhew3.drafttower.shared.Player;

/**
 * Event fired when a player is selected from the main table.
 */
public class PlayerSelectedEvent extends GwtEvent<Handler> {

  public static interface Handler extends EventHandler {
    void onPlayerSelected(PlayerSelectedEvent event);
  }

  public static final Type<Handler> TYPE = new Type<Handler>();

  private final Player player;

  public PlayerSelectedEvent(Player player) {
    this.player = player;
  }

  public Player getPlayer() {
    return player;
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