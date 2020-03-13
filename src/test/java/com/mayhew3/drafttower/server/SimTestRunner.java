package com.mayhew3.drafttower.server;

import com.google.common.base.Strings;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.DraftStatus;
import org.junit.Assert;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * Class description...
 */
public class SimTestRunner {

  private static final int CYCLES_TIMER_EXPIRE = 10;

  @Inject @Nullable DataSource dataSource;
  @Inject DraftStatus draftStatus;
  @Inject DraftTimer draftTimer;
  @Inject PlayerDataSource playerDataSource;
  @Inject PlayerDataProvider playerDataProvider;
  @Inject PickProbabilityPredictor pickProbabilityPredictor;
  @Inject Lock lock;

  private boolean realDb;

  @Inject
  public SimTestRunner() {}

  void run(List<SimulatedClient> clients, final List<Throwable> clientExceptions, final double timeoutMs) {
    final long startTime = System.currentTimeMillis();
    List<Thread> threads = new ArrayList<>();
    try {
      for (final SimulatedClient client : clients) {
        Thread clientThread = new Thread(Strings.nullToEmpty(client.getUsername())) {
          @Override
          public void run() {
            Random random = new Random();
            while (!draftStatus.isOver()
                && System.currentTimeMillis() - startTime < timeoutMs) {
              try {
                client.performAction();
              } catch (Throwable t) {
                clientExceptions.add(t);
              }
              try {
                sleep(random.nextInt(50) + 5);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            }
          }
        };
        threads.add(clientThread);
      }
      for (Thread thread : threads) {
        thread.start();
      }
      int lastNumPicks = 0;
      int numCyclesThisPick = 0;
      while (!draftStatus.isOver()
          && System.currentTimeMillis() - startTime < timeoutMs) {
        verify(clients, clientExceptions);
        if (draftStatus.getCurrentPickDeadline() > 0 && !draftStatus.isPaused()) {
          if (lastNumPicks == draftStatus.getPicks().size()) {
            numCyclesThisPick++;
            if (numCyclesThisPick > CYCLES_TIMER_EXPIRE) {
              ((TestDraftTimer) draftTimer).expire();
            }
          } else {
            lastNumPicks = draftStatus.getPicks().size();
            numCyclesThisPick = 0;
          }
        }
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }

      Assert.assertTrue("Test timed out.", draftStatus.isOver());

      verify(clients, clientExceptions);
    } catch (RuntimeException | AssertionError e) {
      endDraftAndWaitForThreads(threads);
      throw e;
    }
    endDraftAndWaitForThreads(threads);
  }

  private void endDraftAndWaitForThreads(List<Thread> threads) {
    draftStatus.setOver(true);
    boolean threadsRunning = true;
    while (threadsRunning) {
      threadsRunning = false;
      for (Thread thread : threads) {
        if (thread.isAlive()) {
          threadsRunning = true;
        }
      }
      if (threadsRunning) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e1) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  private void verify(List<SimulatedClient> clients, List<Throwable> clientExceptions) {
    verifyDraftStatus();
    if (!clientExceptions.isEmpty()) {
      clientExceptions.get(0).printStackTrace();
      Assert.fail("Clients had exceptions: " + clientExceptions.get(0).getMessage());
    }
    for (SimulatedClient client : clients) {
      client.verify();
    }
  }

  private void verifyDraftStatus() {
    try (Lock ignored = lock.lock()) {
      Set<Long> selectedPlayerIds = new HashSet<>();
      List<DraftPick> picks = draftStatus.getPicks();
      for (int i = 0; i < picks.size(); i++) {
        DraftPick draftPick = picks.get(i);
        Assert.assertFalse("Duplicate player selected: " + draftPick.getPlayerId(),
            selectedPlayerIds.contains(draftPick.getPlayerId()));
        selectedPlayerIds.add(draftPick.getPlayerId());
        Assert.assertEquals("Wrong team on pick " + i,
            draftPick.getTeam(),
            i % 10 + 1);
      }
      if (playerDataSource instanceof TestPlayerDataSource) {
        Assert.assertEquals("Wrong number of unavailable players",
            draftStatus.getPicks().size(),
            ((TestPlayerDataSource) playerDataSource).getAllPlayers().size()
                - ((TestPlayerDataSource) playerDataSource).getAvailablePlayers().size());
      }
      Assert.assertEquals("Wrong current team",
          draftStatus.getCurrentTeam(),
          draftStatus.getPicks().size() % 10 + 1);
      Map<Long, Float> predictions = pickProbabilityPredictor.getTeamPredictions(
          new TeamDraftOrder(draftStatus.getCurrentTeam()));
      for (Long predictionPlayer : predictions.keySet()) {
        Assert.assertFalse("Predictions for current team include selected player " + predictionPlayer,
            selectedPlayerIds.contains(predictionPlayer));
      }
    }
  }

  public void tearDown() throws DataSourceException, SQLException {
    draftStatus.getConnectedTeams().clear();
    draftStatus.setCurrentPickDeadline(0);
    draftStatus.setCurrentTeam(1);
    draftStatus.getNextPickKeeperTeams().clear();
    draftStatus.setOver(false);
    draftStatus.setPaused(false);
    draftStatus.getPicks().clear();
    draftStatus.getRobotTeams().clear();
    draftStatus.setSerialId(0);
    if (realDb) {
      playerDataProvider.reset();
      TestServerDBModule.resetDB(dataSource);
    } else {
      playerDataSource.resetDraft();
      playerDataProvider.reset();
    }
    pickProbabilityPredictor.reset();
  }

  public void setRealDb(boolean realDb) {
    this.realDb = realDb;
  }
}