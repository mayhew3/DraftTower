package com.mayhew3.drafttower.server;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.mayhew3.drafttower.TestPlayerGenerator;
import com.mayhew3.drafttower.shared.*;

import javax.inject.Inject;
import java.util.*;

import static com.mayhew3.drafttower.shared.Position.OF;

/**
 * {@link PlayerDataSource} for testing.
 */
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
        PlayerDataSourceImpl.splitEligibilities(player.getEligibility()));
  }

  @Override
  public void populateDraftPick(DraftPick draftPick) {
    Player player = allPlayers.get(draftPick.getPlayerId());
    draftPick.setPlayerName(player.getName());
    draftPick.setEligibilities(
        PlayerDataSourceImpl.splitEligibilities(player.getEligibility()));
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
  public GraphsData getGraphsData(TeamDraftOrder teamDraftOrder) {
    GraphsData graphsData = beanFactory.createGraphsData().as();
    Map<PlayerColumn, Float> myValues = new HashMap<>();
    graphsData.setMyValues(myValues);
    Map<PlayerColumn, Float> avgValues = new HashMap<>();
    graphsData.setAvgValues(avgValues);

    for (DraftPick draftPick : draftPicks) {
      Player player = allPlayers.get(draftPick.getPlayerId());
      boolean myPick = draftPick.getTeam() == teamDraftOrder.get();
      for (PlayerColumn graphStat : GraphsData.GRAPH_STATS) {
        String valueStr = graphStat.get(player);
        if (valueStr != null) {
          float value = Float.parseFloat(valueStr);
          if (myPick) {
            if (!myValues.containsKey(graphStat)) {
              myValues.put(graphStat, 0f);
            }
            myValues.put(graphStat, myValues.get(graphStat) + value);
          }
          if (!avgValues.containsKey(graphStat)) {
            avgValues.put(graphStat, 0f);
          }
          avgValues.put(graphStat, avgValues.get(graphStat) + (value / 10));
        }
      }
    }

    return graphsData;
  }

  public void setKeepers(ListMultimap<TeamDraftOrder, Integer> keepers) {
    this.keepers = keepers;
  }
}