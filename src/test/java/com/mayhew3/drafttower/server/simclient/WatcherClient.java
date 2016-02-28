package com.mayhew3.drafttower.server.simclient;

import com.mayhew3.drafttower.server.SimulatedClient;

import java.io.IOException;

/**
 * Simulated client which doesn't log in (guest mode).
 */
public class WatcherClient extends SimulatedClient {

  @Override
  public void performAction() {
    if (connection == null) {
      try {
        connect();
      } catch (IOException e) {
        exceptions.add(e);
      }
    }
  }
}