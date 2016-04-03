package com.mayhew3.drafttower.server;

/**
 * {@link SimTest} with fake data sources.
 */
public class NoDBSimTest extends SimTest {

  private static final long TIMEOUT_MS = 300000;

  @Override
  protected SimTestComponent getDependencyComponent() {
    NoDBSimTestComponent testComponent = DaggerNoDBSimTestComponent.create();
    testComponent.injectEager();
    return testComponent;
  }

  @Override
  protected double getTimeoutMs() {
    return TIMEOUT_MS;
  }
}