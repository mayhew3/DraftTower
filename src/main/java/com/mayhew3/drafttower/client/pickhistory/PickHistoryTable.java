package com.mayhew3.drafttower.client.pickhistory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.pickhistory.PickHistoryPresenter.PickHistoryInfo;

/**
 * Table displaying picks so far.
 */
public class PickHistoryTable extends CellTable<PickHistoryInfo> {

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

  @Inject
  public PickHistoryTable(PickHistoryPresenter presenter) {
    setPageSize(Integer.MAX_VALUE);
    addColumn(new TextColumn<PickHistoryInfo> () {
      @Override
      public String getValue(PickHistoryInfo pick) {
        return pick.getPickNumber();
      }
    }, "Pick");
    addColumn(new TextColumn<PickHistoryInfo>() {
      @Override
      public String getValue(PickHistoryInfo pick) {
        return pick.getTeamName();
      }
    }, "Team");
    addColumn(new TextColumn<PickHistoryInfo>() {
      @Override
      public String getValue(PickHistoryInfo pick) {
        return pick.getPlayerName();
      }
    }, "Player");

    presenter.addDataDisplay(this);

    setRowStyles(new RowStyles<PickHistoryInfo>() {
      @Override
      public String getStyleNames(PickHistoryInfo row, int rowIndex) {
        return row.isKeeper() ? CSS.keeper() : null;
      }
    });

  }
}