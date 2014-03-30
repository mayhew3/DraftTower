package com.mayhew3.drafttower.server;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Production version of {@link DraftTimer}, uses real timers.
 */
public class DraftTimerImpl implements DraftTimer {

  private final List<Listener> listeners = Lists.newCopyOnWriteArrayList();

  private final ScheduledThreadPoolExecutor pickTimer = new ScheduledThreadPoolExecutor(1);
  private ScheduledFuture<?> currentPickTimer;

  @Override
  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  @Override
  public void start(long timeMs) {
    currentPickTimer = pickTimer.schedule(new Runnable() {
      @Override
      public void run() {
        currentPickTimer = null;
        for (Listener listener : listeners) {
          listener.timerExpired();
        }
      }
    }, timeMs, TimeUnit.MILLISECONDS);
  }

  @Override
  public void cancel() {
    if (currentPickTimer != null) {
      currentPickTimer.cancel(true);
    }
  }
}