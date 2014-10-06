package com.mayhew3.drafttower.client;

import com.google.gwt.core.client.impl.SchedulerImpl;

/**
 * Override of {@link SchedulerImpl} that hijacks scheduled commands
 * and redirects them to the {@link SchedulerWrapper}, for when GWT framework
 * classes call Scheduler.get() directly.
 */
public class TestSchedulerImpl extends SchedulerImpl {

  private SchedulerWrapper scheduler;

  @Override
  public void scheduleDeferred(final ScheduledCommand cmd) {
    scheduler.scheduleImmediate(new Runnable() {
      @Override
      public void run() {
        cmd.execute();
      }
    });
  }

  @Override
  public void scheduleFinally(final RepeatingCommand cmd) {
    scheduler.scheduleLast(new Runnable() {
      @Override
      public void run() {
        cmd.execute();
      }
    });
  }

  @Override
  public void scheduleFinally(final ScheduledCommand cmd) {
    scheduler.scheduleLast(new Runnable() {
      @Override
      public void run() {
        cmd.execute();
      }
    });
  }

  @Override
  public void scheduleFixedDelay(final RepeatingCommand cmd, int delayMs) {
    scheduler.schedule(new Runnable() {
      @Override
      public void run() {
        cmd.execute();
      }
    }, delayMs);
  }

  @Override
  public void scheduleFixedPeriod(final RepeatingCommand cmd, int delayMs) {
    scheduler.scheduleRepeating(new Runnable() {
      @Override
      public void run() {
        cmd.execute();
      }
    }, delayMs);
  }

  public void setScheduler(SchedulerWrapper scheduler) {
    this.scheduler = scheduler;
  }
}