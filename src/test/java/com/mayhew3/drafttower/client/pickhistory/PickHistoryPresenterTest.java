package com.mayhew3.drafttower.client.pickhistory;

import com.google.gwt.event.shared.EventBus;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.client.pickhistory.PickHistoryPresenter.PickHistoryInfo;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.DraftStatusTestUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@link PickHistoryPresenter}.
 */
public class PickHistoryPresenterTest {

  private BeanFactory beanFactory;
  private PickHistoryPresenter presenter;

  @Before
  public void setUp() {
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    TeamsInfo teamsInfo = Mockito.mock(TeamsInfo.class);
    Mockito.when(teamsInfo.getShortTeamName(Mockito.anyInt()))
        .then(new Answer<Object>() {
          @Override
          public Object answer(InvocationOnMock invocation) throws Throwable {
            return Integer.toString((Integer) invocation.getArguments()[0]);
          }
        });
    presenter = new PickHistoryPresenter(
        teamsInfo,
        10,
        Mockito.mock(EventBus.class));
  }

  @Test
  public void testDraftStatusChanged() {
    List<DraftPick> picks = new ArrayList<>();
    List<PickHistoryInfo> expected = new ArrayList<>();
    for (int round = 1; round <= 3; round++) {
      for (int team = 1; team <= 10; team++) {
        boolean keeper = round == 1 && team % 3 == 0;
        String playerName = Integer.toString(round + team);
        picks.add(DraftStatusTestUtil.createDraftPick(team, playerName, keeper, beanFactory));
        expected.add(0, new PickHistoryInfo(
            round + ":" + team, Integer.toString(team), playerName, "P", keeper));
      }
    }
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Assert.assertEquals(expected, presenter.getList());
  }
}