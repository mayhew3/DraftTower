package com.mayhew3.drafttower.server;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.shared.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Provides probability of players being selected.
 */
@Singleton
public class PickProbabilityPredictor {

  private static final Logger logger = Logger.getLogger(DraftControllerImpl.class.getName());

  private final Map<TeamDraftOrder, Map<Long, Float>> predictionsByTeam = new ConcurrentHashMap<>();
  private final PlayerDataProvider playerDataProvider;
  private final TeamDataSource teamDataSource;
  private final BeanFactory beanFactory;
  private final RosterUtil rosterUtil;
  private final PredictionModel predictionModel;
  private int lastPicksSize = -1;

  @Inject
  public PickProbabilityPredictor(PlayerDataProvider playerDataProvider,
      TeamDataSource teamDataSource,
      BeanFactory beanFactory,
      RosterUtil rosterUtil,
      PredictionModel predictionModel) {
    this.playerDataProvider = playerDataProvider;
    this.teamDataSource = teamDataSource;
    this.beanFactory = beanFactory;
    this.rosterUtil = rosterUtil;
    this.predictionModel = predictionModel;

    for (int i = 1; i <= 10; i++) {
      predictionsByTeam.put(new TeamDraftOrder(i), new HashMap<Long, Float>());
    }
  }

  public void onDraftStatusChanged(DraftStatus draftStatus) {
    List<DraftPick> picks = draftStatus.getPicks();
    if (lastPicksSize > picks.size()) {
      lastPicksSize = picks.size();
      return;
    }
    if (picks.size() == lastPicksSize) {
      return;
    }
    // When draft status changes, recompute predictions for team that just picked.
    if (picks.size() - lastPicksSize > 10) {
      lastPicksSize = picks.size() - 10;
    }
    PlayerColumn sortCol = predictionModel.getSortCol();
    boolean ascending = predictionModel.getSortColAscending();
    Set<Long> selectedPlayers = new HashSet<>();
    for (int i = 0; i < lastPicksSize; i++) {
      selectedPlayers.add(picks.get(i).getPlayerId());
    }
    for (int pickNum = lastPicksSize; pickNum < picks.size(); pickNum++) {
      try {
        if (pickNum >= 0 && pickNum < picks.size()) {
          selectedPlayers.add(picks.get(pickNum).getPlayerId());
        }
        int nextTeam = (pickNum < 0 || picks.isEmpty()) ? 1 : picks.get(pickNum).getTeam() + 1;
        if (nextTeam > 10) {
          nextTeam -= 10;
        }
        int nextPickNum = pickNum + 1;
        logger.info("Updating predictions for team " + nextTeam);
        Map<Position, Integer[]> numFilled = rosterUtil.getNumFilled(picks, nextPickNum);

        TeamDraftOrder draftOrder = new TeamDraftOrder(nextTeam);
        ListMultimap<Position, Long> topPlayerIds = getTopPlayers(selectedPlayers, draftOrder, sortCol, ascending);

        Map<Long, Float> predictions = predictionsByTeam.get(draftOrder);
        predictions.clear();
        for (Entry<Position, Collection<Long>> entry : topPlayerIds.asMap().entrySet()) {
          Position position = entry.getKey();
          // Safe cast per javadoc of ListMultimap.
          List<Long> topPlayers = (List<Long>) entry.getValue();
          for (int i = 0; i < getNumPlayersForPosition(position); i++) {
            if (i < topPlayers.size()) {
              predictions.put(topPlayers.get(i),
                  predictionModel.getPrediction(position, i, nextPickNum, numFilled.get(position)));
            }
          }
        }
      } catch (DataSourceException e) {
        e.printStackTrace();
      }
    }
    lastPicksSize = picks.size();
  }

  private ListMultimap<Position, Long> getTopPlayers(
      Set<Long> selectedPlayers,
      TeamDraftOrder draftOrder,
      PlayerColumn sortCol, boolean ascending) throws DataSourceException {
    TableSpec tableSpec = beanFactory.createTableSpec().as();
    tableSpec.setPlayerDataSet(PlayerDataSet.CBSSPORTS);
    tableSpec.setSortCol(sortCol);
    tableSpec.setAscending(ascending);

    List<Player> players = playerDataProvider.getPlayers(
        teamDataSource.getTeamIdByDraftOrder(draftOrder), tableSpec);

    ListMultimap<Position, Long> topPlayerIds = ArrayListMultimap.create();
    for (Player player : players) {
      if (selectedPlayers.contains(player.getPlayerId())) {
        continue;
      }
      for (String positionStr : RosterUtil.splitEligibilities(player.getEligibility())) {
        Position position = Position.fromShortName(positionStr);
        if (position == Position.DH) {
          continue;
        }
        List<Long> topPositionIds = topPlayerIds.get(position);
        if (topPositionIds.size() < getNumPlayersForPosition(position)) {
          topPositionIds.add(player.getPlayerId());
        }
      }
      boolean filledAllPositions = true;
      for (Position position : Position.REAL_POSITIONS) {
        if (position != Position.DH &&
            topPlayerIds.get(position).size() < getNumPlayersForPosition(position)) {
          filledAllPositions = false;
          break;
        }
      }
      if (filledAllPositions) {
        break;
      }
    }
    return topPlayerIds;
  }

  private int getNumPlayersForPosition(Position position) {
    return (position == Position.P || position == Position.OF) ? 5 : 3;
  }

  public Map<Long, Float> getTeamPredictions(TeamDraftOrder teamDraftOrder) {
    return predictionsByTeam.get(teamDraftOrder);
  }

  public void reset() {
    lastPicksSize = -1;
    for (TeamDraftOrder team : predictionsByTeam.keySet()) {
      predictionsByTeam.get(team).clear();
    }
  }
}