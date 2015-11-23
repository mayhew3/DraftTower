package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.DisconnectClientEvent.Handler;

/**
 * Event fired when a commissioner forces a client to disconnect.
 */
public class DisconnectClientEvent extends GwtEvent<Handler> {

  public interface Handler extends EventHandler {
    void onDisconnectClient(DisconnectClientEvent event);
  }

  public static final Type<Handler> TYPE = new Type<>();

  private final int team;

  public DisconnectClientEvent(int team) {
    this.team = team;
  }

  public int getTeam() {
    return team;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onDisconnectClient(this);
  }
}