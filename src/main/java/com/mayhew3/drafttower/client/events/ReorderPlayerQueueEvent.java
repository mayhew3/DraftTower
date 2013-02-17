package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.ReorderPlayerQueueEvent.Handler;

/**
 * Event fired when a player is moved in the queue.
 */
public class ReorderPlayerQueueEvent extends GwtEvent<Handler> {

  public static interface Handler extends EventHandler {
    void onQueueReordered(ReorderPlayerQueueEvent event);
  }

  public static final Type<Handler> TYPE = new Type<Handler>();

  private final long playerId;
  private final int newPosition;

  public ReorderPlayerQueueEvent(long playerId, int newPosition) {
    this.playerId = playerId;
    this.newPosition = newPosition;
  }

  public long getPlayerId() {
    return playerId;
  }

  public int getNewPosition() {
    return newPosition;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onQueueReordered(this);
  }
}