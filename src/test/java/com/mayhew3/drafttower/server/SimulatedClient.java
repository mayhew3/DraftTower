package com.mayhew3.drafttower.server;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.client.websocket.WebsocketListener;
import com.mayhew3.drafttower.server.SimTest.CommissionerTeam;
import com.mayhew3.drafttower.shared.*;
import com.mayhew3.drafttower.shared.DraftCommand.Command;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.junit.Assert;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
  @Inject protected SetCloserLimitServlet setCloserLimitServlet;
  @Inject protected UnclaimedPlayerLookupServlet unclaimedPlayerLookupServlet;
  @Inject protected DraftStatus draftStatus;
  @Inject @CommissionerTeam protected String commissionerTeam;
  @Inject protected PlayerDataProvider playerDataProvider;
  protected List<Player> players;

  private final Cookie[] cookies;
  protected String username;
  protected String teamToken;
  protected int teamDraftOrder;

  protected Connection connection;
  protected List<Exception> exceptions;

  public SimulatedClient() {
    cookies = new Cookie[1];
    cookies[0] = new Cookie("a", "a");
    exceptions = new ArrayList<>();
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

  protected void changePlayerRank(long playerId, int prevRank, int newRank) throws ServletException, IOException {
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    AutoBean<ChangePlayerRankRequest> request = beanFactory.createChangePlayerRankRequest();
    request.as().setTeamToken(teamToken);
    request.as().setPlayerId(playerId);
    request.as().setPrevRank(prevRank);
    request.as().setNewRank(newRank);
    Mockito.when(req.getReader()).thenReturn(new BufferedReader(new StringReader(AutoBeanCodex.encode(request).getPayload())));
    HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
    Mockito.when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    changePlayerRankServlet.doPost(req, resp);
  }

  protected void copyAllPlayerRanks(TableSpec tableSpec) throws ServletException, IOException {
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    AutoBean<CopyAllPlayerRanksRequest> request = beanFactory.createCopyAllPlayerRanksRequest();
    request.as().setTeamToken(teamToken);
    request.as().setTableSpec(tableSpec);
    Mockito.when(req.getReader()).thenReturn(new BufferedReader(new StringReader(AutoBeanCodex.encode(request).getPayload())));
    HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
    Mockito.when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    copyAllPlayerRanksServlet.doPost(req, resp);
  }

  protected void getGraphsData() throws ServletException, IOException {
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    AutoBean<GetGraphsDataRequest> request = beanFactory.createGetGraphsDataRequest();
    request.as().setTeamToken(teamToken);
    Mockito.when(req.getReader()).thenReturn(new BufferedReader(new StringReader(AutoBeanCodex.encode(request).getPayload())));
    HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
    Mockito.when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    graphsServlet.doPost(req, resp);
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
        teamDraftOrder = response.getTeam();
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
    connection = Mockito.mock(Connection.class, Mockito.withSettings().stubOnly());
    Mockito.doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        String message = (String) invocation.getArguments()[0];
        if (message.startsWith(ServletEndpoints.CLOCK_SYNC)) {
          // ignore
        } else {
          draftStatus = AutoBeanCodex.decode(beanFactory, ClientDraftStatus.class, message).as().getDraftStatus();
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

    TableSpec tableSpec = beanFactory.createTableSpec().as();
    tableSpec.setPlayerDataSet(PlayerDataSet.CBSSPORTS);
    tableSpec.setSortCol(PlayerColumn.MYRANK);
    tableSpec.setAscending(true);
    try {
      players = playerDataProvider.getPlayers(new TeamId(1), tableSpec);
    } catch (DataSourceException e) {
      throw new RuntimeException(e);
    }
  }

  protected void getPlayerQueue() throws ServletException, IOException {
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    AutoBean<GetPlayerQueueRequest> request = beanFactory.createPlayerQueueRequest();
    request.as().setTeamToken(teamToken);
    Mockito.when(req.getReader()).thenReturn(new BufferedReader(new StringReader(AutoBeanCodex.encode(request).getPayload())));
    Mockito.when(req.getPathInfo()).thenReturn(ServletEndpoints.QUEUE_GET);
    HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
    Mockito.when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    queueServlet.doPost(req, resp);
  }

  protected void enqueue(long playerId, Integer position) throws ServletException, IOException {
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    AutoBean<EnqueueOrDequeuePlayerRequest> request = beanFactory.createEnqueueOrDequeuePlayerRequest();
    request.as().setTeamToken(teamToken);
    request.as().setPlayerId(playerId);
    request.as().setPosition(position);
    Mockito.when(req.getReader()).thenReturn(new BufferedReader(new StringReader(AutoBeanCodex.encode(request).getPayload())));
    Mockito.when(req.getPathInfo()).thenReturn(ServletEndpoints.QUEUE_ADD);
    HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
    Mockito.when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    queueServlet.doPost(req, resp);
  }

  protected void dequeue(long playerId) throws ServletException, IOException {
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    AutoBean<EnqueueOrDequeuePlayerRequest> request = beanFactory.createEnqueueOrDequeuePlayerRequest();
    request.as().setTeamToken(teamToken);
    request.as().setPlayerId(playerId);
    Mockito.when(req.getReader()).thenReturn(new BufferedReader(new StringReader(AutoBeanCodex.encode(request).getPayload())));
    Mockito.when(req.getPathInfo()).thenReturn(ServletEndpoints.QUEUE_REMOVE);
    HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
    Mockito.when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    queueServlet.doPost(req, resp);
  }

  protected void reorderQueue(long playerId, int newPosition) throws ServletException, IOException {
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    AutoBean<ReorderPlayerQueueRequest> request = beanFactory.createReorderPlayerQueueRequest();
    request.as().setTeamToken(teamToken);
    request.as().setPlayerId(playerId);
    request.as().setNewPosition(newPosition);
    Mockito.when(req.getReader()).thenReturn(new BufferedReader(new StringReader(AutoBeanCodex.encode(request).getPayload())));
    Mockito.when(req.getPathInfo()).thenReturn(ServletEndpoints.QUEUE_REORDER);
    HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
    Mockito.when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    queueServlet.doPost(req, resp);
  }

  protected void setAutoPickWizard(PlayerDataSet playerDataSet) throws ServletException, IOException {
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    AutoBean<SetWizardTableRequest> request = beanFactory.createSetAutoPickWizardRequest();
    request.as().setTeamToken(teamToken);
    request.as().setPlayerDataSet(playerDataSet);
    Mockito.when(req.getReader()).thenReturn(new BufferedReader(new StringReader(AutoBeanCodex.encode(request).getPayload())));
    HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
    Mockito.when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    setAutoPickWizardServlet.doPost(req, resp);
  }

  protected void setCloserLimits(int minClosers, int maxClosers) throws ServletException, IOException {
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    AutoBean<SetCloserLimitRequest> request = beanFactory.createSetCloserLimitsRequest();
    request.as().setTeamToken(teamToken);
    request.as().setMinClosers(minClosers);
    request.as().setMaxClosers(maxClosers);
    Mockito.when(req.getReader()).thenReturn(new BufferedReader(new StringReader(AutoBeanCodex.encode(request).getPayload())));
    HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
    Mockito.when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    setCloserLimitServlet.doPost(req, resp);
  }

  protected void getUnclaimedPlayers(TableSpec tableSpec) throws ServletException, IOException {
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    AutoBean<UnclaimedPlayerListRequest> request = beanFactory.createUnclaimedPlayerListRequest();
    request.as().setTeamToken(teamToken);
    request.as().setTableSpec(tableSpec);
    Mockito.when(req.getReader()).thenReturn(new BufferedReader(new StringReader(AutoBeanCodex.encode(request).getPayload())));
    HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
    Mockito.when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    unclaimedPlayerLookupServlet.doPost(req, resp);
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

  public void verify() {
    if (!exceptions.isEmpty()) {
      exceptions.get(0).printStackTrace();
      Assert.fail("Client " + username + " threw an exception.");
    }
  }
}