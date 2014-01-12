package com.mayhew3.drafttower.client;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.MyRosterTable.PickAndPosition;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.RosterUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Table displaying user's roster so far.
 */
public class MyRosterTable extends CellTable<PickAndPosition> implements
    DraftStatusChangedEvent.Handler {

  class PickAndPosition {
    private DraftPick pick;
    private Position position;

    private PickAndPosition(DraftPick pick, Position position) {
      this.pick = pick;
      this.position = position;
    }
  }

  private final TeamsInfo teamsInfo;

  private ListDataProvider<PickAndPosition> rosterProvider;

  @Inject
  public MyRosterTable(TeamsInfo teamsInfo,
      EventBus eventBus) {
    this.teamsInfo = teamsInfo;
    setPageSize(Integer.MAX_VALUE);
    addColumn(new TextColumn<PickAndPosition>() {
      @Override
      public String getValue(PickAndPosition pickAndPosition) {
        return pickAndPosition.position.getShortName();
      }
    }, "Pos");
    addColumn(new TextColumn<PickAndPosition>() {
      @Override
      public String getValue(PickAndPosition pickAndPosition) {
        return pickAndPosition.pick == null ? ""
            : pickAndPosition.pick.getPlayerName();
      }
    }, "Player");
    addColumn(new TextColumn<PickAndPosition>() {
      @Override
      public String getValue(PickAndPosition pickAndPosition) {
        return pickAndPosition.pick == null ? ""
            : Joiner.on(", ").join(pickAndPosition.pick.getEligibilities());
      }
    }, "Elig");

    rosterProvider = new ListDataProvider<>();
    rosterProvider.addDataDisplay(this);

    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    List<DraftPick> myPicks = Lists.newArrayList(
        Iterables.filter(event.getStatus().getPicks(),
            new Predicate<DraftPick>() {
              @Override
              public boolean apply(DraftPick input) {
                return input.getTeam() == teamsInfo.getTeam();
              }
            }));
    Multimap<Position,DraftPick> roster = RosterUtil.constructRoster(myPicks);
    List<PickAndPosition> picksAndPositions = new ArrayList<>();
    for (Entry<Position, Integer> position : RosterUtil.POSITIONS_AND_COUNTS.entrySet()) {
      int rowsCreated = 0;
      if (roster.containsKey(position.getKey())) {
        for (DraftPick pick : roster.get(position.getKey())) {
          picksAndPositions.add(new PickAndPosition(pick, position.getKey()));
          myPicks.remove(pick);
          rowsCreated++;
        }
      }
      while (rowsCreated < position.getValue()) {
        picksAndPositions.add(new PickAndPosition(null, position.getKey()));
        rowsCreated++;
      }
    }
    rosterProvider.setList(picksAndPositions);
  }
}