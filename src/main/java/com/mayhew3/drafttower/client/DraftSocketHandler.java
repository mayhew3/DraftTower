package com.mayhew3.drafttower.client;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.client.DraftTowerGinModule.DraftSocketUrl;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.sksamuel.gwt.websockets.Websocket;
import com.sksamuel.gwt.websockets.WebsocketListener;

import java.util.List;

/**
 * Class which handles communicating draft status and actions with the server.
 */
@Singleton
public class DraftSocketHandler implements WebsocketListener {

  public interface DraftStatusListener {
    public void onConnect();
    public void onMessage(DraftStatus status);
    public void onDisconnect();
  }

  private final BeanFactory beanFactory;
  private final Websocket socket;
  private final List<DraftStatusListener> listeners = Lists.newArrayList();

  @Inject
  public DraftSocketHandler(BeanFactory beanFactory,
      @DraftSocketUrl String socketUrl) {
    this.beanFactory = beanFactory;
    socket = new Websocket(socketUrl);
    socket.addListener(this);
    socket.open();
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
}