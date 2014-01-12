package com.mayhew3.drafttower.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftCommand;
import com.mayhew3.drafttower.shared.ServletEndpoints;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.mayhew3.drafttower.shared.DraftCommand.Command.IDENTIFY;

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
      try {
        connection.sendMessage(message);
      } catch (IOException e) {
        // TODO?
        e.printStackTrace();
      }
    }

    @Override
    public void onMessage(String msg) {
      if (msg.startsWith(ServletEndpoints.CLOCK_SYNC)) {
        try {
          connection.sendMessage(msg + ServletEndpoints.CLOCK_SYNC_SEP + System.currentTimeMillis());
        } catch (IOException e) {
          // TODO?
          e.printStackTrace();
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
          connection.close(1008, e.getMessage());
        }
      }
    }

    @Override
    public void onClose(int closeCode, String message) {
      openSockets.remove(this);
      if (closeCode != 1008 && teamToken != null) {
        for (DraftCommandListener listener : listeners) {
          listener.onClientDisconnected(teamToken);
        }
      }
    }
  }

  private final BeanFactory beanFactory;

  private List<DraftCommandListener> listeners = Lists.newArrayList();
  private Set<DraftTowerWebSocket> openSockets = Sets.newHashSet();

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