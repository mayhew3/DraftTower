package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.ToggleFavoritePlayerEvent.Handler;

/**
 * Event fired when a favorite player is toggled.
 */
public class ToggleFavoritePlayerEvent extends GwtEvent<Handler> {

  public interface Handler extends EventHandler {
    void onToggleFavoritePlayer(ToggleFavoritePlayerEvent event);
  }

  public static final Type<Handler> TYPE = new Type<>();

  private final long playerId;
  private boolean add;

  public ToggleFavoritePlayerEvent(long playerId, boolean add) {
    this.playerId = playerId;
    this.add = add;
  }

  public long getPlayerId() {
    return playerId;
  }

  public boolean isAdd() {
    return add;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onToggleFavoritePlayer(this);
  }
}