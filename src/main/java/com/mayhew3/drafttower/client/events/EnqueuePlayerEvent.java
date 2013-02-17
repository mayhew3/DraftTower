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
  private final Integer position;

  public EnqueuePlayerEvent(long playerId, Integer position) {
    this.playerId = playerId;
    this.position = position;
  }

  public long getPlayerId() {
    return playerId;
  }

  public Integer getPosition() {
    return position;
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