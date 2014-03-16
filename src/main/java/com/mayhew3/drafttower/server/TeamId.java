package com.mayhew3.drafttower.server;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Wraps an integer representing a team's database ID, to ensure that we don't get it
 * confused with the team's draft order.
 */
public class TeamId extends AtomicReference<Integer> {
  public TeamId(Integer initialValue) {
    super(initialValue);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof TeamId && ((TeamId) obj).get().equals(get());
  }

  @Override
  public int hashCode() {
    return get();
  }
}