package com.mayhew3.drafttower.server;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.mayhew3.drafttower.server.BindingAnnotations.Queues;
import com.mayhew3.drafttower.shared.*;

import javax.inject.Singleton;
import java.util.List;

/**
 * Handles queue operations.
 */
@Singleton
public class QueueHandler {

  private final BeanFactory beanFactory;
  private final PlayerDataSource playerDataSource;
  private final ListMultimap<TeamDraftOrder, QueueEntry> queues;
  private final DraftStatus status;
  private final Lock lock;

  @Inject
  public QueueHandler(BeanFactory beanFactory,
      PlayerDataSource playerDataSource,
      @Queues ListMultimap<TeamDraftOrder, QueueEntry> queues,
      DraftStatus status,
      Lock lock) {
    this.beanFactory = beanFactory;
    this.playerDataSource = playerDataSource;
    this.queues = queues;
    this.status = status;
    this.lock = lock;
  }

  public List<QueueEntry> getQueue(TeamDraftOrder team) {
    synchronized (queues) {
      List<QueueEntry> queue = queues.get(team);
      return Lists.newArrayList(queue);
    }
  }

  public void enqueue(TeamDraftOrder team,
      final long playerId,
      Integer position) throws DataSourceException {
    try (Lock ignored = lock.lock()) {
      if (Iterables.any(status.getPicks(), new Predicate<DraftPick>() {
            @Override
            public boolean apply(DraftPick pick) {
              return pick.getPlayerId() == playerId;
            }
          })) {
        List<QueueEntry> queue = queues.get(team);
        synchronized (queues) {
          Iterables.removeIf(queue, new QueueEntryPredicate(playerId));
        }
        return;
      }
    }
    QueueEntry queueEntry = beanFactory.createQueueEntry().as();
    queueEntry.setPlayerId(playerId);
    playerDataSource.populateQueueEntry(queueEntry);
    if (position != null) {
      List<QueueEntry> queue = queues.get(team);
      synchronized (queues) {
        if (queue.isEmpty()) {
          queue.add(queueEntry);
        } else {
          position = Math.min(position, queue.size());
          queue.add(position, queueEntry);
        }
      }
    } else {
      synchronized (queues) {
        queues.put(team, queueEntry);
      }
    }
  }

  public void dequeue(TeamDraftOrder team, long playerId) {
    List<QueueEntry> queue = queues.get(team);
    synchronized (queues) {
      Iterables.removeIf(queue, new QueueEntryPredicate(playerId));
    }
  }

  public void reorderQueue(TeamDraftOrder team,
      long playerId,
      int newPosition) {
    List<QueueEntry> queue = queues.get(team);
    newPosition = Math.min(newPosition, queue.size());
    synchronized (queues) {
      int oldPosition = Iterables.indexOf(queue, new QueueEntryPredicate(playerId));
      if (oldPosition != -1) {
        if (oldPosition != newPosition) {
          if (oldPosition < newPosition) {
            newPosition--;
          }
          QueueEntry entry = queue.remove(oldPosition);
          queue.add(newPosition, entry);
        }
      }
    }
  }
}