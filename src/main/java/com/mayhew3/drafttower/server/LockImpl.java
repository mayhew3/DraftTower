package com.mayhew3.drafttower.server;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of {@link Lock} which uses real reentrant lock.
 */
public class LockImpl implements Lock {

  private final ReentrantLock lock;

  public LockImpl() {
    lock = new ReentrantLock();
  }

  @Override
  public Lock lock() {
    lock.lock();
    return this;
  }

  @Override
  public void close() {
    lock.unlock();
  }
}