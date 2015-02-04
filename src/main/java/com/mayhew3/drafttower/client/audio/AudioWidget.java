package com.mayhew3.drafttower.client.audio;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.media.client.Audio;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.GinBindingAnnotations.TtsUrlPrefix;

/**
 * Controls audio clips.
 */
public class AudioWidget extends Composite implements AudioView {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String frame();
    }

    @Source("AudioWidget.css")
    Css css();
  }

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  private final Audio itsOver;
  private final Frame audioFrame;
  private String ttsUrlPrefix;

  @Inject
  public AudioWidget(AudioPresenter presenter,
      @TtsUrlPrefix String ttsUrlPrefix) {
    this.ttsUrlPrefix = ttsUrlPrefix;

    FlowPanel container = new FlowPanel();
    container.setSize("0", "0");

    itsOver = createAudio("over.mp3");
    container.add(itsOver);

    audioFrame = new Frame();
    audioFrame.setStyleName(CSS.frame());
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
    audioFrame.setUrl(ttsUrlPrefix + UriUtils.encode(msg));
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