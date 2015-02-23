package com.mayhew3.drafttower.server.simclient;

import com.mayhew3.drafttower.server.SimulatedClient;
import com.mayhew3.drafttower.shared.DraftCommand.Command;
import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.PlayerDataSet;
import com.mayhew3.drafttower.shared.TableSpec;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Random;

/**
 * Client which performs various actions at random.
 */
public class FuzzClient extends SimulatedClient {

  private final Random random = new Random();

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
        switch (random.nextInt(35)) {
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
            sendDraftCommand(Command.DO_PICK, randomPlayer());
            break;
          case 31:
            if (commissionerTeam.equals(username)) {
              sendDraftCommand(draftStatus.isPaused()
                  ? Command.RESUME : Command.PAUSE, null);
            } else {
              getUnclaimedPlayers(randomTableSpec());
            }
            break;
          case 32:
            if (commissionerTeam.equals(username) && random.nextBoolean()) {
              sendDraftCommand(Command.BACK_OUT, null);
            } else {
              getUnclaimedPlayers(randomTableSpec());
            }
            break;
          case 33:
            if (commissionerTeam.equals(username)) {
              sendDraftCommand(Command.FORCE_PICK, random.nextBoolean()
                  ? randomPlayer() : null);
            } else {
              getUnclaimedPlayers(randomTableSpec());
            }
            break;
          case 34:
            sendDraftCommand(Command.WAKE_UP, null);
            break;
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