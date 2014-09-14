package com.mayhew3.drafttower.client;

/**
 * Interface description...
 */
public interface SchedulerWrapper {
  void schedule(Runnable runnable, int delayMs);
  void scheduleRepeating(Runnable runnable, int periodMs);
}