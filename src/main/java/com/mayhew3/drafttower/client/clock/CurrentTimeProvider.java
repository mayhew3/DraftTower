package com.mayhew3.drafttower.client.clock;

/**
 * Interface providing current time.
 */
public interface CurrentTimeProvider {
  long getCurrentTimeMillis();

  public static class CurrentTimeProviderImpl implements CurrentTimeProvider {
    @Override
    public long getCurrentTimeMillis() {
      return System.currentTimeMillis();
    }
  }

  public static class FakeCurrentTimeProvider implements CurrentTimeProvider {
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