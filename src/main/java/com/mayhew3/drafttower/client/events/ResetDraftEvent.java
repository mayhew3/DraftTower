package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.ResetDraftEvent.Handler;

/**
 * Event fired on "reset draft" click.
 */
public class ResetDraftEvent extends GwtEvent<Handler> {

  public interface Handler extends EventHandler {
    void onResetDraft(ResetDraftEvent event);
  }

  public static final Type<Handler> TYPE = new Type<>();

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onResetDraft(this);
  }
}