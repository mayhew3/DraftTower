package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.CopyAllPlayerRanksEvent.Handler;
import com.mayhew3.drafttower.shared.TableSpec;

/**
 * Event fired when a user wants to copy the current ordering to custom rankings.
 */
public class CopyAllPlayerRanksEvent extends GwtEvent<Handler> {


  public static interface Handler extends EventHandler {
    void onCopyAllPlayerRanks(CopyAllPlayerRanksEvent event);
  }

  public static final Type<Handler> TYPE = new Type<>();

  private final TableSpec tableSpec;

  public CopyAllPlayerRanksEvent(TableSpec tableSpec) {
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
    handler.onCopyAllPlayerRanks(this);
  }
}