package com.mayhew3.drafttower.shared;

/**
 * Message object for communicating current draft status to clients.
 */
public interface DraftStatus {

  long getCurrentPickDeadline();
  void setCurrentPickDeadline(long deadline);

  boolean isPaused();
  void setPaused(boolean paused);
}