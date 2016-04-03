package com.mayhew3.drafttower.client;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Fake scheduler for testing.  Allows tests to advance scheduler synchronously.
 */
@Singleton
public class TestScheduler implements SchedulerWrapper {

  private static final int DELAY_THRESHOLD_MS = 60000;

  private final List<Runnable> immediateTasks = new ArrayList<>();
  private final List<Runnable> finallyTasks = new ArrayList<>();
  private final List<Runnable> tasks = new ArrayList<>();
  private final List<Runnable> repeatingTasks = new ArrayList<>();

  @Inject
  public TestScheduler() {}

  @Override
  public void schedule(Runnable runnable, int delayMs) {
    if (delayMs < DELAY_THRESHOLD_MS) {
      tasks.add(runnable);
    }
  }

  @Override
  public void scheduleImmediate(Runnable runnable) {
    immediateTasks.add(runnable);
  }

  @Override
  public void scheduleLast(Runnable runnable) {
    finallyTasks.add(runnable);
  }

  @Override
  public void scheduleRepeating(Runnable runnable, int periodMs) {
    repeatingTasks.add(runnable);
  }

  public void runNextScheduled() {
    if (!immediateTasks.isEmpty()) {
      immediateTasks.remove(0).run();
    } else if (!finallyTasks.isEmpty()) {
      finallyTasks.remove(0).run();
    } else {
      tasks.remove(0).run();
    }
  }

  public void flush() {
    while (!immediateTasks.isEmpty()
        || !finallyTasks.isEmpty()
        || !tasks.isEmpty()) {
      runNextScheduled();
    }
  }

  public void runRepeating() {
    for (Runnable repeatingTask : repeatingTasks) {
      repeatingTask.run();
    }
  }
}