package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.LoginEvent.Handler;

/**
 * Event fired on successful login.
 */
public class LoginEvent extends GwtEvent<Handler> {

  public static interface Handler extends EventHandler {
    void onLogin(LoginEvent event);
  }

  public static final Type<Handler> TYPE = new Type<Handler>();

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onLogin(this);
  }
}