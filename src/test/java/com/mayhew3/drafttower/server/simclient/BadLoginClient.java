package com.mayhew3.drafttower.server.simclient;

import com.mayhew3.drafttower.server.SimulatedClient;
import org.junit.Assert;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Simulated client which keeps trying to log in with a bad password.
 */
public class BadLoginClient extends SimulatedClient {

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
  }
}