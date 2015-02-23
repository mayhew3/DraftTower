package com.mayhew3.drafttower.server;

import com.google.guiceberry.junit4.GuiceBerryRule;
import org.junit.Rule;

/**
 * {@link SimTest} with real database.
 */
public class DBSimTest extends SimTest {
  @Rule
  public final GuiceBerryRule guiceBerry =
      new GuiceBerryRule(DBSimTestGuiceBerryEnv.class);

  private static final long TIMEOUT_MS = 3000000;

  @Override
  protected double getTimeoutMs() {
    return TIMEOUT_MS;
  }
}