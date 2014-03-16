package com.mayhew3.drafttower.server;

import com.google.common.collect.ListMultimap;
import com.mayhew3.drafttower.shared.*;

import javax.servlet.ServletException;
import java.sql.SQLException;
import java.util.Set;

/**
 * Handles lookup and persistence of player-related data.
 */
public interface PlayerDataSource {
  UnclaimedPlayerListResponse lookupUnclaimedPlayers(UnclaimedPlayerListRequest request) throws ServletException;

  ListMultimap<TeamDraftOrder, Integer> getAllKeepers() throws ServletException;

  void populateQueueEntry(QueueEntry queueEntry) throws SQLException;

  void populateDraftPick(DraftPick draftPick) throws SQLException;

  long getBestPlayerId(PlayerDataSet wizardTable, TeamDraftOrder team, Set<Position> openPositions) throws SQLException;

  void changePlayerRank(ChangePlayerRankRequest request) throws ServletException;

  void postDraftPick(DraftPick draftPick, DraftStatus status) throws SQLException;

  void backOutLastDraftPick(int pickToRemove) throws SQLException;

  void populateDraftStatus(DraftStatus status) throws SQLException;

  void copyTableSpecToCustom(CopyAllPlayerRanksRequest request) throws SQLException;

  GraphsData getGraphsData(TeamDraftOrder teamDraftOrder) throws SQLException;
}