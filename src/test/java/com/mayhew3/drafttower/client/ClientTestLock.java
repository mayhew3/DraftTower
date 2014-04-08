package com.mayhew3.drafttower.client;

import com.mayhew3.drafttower.server.Lock;

/**
 * Implementation of {@link Lock} for client tests - no need to actually lock.
 */
public class ClientTestLock implements Lock{
  @Override
  public Lock lock() {
    return this;
  }

  @Override
  public void close() {
  }
}