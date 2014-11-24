package com.mayhew3.drafttower.server.simclient;

import com.mayhew3.drafttower.server.SimulatedClient;

import java.util.Random;

/**
 * Wrapper for a {@link SimulatedClient} which simulates
 * a flaky connection.
 */
public class FlakyClient extends SimulatedClient {

  private final SimulatedClient delegate;
  private final Random random;

  public FlakyClient(SimulatedClient delegate) {
    this.delegate = delegate;
    random = new Random();
  }

  @Override
  public void setUsername(String username) {
    delegate.setUsername(username);
  }

  @Override
  public String getUsername() {
    return delegate.getUsername();
  }

  @Override
  public void performAction() {
    if (random.nextFloat() < 0.1 && connection != null) {
      disconnect();
    } else {
      delegate.performAction();
    }
  }

  @Override
  public void verify() {
    delegate.verify();
  }
}