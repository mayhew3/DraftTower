package com.mayhew3.drafttower.client.filledpositions;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.RosterUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Data to display in the {@link FilledPositionsChart}.
 */
public class FilledPositionsCounts {
  private final Map<Position, Integer> counts;
  private final Map<Position, Integer> lastRoundCounts;

  public FilledPositionsCounts(final List<DraftPick> picks, int numTeams, RosterUtil rosterUtil) {
    ImmutableListMultimap<Integer, DraftPick> picksPerTeam =
        Multimaps.index(picks, new Function<DraftPick, Integer>() {
          @Override
          public Integer apply(DraftPick input) {
            return input.getTeam();
          }
        });
    counts = Maps.newEnumMap(Position.class);
    lastRoundCounts = Maps.newEnumMap(Position.class);
    for (Position position : FilledPositionsPresenter.positions) {
      counts.put(position, 0);
      lastRoundCounts.put(position, 0);
    }
    for (int i = 1; i <= numTeams; i++) {
      Multimap<Position, DraftPick> roster =
          rosterUtil.constructRoster(Lists.newArrayList(picksPerTeam.get(i)));
      for (Position position : FilledPositionsPresenter.positions) {
        Collection<DraftPick> positionPicks = roster.get(position);
        counts.put(position, counts.get(position) + positionPicks.size());
        lastRoundCounts.put(position, lastRoundCounts.get(position) +
            Iterables.size(Iterables.filter(positionPicks,
                new Predicate<DraftPick>() {
                  @Override
                  public boolean apply(DraftPick pick) {
                    return picks.indexOf(pick) > picks.size() - 10;
                  }
                })));
      }
    }
  }

  public int getPositionCount(Position position) {
    return counts.get(position);
  }

  public int getPositionLastRoundCount(Position position) {
    return lastRoundCounts.get(position);
  }
}