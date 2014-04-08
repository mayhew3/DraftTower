package com.mayhew3.drafttower.server;

/**
 * Wraps an integer representing a team's draft order, to ensure that we don't get it
 * confused with the team's database ID.
 */
public class TeamDraftOrder extends IntWrapper {
  public TeamDraftOrder(int value) {
    super(value);
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