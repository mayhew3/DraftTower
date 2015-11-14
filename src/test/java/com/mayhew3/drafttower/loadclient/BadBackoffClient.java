package com.mayhew3.drafttower.loadclient;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftCommand;
import com.mayhew3.drafttower.shared.DraftCommand.Command;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Degenerate client which fails to back off when trying to reconnect.
 */
public class BadBackoffClient {
  public static void main(String... args) throws Exception {
    final BeanFactory beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    WebSocketClientFactory factory = new WebSocketClientFactory();
    factory.start();
    final WebSocketClient client = factory.newWebSocketClient();
    OnTextMessage websocket = new OnTextMessage() {
      @Override
      public void onOpen(Connection connection) {
        System.out.println("onOpen");
        AutoBean<DraftCommand> draftCommand = beanFactory.createDraftCommand();
        draftCommand.as().setCommandType(Command.IDENTIFY);
        draftCommand.as().setTeamToken("asdf");
        String msg = AutoBeanCodex.encode(draftCommand).getPayload();
        try {
          connection.sendMessage(msg);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onClose(int closeCode, String message) {
        System.out.println("onClose");
        try {
          connect(client, this);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onMessage(String data) {
        // handle incoming message
      }
    };
    connect(client, websocket);
  }

  private static void connect(WebSocketClient client, OnTextMessage websocket) throws Exception {
    client.open(new URI("ws://localhost:8081/draft-tower-1/socket"), websocket).get(5, TimeUnit.SECONDS);
  }
}