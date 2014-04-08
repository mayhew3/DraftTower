package com.mayhew3.drafttower.server;

/**
 * Interface for a reentrant lock.
 */
public interface Lock extends AutoCloseable {
  Lock lock();
  @Override void close();
}