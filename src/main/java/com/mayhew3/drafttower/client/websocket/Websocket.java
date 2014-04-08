package com.mayhew3.drafttower.client.websocket;

/**
 * Interface description...
 */
public interface Websocket {
  void addListener(WebsocketListener listener);

  void close();

  int getState();

  void open();

  void send(String msg);
}