package com.mayhew3.drafttower.client;

import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

/**
 * Fake scheduler for testing.  Allows tests to advance scheduler synchronously.
 */
@Singleton
public class TestScheduler implements SchedulerWrapper {

  private final List<Runnable> tasks = new ArrayList<>();

  @Override
  public void schedule(Runnable runnable, int delayMs) {
    tasks.add(runnable);
  }

  public void advance() {
    tasks.remove(0).run();
  }

  public void flush() {
    while (!tasks.isEmpty()) {
      advance();
    }
  }
}