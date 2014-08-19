package com.mayhew3.drafttower.client.serverrpc;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;

/**
 * Interface wrapping {@link Scheduler} that can be mocked for tests.
 */
class SchedulerWrapper {
  public void schedule(final Runnable runnable, int delayMs) {
    Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
      @Override
      public boolean execute() {
        runnable.run();
        return false;
      }
    }, delayMs);
  }
}