package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.SetAutoPickTableSpecEvent.Handler;
import com.mayhew3.drafttower.shared.TableSpec;

/**
 * Event fired when the user picks a player.
 */
public class SetAutoPickTableSpecEvent extends GwtEvent<Handler> {

  public static interface Handler extends EventHandler {
    void onSetAutoPickTableSpec(SetAutoPickTableSpecEvent event);
  }

  public static final Type<Handler> TYPE = new Type<Handler>();

  private final TableSpec tableSpec;

  public SetAutoPickTableSpecEvent(TableSpec tableSpec) {
    this.tableSpec = tableSpec;
  }

  public TableSpec getTableSpec() {
    return tableSpec;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onSetAutoPickTableSpec(this);
  }
}