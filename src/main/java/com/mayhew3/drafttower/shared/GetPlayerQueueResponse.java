package com.mayhew3.drafttower.shared;

import java.util.List;

/**
 * Response object for players queue.
 */
public interface GetPlayerQueueResponse {

  List<QueueEntry> getQueue();
  void setQueue(List<QueueEntry> queue);
}