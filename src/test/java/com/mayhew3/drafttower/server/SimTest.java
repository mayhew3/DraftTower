package com.mayhew3.drafttower.server;

import com.google.common.testing.TearDownAccepter;
import com.google.inject.BindingAnnotation;
import com.mayhew3.drafttower.server.simclient.BadLoginClient;
import com.mayhew3.drafttower.server.simclient.FlakyClient;
import com.mayhew3.drafttower.server.simclient.FuzzClient;
import com.mayhew3.drafttower.server.simclient.PickNextPlayerClient;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.DraftStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Test harness for running draft with simulated clients.
 */
public abstract class SimTest {

  @BindingAnnotation
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  public static @interface CommissionerTeam {}

  private static final int CYCLES_TIMER_EXPIRE = 10;

  @Inject private Provider<PickNextPlayerClient> pickNextPlayerClientProvider;
  @Inject private Provider<BadLoginClient> badLoginClientProvider;
  @Inject private Provider<FuzzClient> fuzzClientProvider;

  @Inject private DraftTowerWebSocketServlet webSocketServlet;
  @Inject private ChangePlayerRankServlet changePlayerRankServlet;
  @Inject private CopyAllPlayerRanksServlet copyAllPlayerRanksServlet;
  @Inject private GraphsServlet graphsServlet;
  @Inject private LoginServlet loginServlet;
  @Inject private QueueServlet queueServlet;
  @Inject private SetAutoPickWizardServlet setAutoPickWizardServlet;
  @Inject private SetCloserLimitServlet setCloserLimitServlet;
  @Inject private UnclaimedPlayerLookupServlet unclaimedPlayerLookupServlet;

  @Inject private DraftStatus draftStatus;
  @Inject private DraftTimer draftTimer;
  @Inject private PlayerDataSource playerDataSource;
  @Inject private PickProbabilityPredictor pickProbabilityPredictor;
  @Inject private Lock lock;

  @Inject private TearDownAccepter tearDownAccepter;

  private final List<SimulatedClient> clients = new ArrayList<>();
  private final List<Throwable> clientExceptions = new ArrayList<>();

  @After
  public void resetClients() {
    clientExceptions.clear();
    for (SimulatedClient client : clients) {
      client.disconnect();
    }
    clients.clear();
  }

  @Test
  public void allPickNextPlayer() {
    for (int i = 1; i <= 10; i++) {
      PickNextPlayerClient client = pickNextPlayerClientProvider.get();
      client.setUsername(Integer.toString(i));
      clients.add(client);
    }
    run();
  }

  @Test
  public void duplicateTeams() {
    for (int i = 1; i <= 10; i++) {
      PickNextPlayerClient client = pickNextPlayerClientProvider.get();
      client.setUsername(Integer.toString(i));
      clients.add(client);
      client = pickNextPlayerClientProvider.get();
      client.setUsername(Integer.toString(i));
      clients.add(client);
    }
    run();
  }

  @Test
  public void flakyConnections() {
    for (int i = 1; i <= 10; i++) {
      PickNextPlayerClient client = pickNextPlayerClientProvider.get();
      client.setUsername(Integer.toString(i));
      clients.add(new FlakyClient(client));
    }
    run();
  }

  @Test
  public void persistentBadLogin() {
    for (int i = 1; i <= 9; i++) {
      PickNextPlayerClient client = pickNextPlayerClientProvider.get();
      client.setUsername(Integer.toString(i));
      clients.add(client);
    }
    BadLoginClient client = badLoginClientProvider.get();
    client.setUsername("10");
    clients.add(client);
    run();
  }

  @Test
  public void fuzzTest() {
    for (int i = 1; i <= 10; i++) {
      FuzzClient client = fuzzClientProvider.get();
      client.setUsername(Integer.toString(i));
      clients.add(client);
    }
    run();
  }

  protected void run() {
    final long startTime = System.currentTimeMillis();
    List<Thread> threads = new ArrayList<>();
    try {
      for (final SimulatedClient client : clients) {
        Thread clientThread = new Thread(client.getUsername()) {
          @Override
          public void run() {
            Random random = new Random();
            while (!draftStatus.isOver()
                && System.currentTimeMillis() - startTime < getTimeoutMs()) {
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
          && System.currentTimeMillis() - startTime < getTimeoutMs()) {
        verify();
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

      verify();
    } catch (RuntimeException | AssertionError e) {
      endDraftAndWaitForThreads(threads);
      throw e;
    }
    endDraftAndWaitForThreads(threads);
  }

  protected abstract double getTimeoutMs();

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

  private void verify() {
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
}