package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.EnqueuePlayerEvent.Handler;

/**
 * Event fired when the user enqueues a player.
 */
public class EnqueuePlayerEvent extends GwtEvent<Handler> {

  public static interface Handler extends EventHandler {
    void onPlayerEnqueued(EnqueuePlayerEvent event);
  }

  public static final Type<Handler> TYPE = new Type<Handler>();

  private final long playerId;

  public EnqueuePlayerEvent(long playerId) {
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
    handler.onPlayerEnqueued(this);
  }
}