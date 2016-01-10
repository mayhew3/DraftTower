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

import java.util.ArrayList;
import java.util.EnumSet;

/**
 * Tracks open positions for the user's team.
 */
@Singleton
public class OpenPositions {

  private final TeamsInfo teamsInfo;

  private final RosterUtil rosterUtil;
  private final EnumSet<Position> openPositions = EnumSet.allOf(Position.class);
  private final EnumSet<Position> openPositionsOptimal = EnumSet.allOf(Position.class);

  @Inject
  public OpenPositions(TeamsInfo teamsInfo,
      RosterUtil rosterUtil) {
    this.teamsInfo = teamsInfo;
    this.rosterUtil = rosterUtil;
  }

  public void onDraftStatusChanged(DraftStatus status) {
    openPositions.clear();
    openPositionsOptimal.clear();
    ArrayList<DraftPick> myPicks = Lists.newArrayList(Iterables.filter(status.getPicks(),
        new Predicate<DraftPick>() {
          @Override
          public boolean apply(DraftPick input) {
            return input.getTeam() == teamsInfo.getTeam();
          }
        }));
    openPositions.addAll(rosterUtil.getOpenPositions(myPicks));
    openPositionsOptimal.addAll(rosterUtil.getOptimalOpenPositions(myPicks));
  }

  public EnumSet<Position> get() {
    return openPositions;
  }

  public EnumSet<Position> getOptimal() {
    return openPositionsOptimal;
  }
}