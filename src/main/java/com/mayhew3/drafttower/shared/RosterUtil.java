package com.mayhew3.drafttower.shared;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import java.util.List;

import static com.mayhew3.drafttower.shared.Position.*;

/**
 * Roster construction methods.
 */
public class RosterUtil {

  private static List<Position> POSITIONS = Lists.newArrayList(
      C, FB, SB, TB, SS, OF, OF, OF, DH,
      P, P, P, P, P, P, P);

  public static Multimap<Position, DraftPick> constructRoster(List<DraftPick> picks) {
    return doConstructRoster(picks, Lists.newArrayList(POSITIONS),
        ArrayListMultimap.<Position, DraftPick>create());
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