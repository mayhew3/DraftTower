package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.server.simclient.BadLoginClient;
import com.mayhew3.drafttower.server.simclient.FuzzClient;
import com.mayhew3.drafttower.server.simclient.PickNextPlayerClient;
import com.mayhew3.drafttower.server.simclient.WatcherClient;

/**
 * Dependency component for simulation tests.
 */
public interface SimTestComponent {
  SimTestRunner simTestRunner();

  PickNextPlayerClient pickNextPlayerClient();
  WatcherClient watcherClient();
  BadLoginClient badLoginClient();
  FuzzClient fuzzClient();
}