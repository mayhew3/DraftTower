package com.mayhew3.drafttower.server;

import com.google.guiceberry.junit4.GuiceBerryRule;
import com.mayhew3.drafttower.server.simclient.FlakyClient;
import com.mayhew3.drafttower.server.simclient.PickNextPlayerClient;
import com.mayhew3.drafttower.shared.DraftStatus;
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

  @Rule
  public final GuiceBerryRule guiceBerry =
      new GuiceBerryRule(SimTestGuiceBerryEnv.class);

  @Inject private Provider<PickNextPlayerClient> clientProvider;
  @Inject private DraftTowerWebSocketServlet webSocketServlet;
  @Inject private ChangePlayerRankServlet changePlayerRankServlet;
  @Inject private CopyAllPlayerRanksServlet copyAllPlayerRanksServlet;
  @Inject private GraphsServlet graphsServlet;
  @Inject private LoginServlet loginServlet;
  @Inject private QueueServlet queueServlet;
  @Inject private SetAutoPickWizardServlet setAutoPickWizardServlet;
  @Inject private UnclaimedPlayerLookupServlet unclaimedPlayerLookupServlet;
  @Inject private DraftStatus draftStatus;

  private final List<SimulatedClient> clients = new ArrayList<>();
  private final List<Throwable> clientExceptions = new ArrayList<>();

  @Test
  public void allPickNextPlayer() {
    for (int i = 1; i <= 10; i++) {
      PickNextPlayerClient client = clientProvider.get();
      client.setUsername(Integer.toString(i));
      clients.add(client);
    }
    run();
  }

  @Test
  public void duplicateTeams() {
    for (int i = 1; i <= 10; i++) {
      PickNextPlayerClient client = clientProvider.get();
      client.setUsername(Integer.toString(i));
      clients.add(client);
      client = clientProvider.get();
      client.setUsername(Integer.toString(i));
      clients.add(client);
    }
    run();
  }

  @Test
  public void flakyConnections() {
    for (int i = 1; i <= 10; i++) {
      PickNextPlayerClient client = clientProvider.get();
      client.setUsername(Integer.toString(i));
      clients.add(new FlakyClient(client));
    }
    run();
  }

  protected void run() {
    final long startTime = System.currentTimeMillis();
    List<Thread> threads = new ArrayList<>();
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
    while (!draftStatus.isOver()
        && System.currentTimeMillis() - startTime < TIMEOUT_MS) {
      try {
        Thread.sleep(50);
        // TODO: server-side state verification.
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    Assert.assertTrue("Test timed out.", draftStatus.isOver());

    if (!clientExceptions.isEmpty()) {
      clientExceptions.get(0).printStackTrace();
      Assert.fail("Clients had exceptions.");
    }
    for (SimulatedClient client : clients) {
      client.verify();
    }
  }

}