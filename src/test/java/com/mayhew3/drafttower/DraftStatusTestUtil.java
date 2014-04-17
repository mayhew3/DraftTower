package com.mayhew3.drafttower;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.DraftStatus;

import java.util.HashSet;
import java.util.List;

/**
 * Convenience methods for testing draft status.
 */
public class DraftStatusTestUtil {
  public static DraftStatus createDraftStatus(List<DraftPick> picks, BeanFactory beanFactory) {
    DraftStatus draftStatus = beanFactory.createDraftStatus().as();
    draftStatus.setPicks(Lists.newArrayList(picks));
    draftStatus.setCurrentPickDeadline(1);
    draftStatus.setCurrentTeam(picks.isEmpty()
        ? 1
        : picks.get(picks.size() - 1).getTeam() + 1);
    draftStatus.setConnectedTeams(new HashSet<Integer>());
    draftStatus.setNextPickKeeperTeams(new HashSet<Integer>());
    draftStatus.setRobotTeams(new HashSet<Integer>());
    return draftStatus;
  }

  public static DraftPick createDraftPick(int team, String name, boolean isKeeper, BeanFactory beanFactory) {
    return createDraftPick(team, name, isKeeper, "P", 1, beanFactory);
  }

  public static DraftPick createDraftPick(int team, String name, boolean isKeeper, String position, BeanFactory beanFactory) {
    return createDraftPick(team, name, isKeeper, position, 1, beanFactory);
  }

  public static DraftPick createDraftPick(int team, String name, boolean isKeeper, String position, long id, BeanFactory beanFactory) {
    DraftPick draftPick = beanFactory.createDraftPick().as();
    draftPick.setTeam(team);
    draftPick.setEligibilities(ImmutableList.of(position));
    draftPick.setKeeper(isKeeper);
    draftPick.setPlayerName(name);
    draftPick.setPlayerId(id);
    return draftPick;
  }
}