package com.mayhew3.drafttower.client;

import com.mayhew3.drafttower.server.Lock;

import javax.inject.Inject;

/**
 * Implementation of {@link Lock} for client tests - no need to actually lock.
 */
public class ClientTestLock implements Lock{

  @Inject
  public ClientTestLock() {}

  @Override
  public Lock lock() {
    return this;
  }

  @Override
  public void close() {
  }
}