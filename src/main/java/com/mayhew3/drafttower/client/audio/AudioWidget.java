package com.mayhew3.drafttower.client.audio;

import com.google.gwt.media.client.Audio;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.inject.Inject;

/**
 * Controls audio clips.
 */
public class AudioWidget extends Composite implements AudioView {

  private final Audio itsOver;
  private final Frame audioFrame;

  @Inject
  public AudioWidget(AudioPresenter presenter) {
    FlowPanel container = new FlowPanel();
    container.setSize("0", "0");

    this.itsOver = createAudio("over.mp3");

    audioFrame = new Frame();
    audioFrame.setSize("0", "0");
    container.add(audioFrame);

    initWidget(container);

    presenter.setAudioView(this);
  }

  private Audio createAudio(String src) {
    Audio onDeck = Audio.createIfSupported();
    onDeck.setPreload("auto");
    onDeck.setControls(false);
    onDeck.setSrc(src);
    return onDeck;
  }

  @Override
  public void play(String msg) {
    stopCurrentAudio();
    audioFrame.setUrl("http://translate.google.com/translate_tts?tl=en&q="
        + UriUtils.encode(msg));
  }

  @Override
  public void playItsOver() {
    stopCurrentAudio();
    itsOver.play();
  }

  private void stopCurrentAudio() {
    try {
      itsOver.getAudioElement().pause();
      itsOver.getAudioElement().setCurrentTime(0);
      audioFrame.setUrl("");
    } catch (Exception e) {
      // Something happens here sometimes - clearing cache fixes it, so idk
    }
  }
}