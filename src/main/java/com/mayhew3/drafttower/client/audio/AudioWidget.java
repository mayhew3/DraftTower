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
  private final String ttsUrlPrefix;

  @Inject
  public AudioWidget(AudioPresenter presenter,
      @TtsUrlPrefix String ttsUrlPrefix) {
    this.ttsUrlPrefix = ttsUrlPrefix;

    FlowPanel container = new FlowPanel();
    container.setSize("0", "0");

    itsOver = createItsOverAudio();
    container.add(itsOver);

    if (!supportsClientSideTts()) {
      audioFrame = new Frame();
      audioFrame.setStyleName(CSS.frame());
      container.add(audioFrame);
    } else {
      audioFrame = null;
    }

    initWidget(container);

    presenter.setAudioView(this);
  }

  private Audio createItsOverAudio() {
    Audio audio = Audio.createIfSupported();
    audio.setPreload("auto");
    audio.setControls(false);
    audio.setSrc("over.mp3");
    return audio;
  }

  @Override
  public void play(String msg) {
    stopCurrentAudio();
    if (audioFrame != null) {
      audioFrame.setUrl(ttsUrlPrefix + UriUtils.encode(msg));
    } else {
      speak(msg);
    }
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
      if (audioFrame != null) {
        audioFrame.setUrl("");
      } else {
        cancelSpeech();
      }
    } catch (Exception e) {
      // Something happens here sometimes - clearing cache fixes it, so idk
    }
  }

  @Override
  protected void onEnsureDebugId(String baseID) {
    super.onEnsureDebugId(baseID);
    if (audioFrame != null) {
      audioFrame.ensureDebugId(baseID + "-frame");
    }
    itsOver.ensureDebugId(baseID + "-itsover");
  }

  private native static boolean supportsClientSideTts() /*-{
    var supported = "speechSynthesis" in $wnd;
    if (supported && !$wnd.ttsVoice) {
      var setVoice = function() {
        var voices = $wnd.speechSynthesis.getVoices();
        $wnd.ttsVoice = voices.filter(function(voice) {
            return voice.name == "Alex" || voice.name == "Google US English";
        })[0];
      };
      var voices = $wnd.speechSynthesis.getVoices();
      if (voices.length == 0) {
        $wnd.speechSynthesis.onvoiceschanged = setVoice;
      } else {
        setVoice();
      }
    }
    return supported;
  }-*/;

  private native static void speak(String msg) /*-{
    var utterance = new SpeechSynthesisUtterance();
    utterance.voice = $wnd.ttsVoice;
    utterance.text = msg;
    utterance.lang = "en-US";
    $wnd.speechSynthesis.speak(utterance);
  }-*/;

  private native static void cancelSpeech() /*-{
    $wnd.speechSynthesis.cancel();
  }-*/;
}