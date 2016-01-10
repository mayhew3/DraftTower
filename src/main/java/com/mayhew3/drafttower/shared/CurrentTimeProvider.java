package com.mayhew3.drafttower.shared;

import com.google.inject.Singleton;

/**
 * Interface providing current time.
 */
public interface CurrentTimeProvider {
  long getCurrentTimeMillis();

  class CurrentTimeProviderImpl implements CurrentTimeProvider {
    @Override
    public long getCurrentTimeMillis() {
      return System.currentTimeMillis();
    }
  }

  @Singleton
  class FakeCurrentTimeProvider implements CurrentTimeProvider {
    private long currentTimeMillis;

    @Override
    public long getCurrentTimeMillis() {
      return currentTimeMillis;
    }

    public void setCurrentTimeMillis(long currentTimeMillis) {
      this.currentTimeMillis = currentTimeMillis;
    }
  }
}