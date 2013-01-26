package com.mayhew3.drafttower.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Servlet for WebSocket communication with clients.
 */
@Singleton
public class DraftTowerWebSocketServlet extends WebSocketServlet {

  public interface DraftCommandListener {
    void onClientConnected();
    void onDraftCommand(String cmd);
    void onClientDisconnected();
  }

  public class DraftTowerWebSocket implements WebSocket.OnTextMessage {

    private Connection connection;

    public void onOpen(Connection connection) {
      openSockets.add(this);
      this.connection = connection;
      for (DraftCommandListener listener : listeners) {
        listener.onClientConnected();
      }
    }

    public void sendMessage(String message) {
      try {
        connection.sendMessage(message);
      } catch (IOException e) {
        // TODO?
        e.printStackTrace();
      }
    }

    public void onMessage(String msg) {
      for (DraftCommandListener listener : listeners) {
        listener.onDraftCommand(msg);
      }
    }

    public void onClose(int closeCode, String message) {
      openSockets.remove(this);
      for (DraftCommandListener listener : listeners) {
        listener.onClientDisconnected();
      }
    }
  }

  private List<DraftCommandListener> listeners = Lists.newArrayList();
  private Set<DraftTowerWebSocket> openSockets = Sets.newHashSet();

  public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
    return new DraftTowerWebSocket();
  }

  public void addListener(DraftCommandListener listener) {
    listeners.add(listener);
  }

  public void sendMessage(String message) {
    for (DraftTowerWebSocket socket : openSockets) {
      socket.sendMessage(message);
    }
  }
}