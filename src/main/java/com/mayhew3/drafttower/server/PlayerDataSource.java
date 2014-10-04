package com.mayhew3.drafttower.server;

import com.google.common.collect.ListMultimap;
import com.mayhew3.drafttower.shared.*;

import java.util.Set;

/**
 * Handles lookup and persistence of player-related data.
 */
public interface PlayerDataSource {

  UnclaimedPlayerListResponse lookupUnclaimedPlayers(UnclaimedPlayerListRequest request) throws DataSourceException;

  ListMultimap<TeamDraftOrder, Integer> getAllKeepers() throws DataSourceException;

  void populateQueueEntry(QueueEntry queueEntry) throws DataSourceException;

  void populateDraftPick(DraftPick draftPick) throws DataSourceException;

  long getBestPlayerId(PlayerDataSet wizardTable, TeamDraftOrder team, Set<Position> openPositions) throws DataSourceException;

  void changePlayerRank(ChangePlayerRankRequest request) throws DataSourceException;

  void postDraftPick(DraftPick draftPick, DraftStatus status) throws DataSourceException;

  void backOutLastDraftPick(int pickToRemove) throws DataSourceException;

  void populateDraftStatus(DraftStatus status) throws DataSourceException;

  void copyTableSpecToCustom(CopyAllPlayerRanksRequest request) throws DataSourceException;

  GraphsData getGraphsData(TeamDraftOrder myTeam) throws DataSourceException;
}