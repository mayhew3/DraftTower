package com.mayhew3.drafttower.server;

/**
 * Base class for classes wrapping an integer value.
 */
public abstract class IntWrapper {
  private final Integer value;

  public IntWrapper(Integer value) {
    this.value = value;
  }

  public Integer get() {
    return value;
  }

  @Override
  public String toString() {
    return Integer.toString(value);
  }
}