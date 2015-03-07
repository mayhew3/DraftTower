package com.mayhew3.drafttower.shared;

import java.util.Map;

/**
 * Message object for communicating current draft status to clients.
 */
public interface ClientDraftStatus {
  DraftStatus getDraftStatus();
  void setDraftStatus(DraftStatus draftStatus);

  Map<Long, Float> getPickPredictions();
  void setPickPredictions(Map<Long, Float> pickPredictions);
}