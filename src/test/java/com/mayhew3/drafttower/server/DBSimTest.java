package com.mayhew3.drafttower.server;

/**
 * {@link SimTest} with real database.
 */
public class DBSimTest extends SimTest {
  private static final long TIMEOUT_MS = 3000000;

  @Override
  protected SimTestComponent getDependencyComponent() {
    DBSimTestComponent testComponent = DaggerDBSimTestComponent.create();
    testComponent.injectEager();
    return testComponent;
  }

  @Override
  protected double getTimeoutMs() {
    return TIMEOUT_MS;
  }
}