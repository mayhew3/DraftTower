package com.mayhew3.drafttower.server;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.shared.*;

import javax.inject.Inject;
import java.util.*;

import static com.mayhew3.drafttower.shared.PlayerColumn.*;
import static com.mayhew3.drafttower.shared.Position.OF;
import static com.mayhew3.drafttower.shared.Position.P;

/**
 * {@link PlayerDataSource} for testing.
 */
@Singleton
public class TestPlayerDataSource implements PlayerDataSource {

  private final BeanFactory beanFactory;
  private final TestPlayerGenerator playerGenerator;

  private final Map<Long, Player> allPlayers = new HashMap<>();
  private final Map<Long, Player> availablePlayers;
  private final List<DraftPick> draftPicks = new ArrayList<>();
  private ListMultimap<TeamDraftOrder, Integer> keepers = ArrayListMultimap.create();

  @Inject
  public TestPlayerDataSource(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
    this.playerGenerator = new TestPlayerGenerator(beanFactory);
    int playerId = 0;

    for (Position position : Position.REAL_POSITIONS) {
      int numPlayers;
      if (position == Position.P) {
        numPlayers = 140;
      } else if (position == OF) {
        numPlayers = 45;
      } else if (position == Position.DH) {
        numPlayers = 5;
      } else {
        numPlayers = 15;
      }
      for (int i = 0; i < numPlayers; i++) {
        Player player = playerGenerator.generatePlayer(playerId, position, i);
        allPlayers.put(player.getPlayerId(), player);
        playerId++;
      }
    }
    // TODO(kprevas): generate multi-position players
    availablePlayers = new HashMap<>(allPlayers);
  }

  @Override
  public UnclaimedPlayerListResponse lookupUnclaimedPlayers(UnclaimedPlayerListRequest request) {
    UnclaimedPlayerListResponse response = beanFactory.createUnclaimedPlayerListResponse().as();
    response.setPlayers(Lists.newArrayList(availablePlayers.values()));
    return response;
  }

  @Override
  public ListMultimap<TeamDraftOrder, Integer> getAllKeepers() {
    return keepers;
  }

  @Override
  public void populateQueueEntry(QueueEntry queueEntry) {
    Player player = allPlayers.get(queueEntry.getPlayerId());
    queueEntry.setPlayerName(player.getName());
    queueEntry.setEligibilities(
        RosterUtil.splitEligibilities(player.getEligibility()));
  }

  @Override
  public void populateDraftPick(DraftPick draftPick) {
    Player player = allPlayers.get(draftPick.getPlayerId());
    draftPick.setPlayerName(player.getName());
    draftPick.setEligibilities(
        RosterUtil.splitEligibilities(player.getEligibility()));
  }

  @Override
  public long getBestPlayerId(PlayerDataSet wizardTable, TeamDraftOrder team, final Set<Position> openPositions) {
    return Collections.max(availablePlayers.values(), new Comparator<Player>() {
      @Override
      public int compare(Player o1, Player o2) {
        return maxWizard(o1, openPositions) - maxWizard(o2, openPositions);
      }

      private int maxWizard(Player player, final Set<Position> openPositions) {
        String wizardStr = PlayerColumn.getWizard(player, EnumSet.copyOf(openPositions));
        return wizardStr == null
            ? Integer.MIN_VALUE
            : (int) (Float.parseFloat(wizardStr) * 1000);
      }
    }).getPlayerId();
  }

  @Override
  public void changePlayerRank(ChangePlayerRankRequest request) {
    // TODO(kprevas): implement
  }

  @Override
  public void postDraftPick(DraftPick draftPick, DraftStatus status) {
    draftPicks.add(draftPick);
    availablePlayers.remove(draftPick.getPlayerId());
  }

  @Override
  public void backOutLastDraftPick(int pickToRemove) {
    DraftPick draftPick = draftPicks.remove(draftPicks.size() - 1);
    availablePlayers.put(draftPick.getPlayerId(), allPlayers.get(draftPick.getPlayerId()));
  }

  @Override
  public void populateDraftStatus(DraftStatus status) {
    status.getPicks().addAll(draftPicks);
  }

  @Override
  public void copyTableSpecToCustom(CopyAllPlayerRanksRequest request) {
    // TODO(kprevas): implement
  }

  @Override
  public GraphsData getGraphsData(TeamDraftOrder myTeam) {
    GraphsData graphsData = beanFactory.createGraphsData().as();
    Map<Integer, Map<PlayerColumn, Float>> teamValues = new HashMap<>();
    Map<Integer, Integer> pitchersPerTeam = new HashMap<>();
    Map<Integer, Integer> battersPerTeam = new HashMap<>();
    for (int i = 1; i <= 10; i++) {
      teamValues.put(i, new HashMap<PlayerColumn, Float>());
      pitchersPerTeam.put(i, 0);
      battersPerTeam.put(i, 0);
    }

    for (DraftPick draftPick : draftPicks) {
      Player player = allPlayers.get(draftPick.getPlayerId());
      int team = draftPick.getTeam();
      Map<PlayerColumn, Float> values = teamValues.get(team);
      for (PlayerColumn graphStat : GraphsData.GRAPH_STATS) {
        String valueStr = graphStat.get(player);
        if (valueStr != null) {
          float value = Float.parseFloat(valueStr);
          if (values.containsKey(graphStat)) {
            if (EnumSet.of(OBP, SLG, ERA, WHIP).contains(graphStat)) {
              int oldDenom = EnumSet.of(OBP, SLG).contains(graphStat)
                  ? pitchersPerTeam.get(team)
                  : battersPerTeam.get(team);
              int newDenom = oldDenom + 1;
              values.put(graphStat, values.get(graphStat) * oldDenom / newDenom + value / newDenom);
            } else {
              values.put(graphStat, values.get(graphStat) + value);
            }
          } else {
            values.put(graphStat, value);
          }
        }
      }
      if (Position.apply(player, EnumSet.of(P))) {
        pitchersPerTeam.put(team, pitchersPerTeam.get(team) + 1);
      } else {
        battersPerTeam.put(team, battersPerTeam.get(team) + 1);
      }
    }

    graphsData.setMyValues(teamValues.get(myTeam.get()));
    Map<PlayerColumn, Float> avgValues = new HashMap<>();
    for (PlayerColumn graphStat : GraphsData.GRAPH_STATS) {
      for (Integer team : teamValues.keySet()) {
        Float teamStatValue = teamValues.get(team).get(graphStat);
        if (teamStatValue != null) {
          avgValues.put(graphStat,
              (avgValues.containsKey(graphStat) ? avgValues.get(graphStat) : 0)
                  + teamStatValue / 10);
        }
      }
    }
    graphsData.setAvgValues(avgValues);

    return graphsData;
  }

  public long getNextUnclaimedPlayer(Position position) {
    for (long i = 0; i < allPlayers.size(); i++) {
      if (availablePlayers.containsKey(i)
          && Position.apply(availablePlayers.get(i), EnumSet.of(position))) {
        return i;
      }
    }
    throw new IllegalStateException("Out of players at " + position.getLongName());
  }

  public void setDraftPicks(List<DraftPick> draftPicks) {
    this.draftPicks.clear();
    this.draftPicks.addAll(draftPicks);
  }

  public void setKeepers(ListMultimap<TeamDraftOrder, Integer> keepers) {
    this.keepers = keepers;
  }

  public Player getPlayer(long playerId) {
    return allPlayers.get(playerId);
  }
}