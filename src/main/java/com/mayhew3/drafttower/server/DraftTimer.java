package com.mayhew3.drafttower.server;

/**
 * Class responsible for draft clock handling.
 */
public interface DraftTimer {

  interface Listener {
    void timerExpired();
  }

  void addListener(Listener listener);

  void start(long timeMs);

  void cancel();
}