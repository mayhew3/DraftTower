package com.mayhew3.drafttower.server;

/**
 * Base class for classes wrapping an integer value.
 */
public abstract class IntWrapper {
  private Integer value;

  public IntWrapper(Integer value) {
    this.value = value;
  }

  public Integer get() {
    return value;
  }
}