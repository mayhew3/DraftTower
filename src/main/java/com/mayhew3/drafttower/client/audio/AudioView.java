package com.mayhew3.drafttower.client.audio;

/**
 * View interface for {@link AudioWidget}.
 */
public interface AudioView {
  void play(String msg);
  void playItsOver();
}