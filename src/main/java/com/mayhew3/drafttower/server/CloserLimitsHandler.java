package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.server.BindingAnnotations.MaxClosers;
import com.mayhew3.drafttower.server.BindingAnnotations.MinClosers;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Map;

public class CloserLimitsHandler implements Serializable {
  private final TeamDataSource teamDataSource;
  private final Map<TeamDraftOrder, Integer> minClosers;
  private final Map<TeamDraftOrder, Integer> maxClosers;

  @Inject
  public CloserLimitsHandler(
      TeamDataSource teamDataSource,
      @MinClosers Map<TeamDraftOrder, Integer> minClosers,
      @MaxClosers Map<TeamDraftOrder, Integer> maxClosers) {
    this.teamDataSource = teamDataSource;
    this.minClosers = minClosers;
    this.maxClosers = maxClosers;
  }

  public void setCloserLimits(TeamDraftOrder teamDraftOrder, int minClosersToSet, int maxClosersToSet) {
    if (minClosersToSet >= 0 &&
        minClosersToSet <= 7 &&
        maxClosersToSet >= 0 &&
        maxClosersToSet <= 7 &&
        maxClosersToSet >= minClosersToSet) {
      minClosers.put(teamDraftOrder, minClosersToSet);
      maxClosers.put(teamDraftOrder, maxClosersToSet);

      teamDataSource.updateCloserLimits(teamDraftOrder, minClosersToSet, maxClosersToSet);
    }
  }
}