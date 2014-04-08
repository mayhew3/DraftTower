package com.mayhew3.drafttower.server;

/**
 * Wraps an integer representing a team's database ID, to ensure that we don't get it
 * confused with the team's draft order.
 */
public class TeamId extends IntWrapper {
  public TeamId(int value) {
    super(value);
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