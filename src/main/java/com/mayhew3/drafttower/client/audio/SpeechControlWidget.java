package com.mayhew3.drafttower.client.audio;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.mayhew3.drafttower.client.audio.AudioPresenter.Level;

import javax.inject.Inject;

/**
 * Widget for changing amount of TTS.
 */
public class SpeechControlWidget extends Composite implements SpeechControlView {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String control();
      String off();
      String low();
      String high();
    }

    @Source("SpeechControlWidget.css")
    Css css();
  }

  static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  @Inject
  public SpeechControlWidget(final AudioPresenter presenter) {
    HTML widget = new HTML();
    widget.setStyleName(CSS.control());
    widget.addStyleName(CSS.high());
    initWidget(widget);

    presenter.setSpeechControlView(this);
    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        presenter.toggleLevel();
      }
    });
  }

  @Override
  public void setLevel(Level level) {
    switch (level) {
      case OFF:
        removeStyleName(CSS.low());
        removeStyleName(CSS.high());
        addStyleName(CSS.off());
        break;
      case LOW:
        removeStyleName(CSS.off());
        removeStyleName(CSS.high());
        addStyleName(CSS.low());
        break;
      case HIGH:
        removeStyleName(CSS.off());
        removeStyleName(CSS.low());
        addStyleName(CSS.high());
        break;
    }
  }
}