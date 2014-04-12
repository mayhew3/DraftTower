package com.mayhew3.drafttower.client.clock;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Widget for displaying the draft clock.
 */
public class DraftClock extends Composite implements ClockView {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String container();
      String clock();
      String lowTime();
      String paused();
      String playPause();
    }

    @Source("DraftClock.css")
    Css css();
  }

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  interface MyUiBinder extends UiBinder<Widget, DraftClock> {}
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private final ClockPresenter presenter;

  @UiField Label clockDisplay;
  @UiField Label playPause;

  @Inject
  public DraftClock(final ClockPresenter presenter) {
    this.presenter = presenter;

    initWidget(uiBinder.createAndBindUi(this));

    playPause.setVisible(false);

    Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
      @Override
      public boolean execute() {
        presenter.update();
        return true;
      }
    }, ClockPresenter.MILLIS_PER_SECOND / 4);

    presenter.setView(this);
  }

  @UiHandler("playPause")
  public void handlePlayPause(MouseDownEvent e) {
    presenter.playPause();
  }

  @Override
  public void setPlayPauseVisible(boolean visible) {
    playPause.setVisible(visible);
  }

  @Override
  public void clear() {
    clockDisplay.setText(" ");
  }

  @Override
  public void updateTime(long minutes, long seconds, boolean lowTime) {
    clockDisplay.setText(minutes + ":" + (seconds < 10 ? "0" : "") + seconds);
    clockDisplay.setStyleName(CSS.lowTime(), lowTime);
  }

  @Override
  public void updatePaused(boolean paused, boolean canPlay) {
    clockDisplay.setStyleName(CSS.paused(), paused);
    playPause.setText(canPlay ? "▸" : "❙❙");
  }
}