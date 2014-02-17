package com.mayhew3.drafttower.client;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.RosterUtil;

import java.util.EnumSet;

/**
 * Tracks open positions for the user's team.
 */
@Singleton
public class OpenPositions {

  private final TeamsInfo teamsInfo;

  private EnumSet<Position> openPositions = EnumSet.allOf(Position.class);

  @Inject
  public OpenPositions(TeamsInfo teamsInfo) {
    this.teamsInfo = teamsInfo;
  }

  public void onDraftStatusChanged(DraftStatus status) {
    openPositions.clear();
    openPositions.addAll(RosterUtil.getOpenPositions(
        Lists.newArrayList(Iterables.filter(status.getPicks(),
            new Predicate<DraftPick>() {
              @Override
              public boolean apply(DraftPick input) {
                return input.getTeam() == teamsInfo.getTeam();
              }
            }))));
  }

  public EnumSet<Position> get() {
    return openPositions;
  }
}