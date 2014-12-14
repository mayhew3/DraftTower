package com.mayhew3.drafttower.shared;

import com.google.common.base.Predicate;

/** Predicate for finding a QueueEntry by its player ID. */
public class PlayerPredicate implements Predicate<Player> {
  private final long playerId;

  public PlayerPredicate(long playerId) {
    this.playerId = playerId;
  }

  @Override
  public boolean apply(Player input) {
    return input.getPlayerId() == playerId;
  }
}