package com.mayhew3.drafttower.server;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Ordering;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.mayhew3.drafttower.shared.*;

import java.util.*;

import static com.mayhew3.drafttower.shared.PlayerColumn.*;
import static com.mayhew3.drafttower.shared.Position.OF;
import static com.mayhew3.drafttower.shared.Position.P;

/**
 * {@link PlayerDataSource} for testing.
 */
public abstract class TestPlayerDataSource implements PlayerDataSource {

  private final BeanFactory beanFactory;

  private final Map<Long, Player> allPlayers = new HashMap<>();
  private final Map<Long, Player> availablePlayers;
  private final List<DraftPick> draftPicks;
  private ListMultimap<TeamDraftOrder, Integer> keepers = ArrayListMultimap.create();

  public TestPlayerDataSource(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
    TestPlayerGenerator playerGenerator = new TestPlayerGenerator(beanFactory);
    int playerId = 0;

    draftPicks = createDraftPicksList();

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

  protected abstract List<DraftPick> createDraftPicksList();

  @Override
  public List<Player> getPlayers(TeamId teamId, PlayerDataSet playerDataSet) {
    synchronized (availablePlayers) {
      List<Player> players = new ArrayList<>();
      for (Player player : availablePlayers.values()) {
        players.add(AutoBeanCodex.decode(beanFactory, Player.class,
            AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(player))).as());
      }
      return players;
    }
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
  public void shiftInBetweenRanks(TeamId teamID, int lesserRank, int greaterRank, boolean increase) {
    for (Player player : allPlayers.values()) {
      int rank = Integer.parseInt(player.getMyRank());
      if (rank >= lesserRank && rank <= greaterRank) {
        if (increase) {
          player.setMyRank(Integer.toString(rank + 1));
        } else {
          player.setMyRank(Integer.toString(rank - 1));
        }
      }
    }
  }

  @Override
  public void updatePlayerRank(TeamId teamID, int newRank, long playerID) {
    for (Player player : allPlayers.values()) {
      if (player.getPlayerId() == playerID) {
        player.setMyRank(Integer.toString(newRank));
        break;
      }
    }
  }

  @Override
  public void addFavorite(TeamId teamID, long playerID) {
    for (Player player : allPlayers.values()) {
      if (player.getPlayerId() == playerID) {
        player.setFavorite(true);
        break;
      }
    }
  }

  @Override
  public void removeFavorite(TeamId teamID, long playerID) {
    for (Player player : allPlayers.values()) {
      if (player.getPlayerId() == playerID) {
        player.setFavorite(false);
        break;
      }
    }
  }

  @Override
  public void resetDraft() {
    availablePlayers.clear();
    availablePlayers.putAll(allPlayers);
    draftPicks.clear();
  }

  @Override
  public void postDraftPick(DraftPick draftPick, DraftStatus status) {
    draftPicks.add(draftPick);
    synchronized (availablePlayers) {
      availablePlayers.remove(draftPick.getPlayerId());
    }
  }

  @Override
  public void backOutLastDraftPick(int pickToRemove) {
    DraftPick draftPick = draftPicks.remove(draftPicks.size() - 1);
    synchronized (availablePlayers) {
      availablePlayers.put(draftPick.getPlayerId(), allPlayers.get(draftPick.getPlayerId()));
    }
  }

  @Override
  public void populateDraftStatus(DraftStatus status) {
    status.getPicks().addAll(draftPicks);
  }

  @Override
  public void copyTableSpecToCustom(TeamId teamId, TableSpec tableSpec) {
    PlayerColumn sortCol = tableSpec.getSortCol();
    Comparator<Player> comparator = sortCol.getComparator(tableSpec.isAscending());
    synchronized (availablePlayers) {
      List<Player> sortedPlayers = Ordering.from(comparator).sortedCopy(allPlayers.values());
      for (int i = 0; i < sortedPlayers.size(); i++) {
        sortedPlayers.get(i).setMyRank(Integer.toString(i + 1));
      }
    }
  }

  @Override
  public GraphsData getGraphsData(TeamDraftOrder myTeam) {
    GraphsData graphsData = beanFactory.createGraphsData().as();
    Map<Integer, Map<PlayerColumn, Float>> teamValues = new HashMap<>();
    Map<Integer, Float> teamPitchingPoints = new HashMap<>();
    Map<Integer, Float> teamBattingPoints = new HashMap<>();
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
      if (Scoring.CATEGORIES) {
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
      } else {
        float pts = Float.parseFloat(PTS.get(player));
        if (Position.apply(player, EnumSet.of(P))) {
          teamPitchingPoints.put(team,
              teamPitchingPoints.containsKey(team)
                  ? teamPitchingPoints.get(team) + pts
                  : pts);
        } else {
          teamBattingPoints.put(team,
              teamBattingPoints.containsKey(team)
                  ? teamBattingPoints.get(team) + pts
                  : pts);
        }
      }
      if (Position.apply(player, EnumSet.of(P))) {
        pitchersPerTeam.put(team, pitchersPerTeam.get(team) + 1);
      } else {
        battersPerTeam.put(team, battersPerTeam.get(team) + 1);
      }
    }

    if (Scoring.CATEGORIES) {
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
    } else {
      Map<String, Float> teamPitchingValues = new HashMap<>();
      Map<String, Float> teamBattingValues = new HashMap<>();
      Map<String, Float> teamTotals = new HashMap<>();
      for (int i = 1; i <= 10; i++) {
        teamPitchingValues.put(Integer.toString(i), teamPitchingPoints.get(i));
        teamBattingValues.put(Integer.toString(i), teamBattingPoints.get(i));
        if (!teamBattingPoints.containsKey(i)) {
          if (teamPitchingPoints.containsKey(i)) {
            teamTotals.put(Integer.toString(i), teamPitchingPoints.get(i));
          } else {
            teamTotals.put(Integer.toString(i), null);
          }
        } else if (!teamPitchingPoints.containsKey(i)) {
          teamTotals.put(Integer.toString(i), teamBattingPoints.get(i));
        } else {
          teamTotals.put(Integer.toString(i), teamBattingPoints.get(i) + teamPitchingPoints.get(i));
        }
      }
      graphsData.setTeamPitchingValues(teamPitchingValues);
      graphsData.setTeamBattingValues(teamBattingValues);
      graphsData.setTeamTotals(teamTotals);
    }

    return graphsData;
  }

  public long getNextUnclaimedPlayer(Position position) {
    synchronized (availablePlayers) {
      for (long i = 0; i < allPlayers.size(); i++) {
        if (availablePlayers.containsKey(i)
            && Position.apply(availablePlayers.get(i), EnumSet.of(position))) {
          return i;
        }
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

  public Collection<Player> getAllPlayers() {
    return allPlayers.values();
  }

  public Collection<Player> getAvailablePlayers() {
    return availablePlayers.values();
  }
}