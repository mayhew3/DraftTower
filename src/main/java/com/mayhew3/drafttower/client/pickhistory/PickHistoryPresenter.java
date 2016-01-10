package com.mayhew3.drafttower.client.pickhistory;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.ListDataProvider;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.client.pickhistory.PickHistoryPresenter.PickHistoryInfo;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.RosterUtil;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Presenter for pick history table.
 */
public class PickHistoryPresenter extends ListDataProvider<PickHistoryInfo> implements
    DraftStatusChangedEvent.Handler {

  static class PickHistoryInfo {
    private final String pickNumber;
    private final String teamName;
    private final String playerName;
    private final String primaryPosition;
    private final boolean isKeeper;

    PickHistoryInfo(String pickNumber,
        String teamName,
        String playerName,
        String primaryPosition,
        boolean keeper) {
      this.pickNumber = pickNumber;
      this.teamName = teamName;
      this.playerName = playerName;
      this.primaryPosition = primaryPosition;
      isKeeper = keeper;
    }

    String getPickNumber() {
      return pickNumber;
    }

    String getTeamName() {
      return teamName;
    }

    String getPlayerName() {
      return playerName;
    }

    public String getPrimaryPosition() {
      return primaryPosition;
    }

    boolean isKeeper() {
      return isKeeper;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      PickHistoryInfo that = (PickHistoryInfo) o;

      if (isKeeper != that.isKeeper) return false;
      if (!pickNumber.equals(that.pickNumber)) return false;
      if (!playerName.equals(that.playerName)) return false;
      if (!primaryPosition.equals(that.primaryPosition)) return false;
      if (!teamName.equals(that.teamName)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = pickNumber.hashCode();
      result = 31 * result + teamName.hashCode();
      result = 31 * result + playerName.hashCode();
      result = 31 * result + primaryPosition.hashCode();
      result = 31 * result + (isKeeper ? 1 : 0);
      return result;
    }
  }

  private final TeamsInfo teamsInfo;
  private final int numTeams;

  @Inject
  public PickHistoryPresenter(final TeamsInfo teamsInfo,
      @NumTeams int numTeams,
      EventBus eventBus) {
    this.teamsInfo = teamsInfo;
    this.numTeams = numTeams;
    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    List<PickHistoryInfo> list = new ArrayList<>();
    List<DraftPick> draftPicks = Lists.reverse(event.getStatus().getDraftStatus().getPicks());
    for (DraftPick pick : draftPicks) {
      list.add(new PickHistoryInfo(
          getPickNumber(pick, draftPicks),
          teamsInfo.getShortTeamName(pick.getTeam()),
          pick.getPlayerName(),
          RosterUtil.getHighestValuePosition(pick.getEligibilities()),
          pick.isKeeper()));
    }
    setList(list);
  }

  private String getPickNumber(DraftPick pick, List<DraftPick> picks) {
    int overallPick = picks.size() - picks.indexOf(pick);
    int round = (overallPick - 1) / numTeams + 1;
    int pickNum = ((overallPick-1) % numTeams) + 1;
    return round + ":" + pickNum;
  }
}