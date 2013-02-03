package com.mayhew3.drafttower.client;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.client.DraftTowerGinModule.DraftSocketUrl;
import com.mayhew3.drafttower.client.events.*;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftCommand;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.sksamuel.gwt.websockets.Websocket;
import com.sksamuel.gwt.websockets.WebsocketListener;

import static com.mayhew3.drafttower.shared.DraftCommand.Command.*;

/**
 * Class which handles communicating draft status and actions with the server.
 */
@Singleton
public class DraftSocketHandler implements
    WebsocketListener,
    LoginEvent.Handler,
    PlayPauseEvent.Handler {

  private final BeanFactory beanFactory;
  private final Websocket socket;
  private final TeamInfo teamInfo;
  private final EventBus eventBus;

  private DraftStatus draftStatus;

  @Inject
  public DraftSocketHandler(BeanFactory beanFactory,
      @DraftSocketUrl String socketUrl,
      TeamInfo teamInfo,
      EventBus eventBus) {
    this.beanFactory = beanFactory;
    this.teamInfo = teamInfo;
    socket = new Websocket(socketUrl);
    socket.addListener(this);
    socket.open();

    this.eventBus = eventBus;
    eventBus.addHandler(LoginEvent.TYPE, this);
    eventBus.addHandler(PlayPauseEvent.TYPE, this);
  }

  public void onOpen() {
    eventBus.fireEvent(new SocketConnectEvent());
  }

  public void onMessage(String msg) {
    draftStatus = AutoBeanCodex.decode(beanFactory, DraftStatus.class, msg).as();
    eventBus.fireEvent(new DraftStatusChangedEvent(draftStatus));
  }

  public void onClose() {
    eventBus.fireEvent(new SocketDisconnectEvent());
    // TODO: attempt reconnect?
  }

  private void sendDraftCommand(DraftCommand.Command commandType) {
    AutoBean<DraftCommand> commandBean = beanFactory.createDraftCommand();
    DraftCommand command = commandBean.as();
    command.setCommandType(commandType);
    command.setTeamToken(teamInfo.getValue().getTeamToken());
    sendMessage(AutoBeanCodex.encode(commandBean).getPayload());
  }

  public void sendMessage(String msg) {
    socket.send(msg);
  }

  public void onLogin(LoginEvent event) {
    sendDraftCommand(IDENTIFY);
  }

  public void onPlayPause(PlayPauseEvent event) {
    if (draftStatus.isPaused()) {
      sendDraftCommand(RESUME);
    } else {
      sendDraftCommand(PAUSE);
    }
  }
}