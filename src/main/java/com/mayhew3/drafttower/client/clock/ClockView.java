package com.mayhew3.drafttower.client.clock;

/**
 * Interface description...
 */
public interface ClockView {
  void setPlayPauseVisible(boolean visible);

  void clear();

  void updateTime(long minutes, long seconds, boolean lowTime);

  void updatePaused(boolean paused, boolean canPlay);
}