package com.mayhew3.drafttower.client.audio;

import com.mayhew3.drafttower.client.audio.AudioPresenter.Level;

/**
 * View interface for {@link SpeechControlWidget}.
 */
public interface SpeechControlView {
  void setLevel(Level level);
}