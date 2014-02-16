package com.mayhew3.drafttower.client;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.RosterUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Tracks open positions for the user's team.
 */
public class OpenPositions implements DraftStatusChangedEvent.Handler {

  private final TeamsInfo teamsInfo;

  private Set<Position> openPositions = new HashSet<>();

  @Inject
  public OpenPositions(TeamsInfo teamsInfo,
      EventBus eventBus) {
    this.teamsInfo = teamsInfo;
    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    openPositions = RosterUtil.getOpenPositions(
        Lists.newArrayList(Iterables.filter(event.getStatus().getPicks(),
            new Predicate<DraftPick>() {
              @Override
              public boolean apply(DraftPick input) {
                return input.getTeam() == teamsInfo.getTeam();
              }
            })));
  }

  public Set<Position> get() {
    return openPositions;
  }
}