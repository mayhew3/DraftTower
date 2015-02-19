package com.mayhew3.drafttower.server;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.shared.*;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.mayhew3.drafttower.shared.Position.*;
import static java.util.logging.Level.*;

/**
 * Looks up players in the database.
 */
@Singleton
public class PlayerDataSourceImpl implements PlayerDataSource {

  private static final Logger logger = Logger.getLogger(PlayerDataSourceImpl.class.getName());

  private final DataSource db;
  private final BeanFactory beanFactory;
  private final TeamDataSource teamDataSource;
  private final Map<String, TeamDraftOrder> teamTokens;
  private final int numTeams;

  private final Map<String, List<Player>> cache = new ConcurrentHashMap<>();

  @Inject
  public PlayerDataSourceImpl(DataSource db,
      BeanFactory beanFactory,
      TeamDataSource teamDataSource,
      @TeamTokens Map<String, TeamDraftOrder> teamTokens,
      @NumTeams int numTeams) throws DataSourceException {
    this.db = db;
    this.beanFactory = beanFactory;
    this.teamDataSource = teamDataSource;
    this.teamTokens = teamTokens;
    this.numTeams = numTeams;

    // Warm up caches
    TableSpec tableSpec = beanFactory.createTableSpec().as();
    tableSpec.setSortCol(PlayerColumn.MYRANK);
    tableSpec.setAscending(true);
    for (int i = 1; i <= 10; i++) {
      TeamId team = new TeamId(i);
      for (PlayerDataSet playerDataSet : PlayerDataSet.values()) {
        tableSpec.setPlayerDataSet(playerDataSet);
        getPlayers(team, tableSpec);
      }
    }
  }

  @Override
  public UnclaimedPlayerListResponse lookupUnclaimedPlayers(UnclaimedPlayerListRequest request) throws DataSourceException {
    Stopwatch stopwatch = Stopwatch.createStarted();
    UnclaimedPlayerListResponse response = beanFactory.createUnclaimedPlayerListResponse().as();

    TeamId teamId = teamDataSource.getTeamIdByDraftOrder(teamTokens.get(request.getTeamToken()));
    TableSpec tableSpec = request.getTableSpec();
    List<Player> players = getPlayers(teamId, tableSpec);

    response.setPlayers(players);

    stopwatch.stop();
    logger.info("Player table request took " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
    return response;
  }

  @Override
  public List<Player> getPlayers(TeamId teamId, TableSpec tableSpec) throws DataSourceException {
    List<Player> players;
    ResultSet resultSet = null;
    try {
      String cacheKey = getKey(tableSpec, teamId);
      if (cache.containsKey(cacheKey)) {
        players = cache.get(cacheKey);
      } else {
        players = new ArrayList<>();

        resultSet = getResultSetForUnclaimedPlayerRows(teamId, tableSpec);
        while (resultSet.next()) {
          Player player = beanFactory.createPlayer().as();
          player.setPlayerId(resultSet.getInt("PlayerID"));
          player.setCBSId(resultSet.getInt("CBS_ID"));

          List<PlayerColumn> playerColumns = Lists.newArrayList(PlayerColumn.valuesForScoring());
          playerColumns.remove(PlayerColumn.NAME);
          PlayerColumn.NAME.set(player, resultSet.getString("LastName") + ", " + resultSet.getString("FirstName"));
          playerColumns.remove(PlayerColumn.PTS);

          for (PlayerColumn playerColumn : playerColumns) {
            if (playerColumn == PlayerColumn.WIZARD) {
              for (Position position : REAL_POSITIONS) {
                String columnString = resultSet.getString(PlayerColumn.WIZARD.getColumnName() + position.getShortName());
                if (columnString != null) {
                  PlayerColumn.setWizard(player, columnString, position);
                }
              }
            } else if (playerColumn == PlayerColumn.ELIG) {
              String[] elig = resultSet.getString(playerColumn.getColumnName()).split(",");
              int indexOfDH = Arrays.binarySearch(elig, "DH");
              if (indexOfDH >= 0) {
                System.arraycopy(elig, indexOfDH + 1, elig, indexOfDH, elig.length - (indexOfDH + 1));
                elig[elig.length - 1] = "DH";
              }
              playerColumn.set(player, Joiner.on(",").join(elig));
            } else {
              String columnString = resultSet.getString(playerColumn.getColumnName());
              if (columnString != null) {
                if (columnString.startsWith("0.")) {
                  columnString = columnString.substring(1);
                }
                playerColumn.set(player, columnString);
              }
            }
          }
          if (Scoring.POINTS) {
            // TODO m3: read points values from DB?
            if (player.getEligibility().contains("P")) {
              player.setPoints(Float.toString(
                  parseFloatOrZero(player.getINN()) * 2.3f +
                      parseFloatOrZero(player.getHA()) * -0.5f +
                      parseFloatOrZero(player.getBBI()) * -1.5f +
                      parseFloatOrZero(player.getK()) * 2f +
                      parseFloatOrZero(player.getER()) * -1.5f +
                      parseFloatOrZero(player.getHRA()) * -2f +
                      parseFloatOrZero(player.getWL()) * 10f +
                      parseFloatOrZero(player.getS()) * 10f
              ));
            } else {
              player.setPoints(Float.toString(
                  parseFloatOrZero(player.getAB()) * -2f +
                      parseFloatOrZero(player.getH()) * 6f +
                      parseFloatOrZero(player.get2B()) * 3f +
                      parseFloatOrZero(player.get3B()) * 5f +
                      parseFloatOrZero(player.getHR()) * 5f +
                      parseFloatOrZero(player.getRHR()) * 3f +
                      parseFloatOrZero(player.getRBI()) * 3f +
                      parseFloatOrZero(player.getSB()) * 2f +
                      parseFloatOrZero(player.getCS()) * -4f +
                      parseFloatOrZero(player.getBB()) * 3f
              ));
            }
          }

          String injury = resultSet.getString("Injury");
          if (injury != null) {
            player.setInjury(injury);
          }

          players.add(player);
        }
        cache.put(cacheKey, players);
      }
    } catch (SQLException e) {
      throw new DataSourceException("Error getting next element of results.", e);
    } finally {
      close(resultSet);
    }
    Comparator<Player> comparator = tableSpec.getSortCol() == PlayerColumn.WIZARD
        ? PlayerColumn.getWizardComparator(tableSpec.isAscending(), EnumSet.allOf(Position.class))
        : tableSpec.getSortCol().getComparator(tableSpec.isAscending());
    players = Ordering.from(comparator).sortedCopy(players);
    return players;
  }

  private float parseFloatOrZero(String value) {
    if (value == null) {
      return 0f;
    }
    return Float.parseFloat(value);
  }

  @Override
  public ListMultimap<TeamDraftOrder, Integer> getAllKeepers() throws DataSourceException {
    ListMultimap<TeamDraftOrder, Integer> keepers = ArrayListMultimap.create();

    String sql = "select TeamID, PlayerID from keepers";

    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(sql);
      while (resultSet.next()) {
        TeamId teamID = new TeamId(resultSet.getInt("TeamID"));
        int playerID = resultSet.getInt("PlayerID");
        keepers.put(teamDataSource.getDraftOrderByTeamId(teamID), playerID);
      }
    } catch (SQLException e) {
      throw new DataSourceException("Error retreiving keepers from database.", e);
    } finally {
      close(resultSet);
    }
    return keepers;
  }



  // Unclaimed Player Queries

  private ResultSet getResultSetForUnclaimedPlayerRows(final TeamId teamID, TableSpec tableSpec)
      throws SQLException {

    String sql = "select * from ";
    sql = getFromJoins(teamID, sql, null, true, true);

    sql = addFilters(sql, tableSpec);

    return executeQuery(sql);
  }

  @Override
  public long getBestPlayerId(PlayerDataSet wizardTable, TeamDraftOrder teamDraftOrder, Set<Position> openPositions) throws DataSourceException {
    TeamId teamId = teamDataSource.getTeamIdByDraftOrder(teamDraftOrder);

    String sql = "select PlayerID, Eligibility from ";
    sql = getFromJoins(teamId, sql, createFilterStringFromPositions(openPositions), true, false);

    List<String> filters = new ArrayList<>();
    addDataSetFilter(filters, wizardTable);

    if (!filters.isEmpty()) {
      sql += " where " + Joiner.on(" and ").join(filters) + " ";
    }

    if (wizardTable == null) {
      sql += " order by MyRank asc";
    } else {
      String wizardColumnName = PlayerColumn.WIZARD.getColumnName();
      sql += " order by case when " + wizardColumnName + " is null then 1 else 0 end, " + wizardColumnName + " desc ";
    }

    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(sql);
      Long firstReserve = null;
      while (resultSet.next()) {
        if (firstReserve == null) {
          firstReserve = resultSet.getLong("PlayerID");
          if (openPositions.isEmpty()) {
            return firstReserve;
          }
        }
        List<String> eligibility = RosterUtil.splitEligibilities(
            resultSet.getString("Eligibility"));
        if (!eligibility.contains("P") && openPositions.contains(DH)) {
          return resultSet.getLong("PlayerID");
        }
        for (String position : eligibility) {
          if (openPositions.contains(Position.fromShortName(position))) {
            return resultSet.getLong("PlayerID");
          }
        }
      }
      //noinspection ConstantConditions
      return firstReserve;
    } catch (SQLException e) {
      throw new DataSourceException(e);
    } finally {
      close(resultSet);
    }
  }


  // Unclaimed Player utility methods

  @SuppressWarnings("ConstantConditions")
  private String getFromJoins(TeamId teamID, String sql, String positionFilterString, boolean filterClaimed, boolean allWizardPositions) {
    String wizardFilterClause = "";
    String playerFilterClause = "";

    if (positionFilterString != null) {
      wizardFilterClause = " and Position " + positionFilterString + " ";
      playerFilterClause = " and pa.PlayerID IN (SELECT PlayerID FROM eligibilities WHERE Position " + positionFilterString + ") ";
    }


    String subselect = "(SELECT PlayerID, 'Pitcher' AS Role,\n" +
        " APP as G, NULL AS AB, \n" +
        (Scoring.CATEGORIES ? (
        "  NULL AS OBP,\n" +
        "  NULL AS SLG,\n" +
        "  NULL AS RHR,\n" +
        "  NULL AS RBI,\n" +
        "  NULL AS HR,\n" +
        "  NULL AS SBC,\n" +
        "  ROUND(INN, 0) AS INN, ROUND(ERA, 2) AS ERA, ROUND(WHIP, 3) AS WHIP, WL, K, S, "
        ) : (
        "  NULL AS H,\n" +
        "  NULL AS 2B,\n" +
        "  NULL AS 3B,\n" +
        "  NULL AS HR,\n" +
        "  NULL AS RHR,\n" +
        "  NULL AS RBI,\n" +
        "  NULL AS SB,\n" +
        "  NULL AS CS,\n" +
        "  NULL AS BB,\n" +
        "  ROUND(INN, 0) AS INN, HA, BBI, K, ER, HRA, WL, S, "
        )) +
        "Rank, Draft, DataSource, \n" +
        "  (select round(coalesce(max((Rating-1)*0.5), 0), 3)\n" +
        "   from wizardratings\n" +
        "   where projectionRow = projectionspitching.ID\n" +
        "   and batting = 0\n" +
        "  ) as Wizard" +
            (allWizardPositions ?
            ",\n" +
            "  (select round((Rating-1)*0.5, 3)\n" +
            "   from wizardratings\n" +
            "   where projectionRow = projectionspitching.ID\n" +
            "   and batting = 0\n" +
            "  ) as WizardP, " +
            getNullBattingWizardClauses()
            : "") +
        " FROM projectionspitching)\n" +
        " UNION\n" +
        " (SELECT PlayerID, 'Batter' AS Role,\n" +
        " G, AB, \n" +
        (Scoring.CATEGORIES ? (
        "  ROUND(OBP, 3) AS OBP, ROUND(SLG, 3) AS SLG, RHR, RBI, HR, SBC,\n" +
        "  NULL AS INN,\n" +
        "  NULL AS ERA,\n" +
        "  NULL AS WHIP,\n" +
        "  NULL AS WL,\n" +
        "  NULL AS K,\n" +
        "  NULL AS S,\n"
        ) : (
            "  H, 2B, 3B, HR, RHR, RBI, SB, CS, BB,\n" +
            "  NULL AS INN, " +
            "  NULL AS HA, " +
            "  NULL AS BBI," +
            "  NULL AS K," +
            "  NULL AS ER," +
            "  NULL AS HRA," +
            "  NULL AS WL," +
            "  NULL AS S, "
        )) +
        "  Rank, Draft, DataSource, \n" +
        "  (select round(coalesce(max(Rating), 0), 3) \n" +
        "   from wizardratings\n" +
        "   where projectionRow = projectionsbatting.ID\n" +
        "   and batting = 1 \n" +
        wizardFilterClause +
        "  ) as Wizard\n" +
        (allWizardPositions
            ? ",\n" +
            "  NULL as WizardP, \n" +
            getBattingWizardClauses()
            : "") +
        " FROM projectionsbatting)";

    sql +=
        "(SELECT p.PlayerString as Player, p.CBS_ID, p.FirstName, p.LastName, p.MLBTeam, p.Eligibility, \n" +
            " CASE Eligibility WHEN '' THEN 'DH' WHEN NULL THEN 'DH' ELSE Eligibility END as Position, \n" +
            "ds.name as Source, " +
            "cr.Rank as MyRank, " +
            " p.Injury,\n" +
            " pa.*\n" +
            "FROM (" + subselect + ") pa\n" +
            "INNER JOIN players p\n" +
            " ON pa.PlayerID = p.ID\n" +
            "INNER JOIN data_sources ds\n" +
            " ON pa.DataSource = ds.ID\n" +
            "INNER JOIN customrankings cr\n" +
            " ON cr.PlayerID = pa.PlayerID\n" +
            "WHERE cr.TeamID = " + teamID + " \n" +
            playerFilterClause;

    if (filterClaimed) {
      sql += " AND pa.PlayerID NOT IN (SELECT PlayerID FROM draftresults WHERE BackedOut = 0)\n" +
          " AND pa.PlayerID NOT IN (SELECT PlayerID FROM keepers) ";
    }

    sql += " ) p_all ";

    return sql;
  }

  private String getBattingWizardClauses() {
    StringBuilder builder = new StringBuilder();
    for (Position position : Position.BATTING_POSITIONS) {
      if (builder.length() > 0) {
        builder.append(", ");
      }
      builder.append("  (select round(Rating, 3)\n" +
              "   from wizardratings\n" +
              "   where projectionRow = projectionsbatting.ID\n" +
              "   and batting = 1 \n" +
              "   and Position = '")
          .append(position.getShortName())
          .append("'\n" +
              "  ) as Wizard")
          .append(position.getShortName())
          .append(" \n");
    }
    return builder.toString();
  }

  private String getNullBattingWizardClauses() {
    StringBuilder builder = new StringBuilder();
    for (Position position : Position.BATTING_POSITIONS) {
      if (builder.length() > 0) {
        builder.append(", ");
      }
      builder.append("  NULL as Wizard")
          .append(position.getShortName())
          .append(" \n");
    }
    return builder.toString();
  }

  private String addFilters(String sql, TableSpec tableSpec) {
    List<String> filters = new ArrayList<>();

    filters.add("(AB > 0 or INN > 0)");

    addTableSpecFilter(filters, tableSpec);

    if (filters.isEmpty()) {
      return sql;
    } else {
      return sql + " where " + Joiner.on(" and ").join(filters) + " ";
    }
  }

  private void addTableSpecFilter(List<String> filters, TableSpec tableSpec) {
    String sourceFilter = tableSpec.getPlayerDataSet().getDisplayName();
    if (sourceFilter != null) {
      filters.add("Source = '" + sourceFilter + "' ");
    }
  }

  private void addDataSetFilter(List<String> filters, PlayerDataSet playerDataSet) {
    if (playerDataSet == null) {
      filters.add("Source = '" + PlayerDataSet.CBSSPORTS.getDisplayName() + "' ");
    } else {
      filters.add("Source = '" + playerDataSet.getDisplayName() + "' ");
    }
  }


  private String addOrdering(TableSpec tableSpec, String sql) {
    PlayerColumn sortCol = tableSpec.getSortCol();
    String sortColumnName;
    String sortColumnDirection;
    if (sortCol != null) {
      sortColumnName = sortCol.getColumnName();
      sortColumnDirection = (tableSpec.isAscending() ? "asc " : "desc ");
    } else {
      throw new IllegalStateException("Expected tableSpec to always have non-null sort column.");
    }

    sql += " order by case when " + sortColumnName + " is null then 1 else 0 end, "
        + sortColumnName + " " + sortColumnDirection + " ";
    return sql;
  }


  // Open Positions

  private String createFilterStringFromPositions(Set<Position> openPositions) {
    // needs no filter if all positions are open, {P, DH} are open, or all positions are full (reserve time!)
    if (openPositions.isEmpty() || hasAllOpenPositions(openPositions)) {
      return null;
    } else {
      String joined = Joiner.on(',').join(Iterables.transform(openPositions, new Function<Position, String>() {
        @Override
        public String apply(Position input) {
          return "'" + input.getShortName() + "'";
        }
      }));
      return " in (" + joined + ") ";
    }
  }

  private boolean hasAllOpenPositions(Set<Position> openPositions) {
    return openPositions.size() == Position.REAL_POSITIONS.size()
        || (openPositions.contains(DH) && openPositions.contains(P));
  }




  // Player Rank

  @Override
  public void changePlayerRank(ChangePlayerRankRequest request) throws DataSourceException {
    if (teamTokens.containsKey(request.getTeamToken())) {
      TeamId teamID = teamDataSource.getTeamIdByDraftOrder(teamTokens.get(request.getTeamToken()));
      long playerId = request.getPlayerId();
      int prevRank = request.getPrevRank();
      int newRank = request.getNewRank();

      logger.info("Change player rank for team " + teamID
          + " player " + playerId + " from rank " + prevRank + " to new rank " + newRank);

      shiftInBetweenRanks(teamID, prevRank, newRank);
      updatePlayerRank(teamID, newRank, playerId);
      cache.clear();
    }
  }

  private void shiftInBetweenRanks(TeamId teamID, int prevRank, int newRank) {
    String newRankForInbetween = "Rank-1";
    int lesserRank = prevRank+1;
    int greaterRank = newRank;

    if (prevRank > newRank) {
      newRankForInbetween = "Rank+1";
      lesserRank = newRank;
      greaterRank = prevRank-1;
    }

    String sql = "UPDATE customrankings SET Rank = " + newRankForInbetween +
        " WHERE TeamID = ? AND Rank BETWEEN ? AND ?";
    Statement statement = null;
    try {
      statement = prepareStatementUpdate(sql, teamID, lesserRank, greaterRank);
    } catch (SQLException e) {
      logger.log(SEVERE, "Unable to shift ranks for intermediate players!", e);
    } finally {
      close(statement);
    }
  }

  private void updatePlayerRank(TeamId teamID, int newRank, long playerID) {
    String sql = "UPDATE customrankings SET Rank = ? WHERE TeamID = ? AND PlayerID = ?";
    Statement statement = null;
    try {
      statement = prepareStatementUpdate(sql, newRank, teamID, playerID);
    } catch (SQLException e) {
      logger.log(SEVERE, "Unable to change rank for player!", e);
    } finally {
      close(statement);
    }
  }



  @Override
  public void populateQueueEntry(QueueEntry queueEntry) throws DataSourceException {
    String sql = "select PlayerString,Eligibility " +
        "from players " +
        "where ID = " + queueEntry.getPlayerId();

    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(sql);
      resultSet.next();
      queueEntry.setPlayerName(resultSet.getString("PlayerString"));
      queueEntry.setEligibilities(
          RosterUtil.splitEligibilities(resultSet.getString("Eligibility")));
    } catch (SQLException e) {
      throw new DataSourceException(e);
    } finally {
      close(resultSet);
    }
  }

  @Override
  public void populateDraftPick(DraftPick draftPick) throws DataSourceException {
    String sql = "select FirstName,LastName,Eligibility " +
        "from players " +
        "where ID = " + draftPick.getPlayerId();

    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(sql);
      resultSet.next();
      draftPick.setPlayerName(
          resultSet.getString("FirstName") + " " + resultSet.getString("LastName"));
      draftPick.setEligibilities(
          RosterUtil.splitEligibilities(resultSet.getString("Eligibility")));
    } catch (SQLException e) {
      throw new DataSourceException(e);
    } finally {
      close(resultSet);
    }
  }

  @Override
  public void postDraftPick(DraftPick draftPick, DraftStatus status) throws DataSourceException {
    int overallPick = status.getPicks().size();
    int round = (overallPick - 1) / numTeams + 1;
    int pick = ((overallPick-1) % numTeams) + 1;

    long playerID = draftPick.getPlayerId();
    TeamId teamID = teamDataSource.getTeamIdByDraftOrder(new TeamDraftOrder(draftPick.getTeam()));

    String draftPosition = "'" + draftPick.getEligibilities().get(0) + "'";
    String sql = "INSERT INTO DraftResults (Round, Pick, PlayerID, BackedOut, OverallPick, TeamID, DraftPos, Keeper) " +
        "VALUES (" + round + ", " + pick + ", " + playerID + ", 0, " + overallPick + ", " + teamID +
          ", " + draftPosition + ", " + (draftPick.isKeeper() ? "1" : "0") + ")";

    Statement statement = null;
    try {
      statement = executeUpdate(sql);
    } catch (SQLException e) {
      throw new DataSourceException(e);
    } finally {
      close(statement);
    }
  }

  @Override
  public void backOutLastDraftPick(int pickToRemove) throws DataSourceException {
    String sql = "UPDATE draftresults SET BackedOut = 1 WHERE OverallPick = " + pickToRemove;

    Statement statement = null;
    try {
      statement = executeUpdate(sql);
    } catch (SQLException e) {
      throw new DataSourceException(e);
    } finally {
      close(statement);
    }
  }

  @Override
  public void populateDraftStatus(DraftStatus status) throws DataSourceException {
    String sql = "SELECT * from draftresultsload "
        + "ORDER BY Round, Pick";
    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(sql);
      while (resultSet.next()) {
        DraftPick pick = beanFactory.createDraftPick().as();
        pick.setPlayerId(resultSet.getInt("PlayerID"));
        pick.setPlayerName(resultSet.getString("PlayerName"));
        pick.setEligibilities(
            RosterUtil.splitEligibilities(resultSet.getString("Eligibility")));
        pick.setTeam(resultSet.getInt("DraftOrder"));
        pick.setKeeper(resultSet.getBoolean("Keeper"));
        status.getPicks().add(pick);
      }
    } catch (SQLException e) {
      throw new DataSourceException(e);
    } finally {
      close(resultSet);
    }
  }

  @Override
  public void copyTableSpecToCustom(CopyAllPlayerRanksRequest request) throws DataSourceException {
    final TeamId teamID = teamDataSource.getTeamIdByDraftOrder(teamTokens.get(request.getTeamToken()));
    TableSpec tableSpec = request.getTableSpec();

    if (tableSpec.getSortCol() == PlayerColumn.MYRANK) {
      logger.log(SEVERE, "Cannot set MyRank to MyRank column.");
      return;
    }

    logger.log(INFO, "Request from Team " + teamID + " to copy player ranks from " + tableSpec.getPlayerDataSet().getDisplayName()
        + ", " + tableSpec.getSortCol().getShortName() + ".");

    try {
      prepareTmpTable(teamID);

      logger.log(FINE, "Cleared temp table for " + teamID);

      String sql = "INSERT INTO tmp_rankings (TeamID, PlayerID) \n" +
          " SELECT " + teamID + ", PlayerID \n" +
          " FROM ";
      sql = getFromJoins(teamID, sql, null, false, false);

      List<String> filters = new ArrayList<>();
      addTableSpecFilter(filters, tableSpec);

      if (!filters.isEmpty()) {
        sql += " where " + Joiner.on(" and ").join(filters) + " ";
      }

      sql = addOrdering(tableSpec, sql);

      Statement statement = executeUpdate(sql);
      close(statement);

      logger.log(FINE, "Executed big insert for " + teamID);

      sql = "select min(rank) as lower_bound, max(rank) as upper_bound \n" +
          "from tmp_rankings \n" +
          "where teamID = " + teamID;
      ResultSet resultSet = executeQuery(sql);
      resultSet.next();

      logger.log(FINE, "Executed bounds query for " + teamID);

      int lowerBound = resultSet.getInt("lower_bound");
      int upperBound = resultSet.getInt("upper_bound");

      logger.log(FINE, "Got bounds off of result set.");

      close(resultSet);

      logger.log(FINE, "Closed bounds connections.");

      int offset = lowerBound - 1;

      sql = "update customrankings \n" +
          "set Rank = " + (upperBound - offset + 1) + "\n" +
          "where teamid = " + teamID;
      statement = executeUpdate(sql);
      close(statement);

      logger.log(FINE, "Executed base update for " + teamID);

      sql = "update customrankings cr\n" +
          "inner join tmp_rankings tr\n" +
          " on (cr.PlayerID = tr.PlayerID and cr.TeamID = tr.TeamID)\n" +
          "set cr.Rank = tr.Rank - " + offset + " \n" +
          "where tr.teamID = " + teamID;
      statement = executeUpdate(sql);
      close(statement);
    } catch (SQLException e) {
      throw new DataSourceException(e);
    }

    logger.log(FINE, "Executed big update for " + teamID);
    cache.clear();
  }


  private void prepareTmpTable(TeamId teamID) throws SQLException {
    executeUpdate("delete from tmp_rankings where teamID = " + teamID);
  }


  @Override
  public GraphsData getGraphsData(TeamDraftOrder myTeam) throws DataSourceException {
    TeamId teamId = teamDataSource.getTeamIdByDraftOrder(myTeam);
    String sql;
    if (Scoring.CATEGORIES) {
      sql = "select * from teamscoringwithzeroes where source = 'CBSSports'";
    } else {
      sql = "select TeamID, sum(p_all.Wizard) as pitching, sum(p_all.Wizard) as batting from ";
      sql = getFromJoins(teamId, sql, null, false, false);
      sql += " inner join draftresults on p_all.PlayerID = draftresults.PlayerID group by draftresults.TeamID";
    }

    GraphsData graphsData = beanFactory.createGraphsData().as();
    Map<PlayerColumn, Float> myValues = new HashMap<>();
    graphsData.setMyValues(myValues);
    Map<PlayerColumn, Float> avgValues = new HashMap<>();
    graphsData.setAvgValues(avgValues);
    Map<String, Float> teamPitchingValues = new HashMap<>();
    graphsData.setTeamPitchingValues(teamPitchingValues);
    Map<String, Float> teamBattingValues = new HashMap<>();
    graphsData.setTeamBattingValues(teamBattingValues);

    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(sql);
      while (resultSet.next()) {
        int resultTeam = resultSet.getInt("TeamID");
        if (Scoring.CATEGORIES) {
          for (PlayerColumn graphStat : GraphsData.GRAPH_STATS) {
            float value = resultSet.getFloat(graphStat.getColumnName());
            if (resultTeam == teamId.get()) {
              myValues.put(graphStat, value);
            }
            if (!avgValues.containsKey(graphStat)) {
              avgValues.put(graphStat, 0f);
            }
            avgValues.put(graphStat, avgValues.get(graphStat) + (value / numTeams));
          }
        } else {
          teamPitchingValues.put(Integer.toString(resultTeam),
              resultSet.getFloat("pitching"));
          teamBattingValues.put(Integer.toString(resultTeam),
              resultSet.getFloat("batting"));
        }
      }
    } catch (SQLException e) {
      throw new DataSourceException(e);
    } finally {
      close(resultSet);
    }

    return graphsData;
  }


  // DB utility methods

  private ResultSet executeQuery(String sql) throws SQLException {
    Statement statement = db.getConnection().createStatement();
    return statement.executeQuery(sql);
  }

  private Statement executeUpdate(String sql) throws SQLException {
    Statement statement = db.getConnection().createStatement();
    statement.executeUpdate(sql);
    return statement;
  }


  protected Statement prepareStatementUpdate(String sql, Object... params) throws SQLException {
    PreparedStatement preparedStatement = prepareStatement(sql, Lists.newArrayList(params));

    preparedStatement.executeUpdate();
    return preparedStatement;
  }

  @SuppressWarnings("unchecked")
  private PreparedStatement prepareStatement(String sql, List<Object> params) throws SQLException {
    PreparedStatement preparedStatement = db.getConnection().prepareStatement(sql);

    int i = 1;
    for (Object param : params) {
      if (param instanceof String) {
        preparedStatement.setString(i, (String) param);
      } else if (param instanceof Integer) {
        preparedStatement.setInt(i, (Integer) param);
      } else if (param instanceof Long) {
        preparedStatement.setLong(i, (Long) param);
      } else if (param instanceof IntWrapper) {
        preparedStatement.setInt(i, ((IntWrapper) param).get());
      } else {
        throw new RuntimeException("Unknown type of param: " + param.getClass());
      }
      i++;
    }
    return preparedStatement;
  }

  private static void close(ResultSet resultSet) {
    try {
      if (resultSet == null) {
        return;
      }
      Statement statement = resultSet.getStatement();
      Connection connection = statement.getConnection();
      resultSet.close();
      statement.close();
      connection.close();
    } catch (SQLException e) {
      logger.log(SEVERE, "Unable to close SQL connection after use.", e);
    }
  }

  private static void close(Statement statement) {
    try {
      if (statement == null) {
        return;
      }
      Connection connection = statement.getConnection();
      statement.close();
      connection.close();
    } catch (SQLException e) {
      logger.log(SEVERE, "Unable to close SQL connection after use.", e);
    }
  }

  private static String getKey(TableSpec tableSpec, TeamId teamId) {
    return tableSpec.getPlayerDataSet().ordinal() + ""
        + teamId.get();
  }
}
