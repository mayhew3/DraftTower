package com.mayhew3.drafttower.client.myroster;

import com.google.common.base.Joiner;
import com.google.gwt.user.cellview.client.TextColumn;
import com.mayhew3.drafttower.client.myroster.MyRosterPresenter.PickAndPosition;

import javax.inject.Inject;

/**
 * Table displaying user's roster so far.
 */
public class MyRosterTable extends DebugIdCellTable<PickAndPosition> {

  @Inject
  public MyRosterTable(MyRosterPresenter presenter) {
    setPageSize(Integer.MAX_VALUE);
    addColumn(new TextColumn<PickAndPosition>() {
      @Override
      public String getValue(PickAndPosition pickAndPosition) {
        return pickAndPosition.getPosition().getShortName();
      }
    }, "Pos");
    addColumn(new TextColumn<PickAndPosition>() {
      @Override
      public String getValue(PickAndPosition pickAndPosition) {
        return pickAndPosition.getPick() == null ? ""
            : pickAndPosition.getPick().getPlayerName();
      }
    }, "Player");
    addColumn(new TextColumn<PickAndPosition>() {
      @Override
      public String getValue(PickAndPosition pickAndPosition) {
        return pickAndPosition.getPick() == null ? ""
            : Joiner.on(", ").join(pickAndPosition.getPick().getEligibilities());
      }
    }, "Elig");

    presenter.addDataDisplay(this);
  }
}