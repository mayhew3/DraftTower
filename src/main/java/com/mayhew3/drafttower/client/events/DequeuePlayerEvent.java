package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.DequeuePlayerEvent.Handler;

/**
 * Event fired when the user dequeues a player.
 */
public class DequeuePlayerEvent extends GwtEvent<Handler> {

  public static interface Handler extends EventHandler {
    void onPlayerDequeued(DequeuePlayerEvent event);
  }

  public static final Type<Handler> TYPE = new Type<>();

  private final long playerId;

  public DequeuePlayerEvent(long playerId) {
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
    handler.onPlayerDequeued(this);
  }
}