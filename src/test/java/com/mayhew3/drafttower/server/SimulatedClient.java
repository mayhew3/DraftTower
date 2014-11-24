package com.mayhew3.drafttower.server;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.client.websocket.WebsocketListener;
import com.mayhew3.drafttower.shared.*;
import com.mayhew3.drafttower.shared.DraftCommand.Command;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Simulates client behavior in tests.
 */
public abstract class SimulatedClient implements WebsocketListener {

  @Inject protected BeanFactory beanFactory;
  @Inject protected DraftTowerWebSocketServlet webSocketServlet;
  protected DraftTowerWebSocketServlet.DraftTowerWebSocket webSocket;
  @Inject protected ChangePlayerRankServlet changePlayerRankServlet;
  @Inject protected CopyAllPlayerRanksServlet copyAllPlayerRanksServlet;
  @Inject protected GraphsServlet graphsServlet;
  @Inject protected LoginServlet loginServlet;
  @Inject protected QueueServlet queueServlet;
  @Inject protected SetAutoPickWizardServlet setAutoPickWizardServlet;
  @Inject protected UnclaimedPlayerLookupServlet unclaimedPlayerLookupServlet;
  @Inject protected DraftStatus draftStatus;

  private final Cookie[] cookies;
  protected String username;
  protected String teamToken;

  protected Connection connection;

  public SimulatedClient() {
    cookies = new Cookie[1];
    cookies[0] = new Cookie("a", "a");
  }

  @Override
  public void onClose(SocketTerminationReason reason) {
  }

  @Override
  public void onMessage(String msg) {
  }

  @Override
  public void onOpen() {
  }

  protected void login(boolean badPassword) throws ServletException, IOException {
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    Mockito.when(req.getCookies()).thenReturn(cookies);
    Mockito.when(req.getParameter("username")).thenReturn(username);
    Mockito.when(req.getParameter("password"))
        .thenReturn(badPassword ? TestTeamDataSource.BAD_PASSWORD : username);

    HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
    PrintWriter writer = Mockito.mock(PrintWriter.class);
    Mockito.when(resp.getWriter()).thenReturn(writer);
    Mockito.doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        LoginResponse response = AutoBeanCodex.decode(beanFactory, LoginResponse.class,
            (String) invocation.getArguments()[0]).as();
        teamToken = response.getTeamToken();
        return null;
      }
    }).when(writer).append(Mockito.<CharSequence>any());
    Mockito.doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        Cookie cookie = (Cookie) invocation.getArguments()[0];
        cookies[0] = cookie;
        return null;
      }
    }).when(resp).addCookie(Mockito.<Cookie>any());

    loginServlet.doPost(req, resp);
    if (teamToken == null) {
      // Login failed.
      return;
    }

    webSocket = (DraftTowerWebSocketServlet.DraftTowerWebSocket)
        webSocketServlet.doWebSocketConnect(null, null);
    connection = Mockito.mock(Connection.class);
    Mockito.doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        String message = (String) invocation.getArguments()[0];
        if (message.startsWith(ServletEndpoints.CLOCK_SYNC)) {
          // ignore
        } else {
          draftStatus = AutoBeanCodex.decode(beanFactory, DraftStatus.class, message).as();
        }
        return null;
      }
    }).when(connection).sendMessage(Mockito.anyString());
    Mockito.doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        webSocket.onClose((Integer) invocation.getArguments()[0], (String) invocation.getArguments()[1]);
        connection = null;
        return null;
      }
    }).when(connection).close(Mockito.anyInt(), Mockito.anyString());
    webSocket.onOpen(connection);

    sendDraftCommand(Command.IDENTIFY, null);
  }

  protected void sendDraftCommand(Command type, Long playerId) {
    AutoBean<DraftCommand> draftCommand = beanFactory.createDraftCommand();
    DraftCommand command = draftCommand.as();
    command.setTeamToken(teamToken);
    command.setCommandType(type);
    if (playerId != null) {
      command.setPlayerId(playerId);
    }
    webSocket.onMessage(AutoBeanCodex.encode(draftCommand).getPayload());
  }

  public void disconnect() {
    if (connection != null) {
      connection.close(-1, "");
    }
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public abstract void performAction();

  public abstract void verify();
}