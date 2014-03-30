package com.mayhew3.drafttower.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftCommand;
import com.mayhew3.drafttower.shared.ServletEndpoints;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.mayhew3.drafttower.shared.DraftCommand.Command.IDENTIFY;
import static com.mayhew3.drafttower.shared.SocketTerminationReason.TEAM_ALREADY_CONNECTED;

/**
 * Servlet for WebSocket communication with clients.
 */
@Singleton
public class DraftTowerWebSocketServlet extends WebSocketServlet {

  public interface DraftCommandListener {
    void onClientConnected();
    void onDraftCommand(DraftCommand cmd) throws TerminateSocketException;
    void onClientDisconnected(String playerToken);
  }

  public class DraftTowerWebSocket implements WebSocket.OnTextMessage {

    private static final int MAX_RETRIES = 5;

    private Connection connection;
    private String teamToken;

    @Override
    public void onOpen(Connection connection) {
      openSockets.add(this);
      this.connection = connection;
      connection.setMaxIdleTime(0);
      for (DraftCommandListener listener : listeners) {
        listener.onClientConnected();
      }

    }

    public void sendMessage(String message) {
      for (int i = 0; i < MAX_RETRIES; i++) {
        try {
          connection.sendMessage(message);
          break;
        } catch (IOException e) {
          // retry...
        }
      }
    }

    @Override
    public void onMessage(String msg) {
      if (msg.startsWith(ServletEndpoints.CLOCK_SYNC)) {
        try {
          connection.sendMessage(msg + ServletEndpoints.CLOCK_SYNC_SEP + System.currentTimeMillis());
        } catch (IOException e) {
          // welp
        }
      } else {
        DraftCommand cmd = AutoBeanCodex.decode(beanFactory, DraftCommand.class, msg).as();
        if (cmd.getCommandType() == IDENTIFY) {
          teamToken = cmd.getTeamToken();
        }
        try {
          for (DraftCommandListener listener : listeners) {
            listener.onDraftCommand(cmd);
          }
        } catch (TerminateSocketException e) {
          connection.close(e.getReason().getCloseCode(), e.getMessage());
        }
      }
    }

    @Override
    public void onClose(int closeCode, String message) {
      openSockets.remove(this);
      if (closeCode != TEAM_ALREADY_CONNECTED.getCloseCode() && teamToken != null) {
        for (DraftCommandListener listener : listeners) {
          listener.onClientDisconnected(teamToken);
        }
      }
    }
  }

  private final BeanFactory beanFactory;

  private final List<DraftCommandListener> listeners = new CopyOnWriteArrayList<>();
  private final Set<DraftTowerWebSocket> openSockets = new ConcurrentHashSet<>();

  @Inject
  public DraftTowerWebSocketServlet(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  @Override
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