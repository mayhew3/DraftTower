package com.mayhew3.drafttower.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftCommand;

/**
 * Widget containing the entire UI.
 */
public class MainPageWidget extends Composite {

  interface MyUiBinder extends UiBinder<Widget, MainPageWidget> {}
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private final DraftSocketHandler socketHandler;
  private final BeanFactory beanFactory;
  @UiField(provided = true) final ConnectivityIndicator connectivityIndicator;
  @UiField(provided = true) final DraftClock clock;

  // Temporary.
  @UiField Button start;
  @UiField Button pause;
  @UiField Button resume;
  @UiField Button pick;

  @Inject
  public MainPageWidget(ConnectivityIndicator connectivityIndicator,
      DraftClock clock,
      final DraftSocketHandler socketHandler,
      final BeanFactory beanFactory) {
    this.connectivityIndicator = connectivityIndicator;
    this.socketHandler = socketHandler;
    this.beanFactory = beanFactory;
    this.clock = clock;

    initWidget(uiBinder.createAndBindUi(this));
  }

  // Temporary.

  @UiHandler("start") void sendStart(ClickEvent e) {
    sendDraftCommand(DraftCommand.Command.START_DRAFT);
  }

  @UiHandler("pause") void sendPause(ClickEvent e) {
    sendDraftCommand(DraftCommand.Command.PAUSE);
  }

  @UiHandler("resume") void sendResume(ClickEvent e) {
    sendDraftCommand(DraftCommand.Command.RESUME);
  }

  @UiHandler("pick") void sendPick(ClickEvent e) {
    sendDraftCommand(DraftCommand.Command.DO_PICK);
  }

  private void sendDraftCommand(DraftCommand.Command commandType) {
    AutoBean<DraftCommand> commandBean = beanFactory.createDraftCommand();
    DraftCommand command = commandBean.as();
    command.setCommandType(commandType);
    socketHandler.sendMessage(AutoBeanCodex.encode(commandBean).getPayload());
  }

}