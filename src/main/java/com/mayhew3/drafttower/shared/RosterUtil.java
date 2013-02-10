package com.mayhew3.drafttower.shared;

import com.google.common.base.Predicate;
import com.google.common.collect.*;

import java.util.List;
import java.util.Map;

import static com.mayhew3.drafttower.shared.Position.*;

/**
 * Roster construction methods.
 */
public class RosterUtil {

  public static Map<Position, Integer> POSITIONS_AND_COUNTS = ImmutableMap.<Position, Integer>builder()
      .put(C, 1)
      .put(FB, 1)
      .put(SB, 1)
      .put(TB, 1)
      .put(SS, 1)
      .put(OF, 3)
      .put(DH, 1)
      .put(P, 7)
      .put(RS, 6)
      .build();

  private static List<Position> POSITIONS = Lists.newArrayList(
      C, FB, SB, TB, SS, OF, OF, OF, DH,
      P, P, P, P, P, P, P);

  public static Multimap<Position, DraftPick> constructRoster(List<DraftPick> picks) {
    final Multimap<Position, DraftPick> roster = doConstructRoster(picks, Lists.newArrayList(POSITIONS),
        ArrayListMultimap.<Position, DraftPick>create());
    Iterable<DraftPick> reserves = Iterables.filter(picks, new Predicate<DraftPick>() {
      @Override
      public boolean apply(DraftPick input) {
        return !roster.containsValue(input);
      }
    });
    for (DraftPick reserve : reserves) {
      roster.put(RS, reserve);
    }
    return roster;
  }

  private static Multimap<Position, DraftPick> doConstructRoster(
      List<DraftPick> picks, List<Position> positions, Multimap<Position, DraftPick> roster) {
    if (picks.isEmpty()) {
      return roster;
    } else {
      DraftPick pick = picks.remove(0);
      Multimap<Position, DraftPick> bestRoster = null;
      for (String positionStr : pick.getEligibilities()) {
        Position position = Position.fromShortName(positionStr);
        if (position == SP || position == RP) {
          position = P;
        }
        if (!positions.contains(position)
            && (!positions.contains(DH) || position == P)) {
          continue;
        }
        Multimap<Position, DraftPick> expandedRoster = ArrayListMultimap.create(roster);
        if (!positions.contains(position)) {
          position = DH;
        }
        expandedRoster.put(position, pick);
        positions.remove(position);
        Multimap<Position, DraftPick> result = doConstructRoster(picks, positions, expandedRoster);
        if (bestRoster == null
            || result.size() > bestRoster.size()
            || (result.size() == bestRoster.size() && result.get(DH).isEmpty() && !bestRoster.get(DH).isEmpty())) {
          bestRoster = result;
        }
        positions.add(position);
      }
      if (bestRoster == null) {
        // No room for this player.
        bestRoster = doConstructRoster(picks, positions, roster);
      }
      picks.add(0, pick);
      return bestRoster;
    }
  }

}