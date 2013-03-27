package com.mayhew3.drafttower.server;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mayhew3.drafttower.shared.*;

import javax.servlet.ServletException;
import java.sql.SQLException;
import java.util.Set;

/**
 * {@link PlayerDataSource} for testing.
 */
public class TestPlayerDataSource implements PlayerDataSource {
  @Override
  public UnclaimedPlayerListResponse lookupUnclaimedPlayers(UnclaimedPlayerListRequest request) throws ServletException {
    return null;
  }

  @Override
  public ListMultimap<Integer, Integer> getAllKeepers() throws ServletException {
    return ArrayListMultimap.create();
  }

  @Override
  public void populateQueueEntry(QueueEntry queueEntry) throws SQLException {
  }

  @Override
  public void populateDraftPick(DraftPick draftPick) throws SQLException {
  }

  @Override
  public long getBestPlayerId(PlayerDataSet wizardTable, Integer team, Set<Position> openPositions) throws SQLException {
    return 0;
  }

  @Override
  public void changePlayerRank(ChangePlayerRankRequest request) {
  }

  @Override
  public void postDraftPick(DraftPick draftPick, DraftStatus status) throws SQLException {
  }

  @Override
  public void backOutLastDraftPick(int pickToRemove) throws SQLException {
  }

  @Override
  public void populateDraftStatus(DraftStatus status) throws SQLException {
  }

  @Override
  public void copyTableSpecToCustom(CopyAllPlayerRanksRequest request) throws SQLException {
  }

  @Override
  public GraphsData getGraphsData(int team) throws SQLException {
    return null;
  }
}