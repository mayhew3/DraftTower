package com.mayhew3.drafttower.client.audio;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AudioElement;
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

    itsOver = createAudio("over.mp3");
    container.add(itsOver);

    audioFrame = new Frame();
    audioFrame.setSize("0", "0");
    container.add(audioFrame);

    initWidget(container);

    presenter.setAudioView(this);
  }

  private Audio createAudio(String src) {
    Audio audio = Audio.createIfSupported();
    audio.setPreload("auto");
    audio.setControls(false);
    audio.setSrc(src);
    return audio;
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
    if (GWT.isProdMode()
        && itsOver.getError() == null
        && itsOver.getReadyState() == AudioElement.HAVE_ENOUGH_DATA) {
      itsOver.play();
    }
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

  @Override
  protected void onEnsureDebugId(String baseID) {
    super.onEnsureDebugId(baseID);
    audioFrame.ensureDebugId(baseID + "-frame");
    itsOver.ensureDebugId(baseID + "-itsover");
  }
}