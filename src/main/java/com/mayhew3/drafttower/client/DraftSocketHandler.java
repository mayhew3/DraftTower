package com.mayhew3.drafttower.client;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.client.DraftTowerGinModule.DraftSocketUrl;
import com.mayhew3.drafttower.client.DraftTowerGinModule.TeamToken;
import com.mayhew3.drafttower.client.events.LoginEvent;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftCommand;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.sksamuel.gwt.websockets.Websocket;
import com.sksamuel.gwt.websockets.WebsocketListener;

import java.util.List;

import static com.mayhew3.drafttower.shared.DraftCommand.Command.IDENTIFY;

/**
 * Class which handles communicating draft status and actions with the server.
 */
@Singleton
public class DraftSocketHandler implements
    WebsocketListener, LoginEvent.Handler {

  public interface DraftStatusListener {
    public void onConnect();
    public void onMessage(DraftStatus status);
    public void onDisconnect();
  }

  private final BeanFactory beanFactory;
  private final Websocket socket;
  private final List<DraftStatusListener> listeners = Lists.newArrayList();
  private final StringHolder teamToken;

  @Inject
  public DraftSocketHandler(BeanFactory beanFactory,
      @DraftSocketUrl String socketUrl,
      @TeamToken StringHolder teamToken,
      EventBus eventBus) {
    this.beanFactory = beanFactory;
    this.teamToken = teamToken;
    socket = new Websocket(socketUrl);
    socket.addListener(this);
    socket.open();

    eventBus.addHandler(LoginEvent.TYPE, this);
  }

  public void onOpen() {
    for (DraftStatusListener listener : listeners) {
      listener.onConnect();
    }
  }

  public void onMessage(String msg) {
    for (DraftStatusListener listener : listeners) {
      listener.onMessage(AutoBeanCodex.decode(beanFactory, DraftStatus.class, msg).as());
    }
  }

  public void onClose() {
    for (DraftStatusListener listener : listeners) {
      listener.onDisconnect();
    }
    // TODO: attempt reconnect?
  }

  public void addListener(DraftStatusListener listener) {
    listeners.add(listener);
  }

  public void sendMessage(String msg) {
    socket.send(msg);
  }

  public void onLogin(LoginEvent event) {
    AutoBean<DraftCommand> commandBean = beanFactory.createDraftCommand();
    DraftCommand command = commandBean.as();
    command.setCommandType(IDENTIFY);
    command.setTeamToken(teamToken.getValue());
    sendMessage(AutoBeanCodex.encode(commandBean).getPayload());
  }
}