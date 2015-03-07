package com.mayhew3.drafttower.client.depthcharts;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.ListDataProvider;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.RosterUtil;

import javax.inject.Inject;
import java.util.*;

/**
 * Presenter for depth charts table.
 */
public class DepthChartsPresenter extends ListDataProvider<Integer> implements
    DraftStatusChangedEvent.Handler {

  private final RosterUtil rosterUtil;
  private Map<Integer, Multimap<Position, DraftPick>> rosters = new HashMap<>();

  @Inject
  public DepthChartsPresenter(EventBus eventBus,
      RosterUtil rosterUtil) {
    this.rosterUtil = rosterUtil;
    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    rosters.clear();
    List<Integer> teams = new ArrayList<>();
    List<DraftPick> picks = event.getStatus().getDraftStatus().getPicks();
    for (DraftPick draftPick : picks) {
      final int team = draftPick.getTeam();
      if (teams.contains(team)) {
        break;
      }
      teams.add(team);
      rosters.put(team, rosterUtil.constructRoster(
          Lists.newArrayList(Iterables.filter(picks, new Predicate<DraftPick>() {
            @Override
            public boolean apply(DraftPick input) {
              return input.getTeam() == team;
            }
          }))));
    }
    setList(teams);
  }

  public Collection<DraftPick> getPicks(Integer team, Position position) {
    if (rosters.containsKey(team)) {
      return rosters.get(team).get(position);
    } else {
      return ImmutableList.of();
    }
  }
}