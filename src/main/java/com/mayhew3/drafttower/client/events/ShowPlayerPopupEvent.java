package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.ShowPlayerPopupEvent.Handler;
import com.mayhew3.drafttower.shared.Player;

/**
 * Event fired when the user clicks on a player's name.
 */
public class ShowPlayerPopupEvent extends GwtEvent<Handler> {

  public static interface Handler extends EventHandler {
    void showPlayerPopup(ShowPlayerPopupEvent event);
  }

  public static final Type<Handler> TYPE = new Type<>();

  private final Player player;

  public ShowPlayerPopupEvent(Player player) {
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
    handler.showPlayerPopup(this);
  }
}