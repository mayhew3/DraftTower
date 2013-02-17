package com.mayhew3.drafttower.shared;

import com.google.common.base.Predicate;

/** Predicate for finding a QueueEntry by its player ID. */
public class QueueEntryPredicate implements Predicate<QueueEntry> {
  private final long playerId;

  public QueueEntryPredicate(long playerId) {
    this.playerId = playerId;
  }

  @Override
  public boolean apply(QueueEntry input) {
    return input.getPlayerId() == playerId;
  }
}