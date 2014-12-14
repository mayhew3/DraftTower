package com.mayhew3.drafttower.client;

/**
 * Wraps scheduling commands to allow synchronous operation in tests.
 */
public interface SchedulerWrapper {
  void schedule(Runnable runnable, int delayMs);
  void scheduleImmediate(Runnable runnable);
  void scheduleLast(Runnable runnable);
  void scheduleRepeating(Runnable runnable, int periodMs);
}