package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.PickPlayerEvent.Handler;

/**
 * Event fired when the user picks a player.
 */
public class PickPlayerEvent extends GwtEvent<Handler> {

  public interface Handler extends EventHandler {
    void onPlayerPicked(PickPlayerEvent event);
  }

  public static final Type<Handler> TYPE = new Type<>();

  private final long playerId;

  public PickPlayerEvent(long playerId) {
    this.playerId = playerId;
  }

  public long getPlayerId() {
    return playerId;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onPlayerPicked(this);
  }
}