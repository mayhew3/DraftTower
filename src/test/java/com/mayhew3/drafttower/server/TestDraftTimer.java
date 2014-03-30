package com.mayhew3.drafttower.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Testing version of {@link DraftTimer}, advanced programatically.
 */
public class TestDraftTimer implements DraftTimer {
  private List<Listener> listeners = Lists.newCopyOnWriteArrayList();
  private boolean started;

  @Override
  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  @Override
  public void start(long timeMs) {
    started = true;
  }

  @Override
  public void cancel() {
    started = false;
  }

  @VisibleForTesting
  public void expire() {
    if (!started) {
      throw new IllegalStateException("Tried to expire paused timer");
    }
    started = false;
    for (Listener listener : listeners) {
      listener.timerExpired();
    }
  }
}