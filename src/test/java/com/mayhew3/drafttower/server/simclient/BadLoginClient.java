package com.mayhew3.drafttower.server.simclient;

import com.mayhew3.drafttower.server.SimulatedClient;
import org.junit.Assert;

import javax.inject.Inject;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulated client which keeps trying to log in with a bad password.
 */
public class BadLoginClient extends SimulatedClient {

  private List<Exception> exceptions;

  @Inject
  public BadLoginClient() {
    super();
    exceptions = new ArrayList<>();
  }

  @Override
  public void performAction() {
    try {
      login(true);
    } catch (ServletException | IOException e) {
      exceptions.add(e);
    }
  }

  @Override
  public void verify() {
    Assert.assertNull(connection);
    if (!exceptions.isEmpty()) {
      exceptions.get(0).printStackTrace();
      Assert.fail("Client " + username + " failed to log in.");
    }
  }
}