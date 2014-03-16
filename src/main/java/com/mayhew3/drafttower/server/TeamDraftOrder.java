package com.mayhew3.drafttower.server;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Wraps an integer representing a team's draft order, to ensure that we don't get it
 * confused with the team's database ID.
 */
public class TeamDraftOrder extends AtomicReference<Integer> {
  public TeamDraftOrder(Integer initialValue) {
    super(initialValue);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof TeamDraftOrder && ((TeamDraftOrder) obj).get().equals(get());
  }

  @Override
  public int hashCode() {
    return get();
  }
}