package com.mayhew3.drafttower.server;

import com.google.common.collect.ListMultimap;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.server.BindingAnnotations.Queues;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.shared.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Servlet providing data for players queue.
 */
@Singleton
public class QueueServlet extends HttpServlet {

  private final BeanFactory beanFactory;
  private final QueueHandler queueHandler;
  private final PlayerDataSource playerDataSource;
  private final Map<String, TeamDraftOrder> teamTokens;
  private final ListMultimap<TeamDraftOrder, QueueEntry> queues;
  private final DraftStatus status;

  @Inject
  public QueueServlet(BeanFactory beanFactory,
      QueueHandler queueHandler,
      PlayerDataSource playerDataSource,
      @TeamTokens Map<String, TeamDraftOrder> teamTokens,
      @Queues ListMultimap<TeamDraftOrder, QueueEntry> queues,
      DraftStatus status) {
    this.beanFactory = beanFactory;
    this.queueHandler = queueHandler;
    this.playerDataSource = playerDataSource;
    this.teamTokens = teamTokens;
    this.queues = queues;
    this.status = status;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String requestStr = CharStreams.toString(req.getReader());
    if (req.getPathInfo().endsWith(ServletEndpoints.QUEUE_GET)) {
      GetPlayerQueueRequest request =
          AutoBeanCodex.decode(beanFactory, GetPlayerQueueRequest.class, requestStr).as();
      AutoBean<GetPlayerQueueResponse> response = beanFactory.createPlayerQueueResponse();
      if (teamTokens.containsKey(request.getTeamToken())) {
        TeamDraftOrder team = teamTokens.get(request.getTeamToken());
        response.as().setQueue(queueHandler.getQueue(team));
      }

      resp.getWriter().append(AutoBeanCodex.encode(response).getPayload());
    } else if (req.getPathInfo().endsWith(ServletEndpoints.QUEUE_ADD)) {
      final EnqueueOrDequeuePlayerRequest request =
          AutoBeanCodex.decode(beanFactory, EnqueueOrDequeuePlayerRequest.class, requestStr).as();
      String teamToken = request.getTeamToken();
      final long playerId = request.getPlayerId();
      Integer position = request.getPosition();
      try {
        if (teamTokens.containsKey(teamToken)) {
          TeamDraftOrder team = teamTokens.get(teamToken);
          queueHandler.enqueue(team, playerId, position);
        }
      } catch (DataSourceException e) {
        log(e.getMessage(), e);
        resp.setStatus(500);
      }
    } else if (req.getPathInfo().endsWith(ServletEndpoints.QUEUE_REMOVE)) {
      EnqueueOrDequeuePlayerRequest request =
          AutoBeanCodex.decode(beanFactory, EnqueueOrDequeuePlayerRequest.class, requestStr).as();
      if (teamTokens.containsKey(request.getTeamToken())) {
        TeamDraftOrder team = teamTokens.get(request.getTeamToken());
        long playerId = request.getPlayerId();
        queueHandler.dequeue(team, playerId);
      }
    } else if (req.getPathInfo().endsWith(ServletEndpoints.QUEUE_REORDER)) {
      ReorderPlayerQueueRequest request =
          AutoBeanCodex.decode(beanFactory, ReorderPlayerQueueRequest.class, requestStr).as();
      if (teamTokens.containsKey(request.getTeamToken())) {
        TeamDraftOrder team = teamTokens.get(request.getTeamToken());
        long playerId = request.getPlayerId();
        int newPosition = request.getNewPosition();
        queueHandler.reorderQueue(team, playerId, newPosition);
      }
    } else {
      resp.setStatus(404);
    }
    resp.setContentType("text/json");
  }
}