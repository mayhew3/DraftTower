package com.mayhew3.drafttower.client;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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
    public void onMessage(String msg);
    public void onDisconnect();
  }

  private final Websocket socket;
  private final List<DraftStatusListener> listeners = Lists.newArrayList();

  @Inject
  public DraftSocketHandler() {
    String socketUrl = Window.Location.createUrlBuilder()
        .setProtocol("ws")
        .setPath("socket")
        .buildString();
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
      // TODO: parse structured message.
      listener.onMessage(msg);
    }
  }

  public void onClose() {
    for (DraftStatusListener listener : listeners) {
      listener.onDisconnect();
    }
    // TODO: attempt reconnect?
  }
}