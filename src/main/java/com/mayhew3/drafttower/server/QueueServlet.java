package com.mayhew3.drafttower.server;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.server.ServerModule.Queues;
import com.mayhew3.drafttower.server.ServerModule.TeamTokens;
import com.mayhew3.drafttower.shared.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Servlet providing data for players queue.
 */
@Singleton
public class QueueServlet extends HttpServlet {

  private static class QueueEntryPredicate implements Predicate<QueueEntry> {
    private final long playerId;

    public QueueEntryPredicate(long playerId) {
      this.playerId = playerId;
    }

    @Override
    public boolean apply(QueueEntry input) {
      return input.getPlayerId() == playerId;
    }
  }

  private final BeanFactory beanFactory;
  private final PlayerDataSource playerDataSource;
  private final Map<String, Integer> teamTokens;
  private final ListMultimap<Integer, QueueEntry> queues;

  @Inject
  public QueueServlet(BeanFactory beanFactory,
      PlayerDataSource playerDataSource,
      @TeamTokens Map<String, Integer> teamTokens,
      @Queues ListMultimap<Integer, QueueEntry> queues) {
    this.beanFactory = beanFactory;
    this.playerDataSource = playerDataSource;
    this.teamTokens = teamTokens;
    this.queues = queues;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String requestStr = CharStreams.toString(req.getReader());
    if (req.getPathInfo().endsWith(ServletEndpoints.QUEUE_GET)) {
      GetPlayerQueueRequest request =
          AutoBeanCodex.decode(beanFactory, GetPlayerQueueRequest.class, requestStr).as();
      AutoBean<GetPlayerQueueResponse> response = beanFactory.createPlayerQueueResponse();
      List<QueueEntry> queue = queues.get(teamTokens.get(request.getTeamToken()));
      synchronized (queues) {
        response.as().setQueue(Lists.newArrayList(queue));
      }

      resp.getWriter().append(AutoBeanCodex.encode(response).getPayload());
    } else if (req.getPathInfo().endsWith(ServletEndpoints.QUEUE_ADD)) {
      EnqueueOrDequeuePlayerRequest request =
          AutoBeanCodex.decode(beanFactory, EnqueueOrDequeuePlayerRequest.class, requestStr).as();
      try {
        QueueEntry queueEntry = beanFactory.createQueueEntry().as();
        queueEntry.setPlayerId(request.getPlayerId());
        playerDataSource.populateQueueEntry(queueEntry);
        // TODO(m3): persist to database
        Integer team = teamTokens.get(request.getTeamToken());
        if (request.getPosition() != null) {
          List<QueueEntry> queue = queues.get(team);
          synchronized (queues) {
              queue.add(request.getPosition(), queueEntry);
          }
        } else {
          queues.put(team, queueEntry);
        }
      } catch (SQLException e) {
        log(e.getMessage(), e);
        resp.setStatus(500);
      }
    } else if (req.getPathInfo().endsWith(ServletEndpoints.QUEUE_REMOVE)) {
      EnqueueOrDequeuePlayerRequest request =
          AutoBeanCodex.decode(beanFactory, EnqueueOrDequeuePlayerRequest.class, requestStr).as();
      List<QueueEntry> queue = queues.get(teamTokens.get(request.getTeamToken()));
      synchronized (queues) {
        // TODO(m3): persist to database
        Iterables.removeIf(queue, new QueueEntryPredicate(request.getPlayerId()));
      }
    } else if (req.getPathInfo().endsWith(ServletEndpoints.QUEUE_REORDER)) {
      ReorderPlayerQueueRequest request =
          AutoBeanCodex.decode(beanFactory, ReorderPlayerQueueRequest.class, requestStr).as();
      List<QueueEntry> queue = queues.get(teamTokens.get(request.getTeamToken()));
      synchronized (queues) {
        int startIndex = Iterables.indexOf(queue, new QueueEntryPredicate(request.getPlayerId()));
        int endIndex = Math.min(request.getNewPosition(), queue.size());
        // TODO(m3): persist to database
        if (startIndex < endIndex) {
          Collections.rotate(queue.subList(startIndex, endIndex + 1), -1);
        } else if (endIndex < startIndex) {
          Collections.rotate(queue.subList(endIndex + 1, startIndex + 1), 1);
        }
      }
    } else {
      resp.setStatus(404);
    }
    resp.setContentType("text/json");
  }
}