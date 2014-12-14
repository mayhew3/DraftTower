package com.mayhew3.drafttower.client.myroster;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.ListDataProvider;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.client.myroster.MyRosterPresenter.PickAndPosition;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.RosterUtil;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Presenter for "my roster" table.
 */
public class MyRosterPresenter extends ListDataProvider<PickAndPosition> implements
    DraftStatusChangedEvent.Handler {

  static class PickAndPosition {
    private final DraftPick pick;
    private final Position position;

    @VisibleForTesting
    PickAndPosition(DraftPick pick, Position position) {
      this.pick = pick;
      this.position = position;
    }

    DraftPick getPick() {
      return pick;
    }

    Position getPosition() {
      return position;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      PickAndPosition that = (PickAndPosition) o;

      if (pick != null ? !pick.equals(that.pick) : that.pick != null) return false;
      if (position != that.position) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = pick != null ? pick.hashCode() : 0;
      result = 31 * result + position.hashCode();
      return result;
    }
  }

  private final TeamsInfo teamsInfo;
  private final RosterUtil rosterUtil;

  @Inject
  public MyRosterPresenter(TeamsInfo teamsInfo,
      EventBus eventBus,
      RosterUtil rosterUtil) {
    this.teamsInfo = teamsInfo;
    this.rosterUtil = rosterUtil;

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
    Multimap<Position,DraftPick> roster = rosterUtil.constructRoster(myPicks);
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
    setList(picksAndPositions);
  }
}