package com.mayhew3.drafttower.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.sksamuel.gwt.websockets.Websocket;
import com.sksamuel.gwt.websockets.WebsocketListener;

/**
 * Sample UI for testing WebSocket communication.
 */
public class WebSocketDemonstrationPanel extends Composite implements WebsocketListener {

  private final Websocket socket;
  private final FlowPanel container;
  private final Button sendButton;

  public WebSocketDemonstrationPanel() {
    container = new FlowPanel();

    final TextBox textBox = new TextBox();
    container.add(textBox);

    sendButton = new Button("Send", new ClickHandler() {
      public void onClick(ClickEvent event) {
        socket.send(textBox.getValue());
      }
    });
    sendButton.setEnabled(false);
    container.add(sendButton);

    initWidget(container);

    String socketUrl = Window.Location.createUrlBuilder()
        .setProtocol("ws")
        .setPath("socket")
        .buildString();
    socket = new Websocket(socketUrl);
    socket.addListener(this);
    socket.open();
  }

  public void onOpen() {
    sendButton.setEnabled(true);
  }

  public void onMessage(String msg) {
    container.add(new Label(msg));
  }

  public void onClose() {
    container.add(new Label("Connection closed."));
    sendButton.setEnabled(false);
  }
}