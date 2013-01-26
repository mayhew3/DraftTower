package com.mayhew3.drafttower.server;

import com.google.common.collect.Sets;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;

/**
 * Servlet for WebSocket communication with clients.
 */
public class DraftTowerWebSocketServlet extends WebSocketServlet {

  private class DraftTowerWebSocket implements WebSocket.OnTextMessage {

    private Connection connection;

    public void onOpen(Connection connection) {
      openSockets.add(this);
      this.connection = connection;
    }

    public void sendMessage(String data) {
      try {
        connection.sendMessage(data);
      } catch (IOException e) {
        // TODO?
        e.printStackTrace();
      }
    }

    public void onMessage(String data) {
      // example: just relay message to all clients.
      for (DraftTowerWebSocket socket : openSockets) {
        socket.sendMessage(data);
      }
    }

    public void onClose(int closeCode, String message) {
      openSockets.remove(this);
    }
  }

  private Set<DraftTowerWebSocket> openSockets = Sets.newHashSet();

  public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
    return new DraftTowerWebSocket();
  }
}