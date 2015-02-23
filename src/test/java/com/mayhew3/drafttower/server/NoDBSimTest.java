package com.mayhew3.drafttower.server;

import com.google.guiceberry.junit4.GuiceBerryRule;
import org.junit.Rule;

/**
 * {@link SimTest} with fake data sources.
 */
public class NoDBSimTest extends SimTest {
  @Rule
  public final GuiceBerryRule guiceBerry =
      new GuiceBerryRule(SimTestGuiceBerryEnv.class);

  private static final long TIMEOUT_MS = 300000;

  @Override
  protected double getTimeoutMs() {
    return TIMEOUT_MS;
  }
}