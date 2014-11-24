package com.mayhew3.drafttower.server;

import com.google.common.testing.TearDownAccepter;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.mayhew3.drafttower.server.simclient.BadLoginClient;
import com.mayhew3.drafttower.server.simclient.FlakyClient;
import com.mayhew3.drafttower.server.simclient.PickNextPlayerClient;
import com.mayhew3.drafttower.shared.DraftStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Test harness for running draft with simulated clients.
 */
public class SimTest {

  private static final long TIMEOUT_MS = 300000;
  private static final int CYCLES_TIMER_EXPIRE = 10;

  @Rule
  public final GuiceBerryRule guiceBerry =
      new GuiceBerryRule(SimTestGuiceBerryEnv.class);

  @Inject private Provider<PickNextPlayerClient> pickNextPlayerClientProvider;
  @Inject private Provider<BadLoginClient> badLoginClientProvider;

  @Inject private DraftTowerWebSocketServlet webSocketServlet;
  @Inject private ChangePlayerRankServlet changePlayerRankServlet;
  @Inject private CopyAllPlayerRanksServlet copyAllPlayerRanksServlet;
  @Inject private GraphsServlet graphsServlet;
  @Inject private LoginServlet loginServlet;
  @Inject private QueueServlet queueServlet;
  @Inject private SetAutoPickWizardServlet setAutoPickWizardServlet;
  @Inject private UnclaimedPlayerLookupServlet unclaimedPlayerLookupServlet;
  @Inject private DraftStatus draftStatus;
  @Inject private DraftTimer draftTimer;

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
                && System.currentTimeMillis() - startTime < TIMEOUT_MS) {
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
          && System.currentTimeMillis() - startTime < TIMEOUT_MS) {
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
    // TODO: server-side state verification.
    if (!clientExceptions.isEmpty()) {
      clientExceptions.get(0).printStackTrace();
      Assert.fail("Clients had exceptions.");
    }
    for (SimulatedClient client : clients) {
      client.verify();
    }
  }

}