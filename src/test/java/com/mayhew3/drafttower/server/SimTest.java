package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.server.simclient.BadLoginClient;
import com.mayhew3.drafttower.server.simclient.FlakyClient;
import com.mayhew3.drafttower.server.simclient.FuzzClient;
import com.mayhew3.drafttower.server.simclient.PickNextPlayerClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Test harness for running draft with simulated clients.
 */
public abstract class SimTest {

  @Qualifier
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RUNTIME)
  public @interface CommissionerTeam {}

  private final List<SimulatedClient> clients = new ArrayList<>();
  private final List<Throwable> clientExceptions = new ArrayList<>();

  private SimTestComponent depsComponent;
  private SimTestRunner runner;

  @Before
  public void setup() {
    depsComponent = getDependencyComponent();
    runner = depsComponent.simTestRunner();
  }

  @After
  public void resetClients() throws DataSourceException, SQLException {
    clientExceptions.clear();
    for (SimulatedClient client : clients) {
      client.disconnect();
    }
    clients.clear();
    runner.tearDown();
  }

  @Test
  public void allPickNextPlayer() {
    for (int i = 1; i <= 10; i++) {
      PickNextPlayerClient client = depsComponent.pickNextPlayerClient();
      client.setUsername(Integer.toString(i));
      clients.add(client);
    }
    runner.run(clients, clientExceptions, getTimeoutMs());
  }

  @Test
  public void allPickNextPlayerPlusWatchers() {
    for (int i = 1; i <= 10; i++) {
      PickNextPlayerClient client = depsComponent.pickNextPlayerClient();
      client.setUsername(Integer.toString(i));
      clients.add(client);
    }
    for (int i = 0; i < 20; i++) {
      clients.add(depsComponent.watcherClient());
    }
    runner.run(clients, clientExceptions, getTimeoutMs());
  }

  @Test
  public void duplicateTeams() {
    for (int i = 1; i <= 10; i++) {
      PickNextPlayerClient client = depsComponent.pickNextPlayerClient();
      client.setUsername(Integer.toString(i));
      clients.add(client);
      client = depsComponent.pickNextPlayerClient();
      client.setUsername(Integer.toString(i));
      clients.add(client);
    }
    runner.run(clients, clientExceptions, getTimeoutMs());
  }

  @Test
  public void flakyConnections() {
    for (int i = 1; i <= 10; i++) {
      PickNextPlayerClient client = depsComponent.pickNextPlayerClient();
      client.setUsername(Integer.toString(i));
      clients.add(new FlakyClient(client));
    }
    runner.run(clients, clientExceptions, getTimeoutMs());
  }

  @Test
  public void persistentBadLogin() {
    for (int i = 1; i <= 9; i++) {
      PickNextPlayerClient client = depsComponent.pickNextPlayerClient();
      client.setUsername(Integer.toString(i));
      clients.add(client);
    }
    BadLoginClient client = depsComponent.badLoginClient();
    client.setUsername("10");
    clients.add(client);
    runner.run(clients, clientExceptions, getTimeoutMs());
  }

  @Test
  public void fuzzTest() {
    for (int i = 1; i <= 10; i++) {
      FuzzClient client = depsComponent.fuzzClient();
      client.setUsername(Integer.toString(i));
      clients.add(client);
    }
    runner.run(clients, clientExceptions, getTimeoutMs());
  }

  protected abstract SimTestComponent getDependencyComponent();

  protected abstract double getTimeoutMs();
}