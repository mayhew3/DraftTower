package com.mayhew3.drafttower.shared;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Interface providing current time.
 */
public interface CurrentTimeProvider {
  long getCurrentTimeMillis();

  class CurrentTimeProviderImpl implements CurrentTimeProvider {
    @Inject
    public CurrentTimeProviderImpl() {}

    @Override
    public long getCurrentTimeMillis() {
      return System.currentTimeMillis();
    }
  }

  @Singleton
  class FakeCurrentTimeProvider implements CurrentTimeProvider {
    private long currentTimeMillis;

    @Inject
    public FakeCurrentTimeProvider() {}

    @Override
    public long getCurrentTimeMillis() {
      return currentTimeMillis;
    }

    public void setCurrentTimeMillis(long currentTimeMillis) {
      this.currentTimeMillis = currentTimeMillis;
    }
  }
}