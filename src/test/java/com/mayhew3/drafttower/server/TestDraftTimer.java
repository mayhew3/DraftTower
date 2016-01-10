package com.mayhew3.drafttower.server;

import com.google.common.annotations.VisibleForTesting;
import com.mayhew3.drafttower.server.BindingAnnotations.DraftTimerListenerList;

import javax.inject.Inject;
import java.util.List;

/**
 * Testing version of {@link DraftTimer}, advanced programatically.
 */
public class TestDraftTimer implements DraftTimer {
  private final List<Listener> listeners;
  private boolean started;

  @Inject
  public TestDraftTimer(@DraftTimerListenerList List<Listener> listenerList) {
    this.listeners = listenerList;
  }

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