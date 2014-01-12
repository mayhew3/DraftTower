package com.mayhew3.drafttower.client;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;

/**
 * Table displaying picks so far.
 */
public class PickHistoryTable extends CellTable<DraftPick> implements
    DraftStatusChangedEvent.Handler {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String keeper();
    }

    @Source("PickHistoryTable.css")
    Css css();
  }

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  private ListDataProvider<DraftPick> pickProvider;

  @Inject
  public PickHistoryTable(final TeamsInfo teamsInfo,
      final @NumTeams int numTeams,
      EventBus eventBus) {
    setPageSize(Integer.MAX_VALUE);
    addColumn(new TextColumn<DraftPick>() {
      @Override
      public String getValue(DraftPick pick) {
        int overallPick = pickProvider.getList().size()
            - pickProvider.getList().indexOf(pick);
        int round = (overallPick - 1) / numTeams + 1;
        int pickNum = ((overallPick-1) % numTeams) + 1;
        return round + ":" + pickNum;
      }
    }, "Pick");
    addColumn(new TextColumn<DraftPick>() {
      @Override
      public String getValue(DraftPick pick) {
        return teamsInfo.getShortTeamName(pick.getTeam());
      }
    }, "Team");
    addColumn(new TextColumn<DraftPick>() {
      @Override
      public String getValue(DraftPick pick) {
        return pick.getPlayerName();
      }
    }, "Player");

    pickProvider = new ListDataProvider<>();
    pickProvider.addDataDisplay(this);

    setRowStyles(new RowStyles<DraftPick>() {
      @Override
      public String getStyleNames(DraftPick row, int rowIndex) {
        return row.isKeeper() ? CSS.keeper() : null;
      }
    });

    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    pickProvider.setList(Lists.reverse(event.getStatus().getPicks()));
  }
}