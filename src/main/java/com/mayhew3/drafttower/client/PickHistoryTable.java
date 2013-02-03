package com.mayhew3.drafttower.client;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.shared.DraftPick;

/**
 * Table displaying picks so far.
 */
public class PickHistoryTable extends CellTable<DraftPick> implements
    DraftStatusChangedEvent.Handler {

  private ListDataProvider<DraftPick> pickProvider;

  @Inject
  public PickHistoryTable(EventBus eventBus) {
    addColumn(new TextColumn<DraftPick>() {
      @Override
      public String getValue(DraftPick pick) {
        return Integer.toString(pickProvider.getList().size()
            - pickProvider.getList().indexOf(pick));
      }
    }, "Pick");
    addColumn(new TextColumn<DraftPick>() {
      @Override
      public String getValue(DraftPick pick) {
        return pick.getTeamName();
      }
    }, "Team");
    addColumn(new TextColumn<DraftPick>() {
      @Override
      public String getValue(DraftPick pick) {
        return pick.getPlayerName();
      }
    }, "Player");

    pickProvider = new ListDataProvider<DraftPick>();
    pickProvider.addDataDisplay(this);

    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    pickProvider.setList(Lists.reverse(event.getStatus().getPicks()));
  }
}