package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent.Handler;
import com.mayhew3.drafttower.shared.ClientDraftStatus;

/**
 * Event fired when a draft status update message is received.
 */
public class DraftStatusChangedEvent extends GwtEvent<Handler> {

  public static interface Handler extends EventHandler {
    void onDraftStatusChanged(DraftStatusChangedEvent event);
  }

  public static final Type<Handler> TYPE = new Type<>();

  private final ClientDraftStatus status;

  public DraftStatusChangedEvent(ClientDraftStatus status) {
    this.status = status;
  }

  public ClientDraftStatus getStatus() {
    return status;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onDraftStatusChanged(this);
  }
}