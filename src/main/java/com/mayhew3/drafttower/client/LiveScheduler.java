package com.mayhew3.drafttower.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import javax.inject.Inject;

/**
 * Interface wrapping {@link Scheduler} that can be mocked for tests.
 */
public class LiveScheduler implements SchedulerWrapper {

  @Inject
  public LiveScheduler() {}

  @Override
  public void schedule(final Runnable runnable, int delayMs) {
    Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
      @Override
      public boolean execute() {
        runnable.run();
        return false;
      }
    }, delayMs);
  }

  @Override
  public void scheduleImmediate(final Runnable runnable) {
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        runnable.run();
      }
    });
  }

  @Override
  public void scheduleLast(final Runnable runnable) {
    Scheduler.get().scheduleFinally(new ScheduledCommand() {
      @Override
      public void execute() {
        runnable.run();
      }
    });
  }

  @Override
  public void scheduleRepeating(final Runnable runnable, int periodMs) {
    Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {
      @Override
      public boolean execute() {
        runnable.run();
        return true;
      }
    }, periodMs);
  }
}