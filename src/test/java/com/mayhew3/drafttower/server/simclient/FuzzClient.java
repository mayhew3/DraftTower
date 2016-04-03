package com.mayhew3.drafttower.server.simclient;

import com.mayhew3.drafttower.server.SimulatedClient;
import com.mayhew3.drafttower.shared.DraftCommand.Command;
import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.PlayerDataSet;
import com.mayhew3.drafttower.shared.TableSpec;

import javax.inject.Inject;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Random;

/**
 * Client which performs various actions at random.
 */
public class FuzzClient extends SimulatedClient {

  private final Random random = new Random();

  @Inject
  public FuzzClient() {}

  @Override
  public void performAction() {
    if (connection == null) {
      try {
        login(false);
      } catch (ServletException | IOException e) {
        exceptions.add(e);
      }
    } else if (commissionerTeam.equals(username) && draftStatus.getCurrentPickDeadline() == 0) {
      sendDraftCommand(Command.START_DRAFT, null);
    } else {
      try {
        switch (random.nextInt(43)) {
          case 0:
          case 1:
          case 2:
            changePlayerRank(randomPlayer(),
                random.nextInt(220), random.nextInt(220));
            break;
          case 3:
          case 4:
          case 5:
            copyAllPlayerRanks(randomTableSpec());
            break;
          case 6:
          case 7:
          case 8:
            getGraphsData();
            break;
          case 9:
          case 10:
          case 11:
            getPlayerQueue();
            break;
          case 12:
          case 13:
          case 14:
            enqueue(randomPlayer(), random.nextInt(10));
            break;
          case 15:
          case 16:
          case 17:
            dequeue(randomPlayer());
            break;
          case 18:
          case 19:
          case 20:
            reorderQueue(randomPlayer(),
                random.nextInt(10));
            break;
          case 21:
          case 22:
          case 23:
            setAutoPickWizard(randomDataSet());
            break;
          case 24:
          case 25:
          case 26:
            getUnclaimedPlayers(randomTableSpec());
            break;
          case 27:
          case 28:
          case 29:
          case 30:
          case 31:
          case 32:
            sendDraftCommand(Command.DO_PICK, randomPlayer());
            break;
          case 33:
            if (commissionerTeam.equals(username)) {
              sendDraftCommand(draftStatus.isPaused()
                  ? Command.RESUME : Command.PAUSE, null);
            }
            break;
          case 34:
            if (commissionerTeam.equals(username) && random.nextBoolean()) {
              sendDraftCommand(Command.BACK_OUT, null);
            }
            break;
          case 35:
            if (commissionerTeam.equals(username)) {
              sendDraftCommand(Command.FORCE_PICK, random.nextBoolean()
                  ? randomPlayer() : null);
            }
            break;
          case 36:
            sendDraftCommand(Command.WAKE_UP, null);
            break;
          case 37:
          case 38:
          case 39:
            setCloserLimits(random.nextInt(10), random.nextInt(10));
            break;
          case 40:
          case 41:
          case 42:
            addOrRemoveFavoritePlayer(randomPlayer(), random.nextBoolean());
        }
      } catch (Exception e) {
        exceptions.add(e);
      }
    }
  }

  private long randomPlayer() {
    return players.get(random.nextInt(players.size())).getPlayerId();
  }

  private TableSpec randomTableSpec() {
    TableSpec tableSpec = beanFactory.createTableSpec().as();
    tableSpec.setAscending(random.nextBoolean());
    tableSpec.setPlayerDataSet(randomDataSet());
    tableSpec.setSortCol(randomColumn());
    return tableSpec;
  }

  private PlayerColumn randomColumn() {
    return PlayerColumn.valuesForScoring()[random.nextInt(PlayerColumn.valuesForScoring().length)];
  }

  private PlayerDataSet randomDataSet() {
    // TODO uncomment when we have projections for other data sets
//    return PlayerDataSet.values()[random.nextInt(PlayerDataSet.values().length)];
    return PlayerDataSet.CBSSPORTS;
  }
}