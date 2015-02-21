package com.mayhew3.drafttower.server;

import com.google.common.collect.ListMultimap;
import com.mayhew3.drafttower.shared.*;

import java.util.List;

/**
 * Handles lookup and persistence of player-related data.
 */
public interface PlayerDataSource {

  ListMultimap<TeamDraftOrder, Integer> getAllKeepers() throws DataSourceException;

  void populateQueueEntry(QueueEntry queueEntry) throws DataSourceException;

  void populateDraftPick(DraftPick draftPick) throws DataSourceException;

  void postDraftPick(DraftPick draftPick, DraftStatus status) throws DataSourceException;

  void backOutLastDraftPick(int pickToRemove) throws DataSourceException;

  void populateDraftStatus(DraftStatus status) throws DataSourceException;

  void copyTableSpecToCustom(TeamId teamID, TableSpec tableSpec) throws DataSourceException;

  GraphsData getGraphsData(TeamDraftOrder myTeam) throws DataSourceException;

  List<Player> getPlayers(TeamId teamId, TableSpec tableSpec) throws DataSourceException;

  void shiftInBetweenRanks(TeamId teamID, int lesserRank, int greaterRank, boolean increase);

  void updatePlayerRank(TeamId teamID, int newRank, long playerID);
}