package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.ChangePlayerRankEvent.Handler;

/**
 * Event fired when a player's rank is changed.
 */
public class ChangePlayerRankEvent extends GwtEvent<Handler> {

  public interface Handler extends EventHandler {
    void onChangePlayerRank(ChangePlayerRankEvent event);
  }

  public static final Type<Handler> TYPE = new Type<>();

  private final long playerId;
  private final int newRank;
  private final int prevRank;

  public ChangePlayerRankEvent(long playerId, int newRank, int prevRank) {
    this.playerId = playerId;
    this.newRank = newRank;
    this.prevRank = prevRank;
  }

  public long getPlayerId() {
    return playerId;
  }

  public int getNewRank() {
    return newRank;
  }

  public int getPrevRank() {
    return prevRank;
  }


  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onChangePlayerRank(this);
  }
}