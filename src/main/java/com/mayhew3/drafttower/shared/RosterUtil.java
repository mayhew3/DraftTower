package com.mayhew3.drafttower.shared;

import com.google.common.base.Predicate;
import com.google.common.collect.*;

import java.util.*;
import java.util.Map.Entry;

import static com.mayhew3.drafttower.shared.Position.*;

/**
 * Roster construction methods.
 */
public class RosterUtil {

  public static final Map<Position, Integer> POSITIONS_AND_COUNTS = ImmutableMap.<Position, Integer>builder()
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

  private static final List<Position> POSITIONS = Lists.newArrayList(
      C, FB, SB, TB, SS, OF, OF, OF, DH,
      P, P, P, P, P, P, P);

  public static List<String> splitEligibilities(String eligibility) {
    if (eligibility.isEmpty()) {
      return Lists.newArrayList("DH");
    } else {
      String[] eligSplit = eligibility.split(",");
      int indexOfDH = Arrays.binarySearch(eligSplit, "DH");
      if (indexOfDH >= 0) {
        System.arraycopy(eligSplit, indexOfDH + 1, eligSplit, indexOfDH, eligSplit.length - (indexOfDH + 1));
        eligSplit[eligSplit.length - 1] = "DH";
      }
      return Lists.newArrayList(eligSplit);
    }
  }

  public Multimap<Position, DraftPick> constructRoster(List<DraftPick> picks) {
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

  public EnumSet<Position> getOpenPositions(List<DraftPick> picks) {
    Multimap<Position, DraftPick> optimalRoster = constructRoster(picks);
    optimalRoster.removeAll(RS);
    if (optimalRoster.size() == POSITIONS.size()) {
      return EnumSet.noneOf(Position.class);
    }
    EnumSet<Position> openPositions = EnumSet.noneOf(Position.class);
    int reservesAllowed = 0;
    doGetOpenPositions(picks, Lists.newArrayList(POSITIONS), openPositions,
        ArrayListMultimap.<Position, DraftPick>create(), false, reservesAllowed);
    while (openPositions.isEmpty() && reservesAllowed <= 6) {
      doGetOpenPositions(picks, Lists.newArrayList(POSITIONS), openPositions,
          ArrayListMultimap.<Position, DraftPick>create(), true, reservesAllowed++);
    }
    return openPositions;
  }

  private static void doGetOpenPositions(
      List<DraftPick> picks, List<Position> positions, Set<Position> openPositions,
      Multimap<Position, DraftPick> roster, boolean allowDH, int reservesAllowed) {
    if (picks.isEmpty()) {
      List<Position> rosterOpenPositions = Lists.newArrayList(POSITIONS);
      for (Entry<Position, DraftPick> rosterEntry : roster.entries()) {
        rosterOpenPositions.remove(rosterEntry.getKey());
      }
      openPositions.addAll(rosterOpenPositions);
    } else {
      DraftPick pick = picks.remove(0);
      boolean foundPosition = false;
      for (String positionStr : pick.getEligibilities()) {
        Position position = Position.fromShortName(positionStr);
        if (!positions.contains(position)) {
          continue;
        }
        foundPosition = true;
        Multimap<Position, DraftPick> expandedRoster = ArrayListMultimap.create(roster);
        expandedRoster.put(position, pick);
        positions.remove(position);
        doGetOpenPositions(picks, positions, openPositions, expandedRoster, allowDH, reservesAllowed);
        positions.add(position);
      }
      if (!foundPosition) {
        if (allowDH
            && !pick.getEligibilities().contains(P.getShortName())
            && !pick.getEligibilities().contains(DH.getShortName())
            && positions.contains(DH)) {
          Multimap<Position, DraftPick> expandedRoster = ArrayListMultimap.create(roster);
          expandedRoster.put(DH, pick);
          positions.remove(DH);
          doGetOpenPositions(picks, positions, openPositions, expandedRoster, allowDH, reservesAllowed);
          positions.add(DH);
        } else if (reservesAllowed > 0) {
          doGetOpenPositions(picks, positions, openPositions, roster, allowDH, reservesAllowed - 1);
        }
      }
      picks.add(0, pick);
    }
  }

  public Map<Position, Integer[]> getNumFilled(List<DraftPick> picks, int pickNum) {
    Map<Position, Integer[]> numFilled = new HashMap<>();
    for (int i = 0; i < 10; i++) {
      final int team = i;
      Multimap<Position, DraftPick> roster = constructRoster(
          Lists.newArrayList(Iterables.filter(
              picks.subList(0, pickNum),
              new Predicate<DraftPick>() {
                @Override
                public boolean apply(DraftPick pick) {
                  return pick.getTeam() == team;
                }
              })));
      for (Position position : values()) {
        if (position == DH || position == RS) {
          continue;
        }
        Integer[] numFilledForPosition = numFilled.get(position);
        if (numFilledForPosition == null) {
          numFilledForPosition = new Integer[position == P ? 6 : position == OF ? 3 : 1];
          Arrays.fill(numFilledForPosition, 0);
          numFilled.put(position, numFilledForPosition);
        }
        for (int j = 0; j < numFilledForPosition.length; j++) {
          if (roster.get(position).size() >= j + 1) {
            numFilledForPosition[j] = numFilledForPosition[j] + 1;
          }
        }
      }
    }
    return numFilled;
  }
}