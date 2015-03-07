package com.mayhew3.drafttower.client.teamorder;

import com.google.gwt.event.shared.EventBus;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.client.events.LoginEvent;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.DraftStatusTestUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@link TeamOrderPresenter}.
 */
public class TeamOrderPresenterTest {

  private static final int MY_TEAM = 4;

  private BeanFactory beanFactory;
  private TeamsInfo teamsInfo;
  private List<DraftPick> picks = new ArrayList<>();
  private TeamOrderPresenter presenter;
  private TeamOrderView view;

  @Before
  public void setUp() {
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    teamsInfo = Mockito.mock(TeamsInfo.class);
    Mockito.when(teamsInfo.getShortTeamName(Mockito.anyInt()))
        .thenAnswer(new Answer<Object>() {
          @Override
          public Object answer(InvocationOnMock invocation) throws Throwable {
            return "Team " + invocation.getArguments()[0];
          }
        });
    Mockito.when(teamsInfo.getTeam()).thenReturn(MY_TEAM);
    Mockito.when(teamsInfo.isMyPick(Mockito.any(DraftStatus.class)))
        .thenReturn(false);
    Mockito.when(teamsInfo.isOnDeck(Mockito.any(DraftStatus.class)))
        .thenReturn(false);
    presenter = new TeamOrderPresenter(10,
        teamsInfo,
        Mockito.mock(EventBus.class));
    view = Mockito.mock(TeamOrderView.class);
    presenter.setView(view);
  }

  @Test
  public void testTeamNamesSetOnLogin() {
    presenter.onLogin(Mockito.mock(LoginEvent.class));
    for (int i = 1; i <= 10; i++) {
      Mockito.verify(view).setTeamName(i, "Team " + i);
      Mockito.verify(view).setMe(i, i == MY_TEAM);
    }
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testRoundTextNoPicks() {
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).setRoundText("Round 1");
  }

  @Test
  public void testRoundTextMidRound1() {
    for (int i = 0; i < 5; i++) {
      picks.add(DraftStatusTestUtil.createDraftPick(i, "", false, beanFactory));
    }
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).setRoundText("Round 1");
  }

  @Test
  public void testRoundTextEndRound1() {
    for (int i = 0; i < 9; i++) {
      picks.add(DraftStatusTestUtil.createDraftPick(i, "", false, beanFactory));
    }
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).setRoundText("Round 1");
  }

  @Test
  public void testRoundTextStartRound2() {
    for (int i = 0; i < 10; i++) {
      picks.add(DraftStatusTestUtil.createDraftPick(i, "", false, beanFactory));
    }
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).setRoundText("Round 2");
  }

  @Test
  public void testRoundTextItsOver() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(picks, beanFactory);
    draftStatus.setOver(true);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verify(view).setRoundText("It's over!");
  }

  @Test
  public void testSetCurrentTeam() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(picks, beanFactory);
    draftStatus.setCurrentTeam(6);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    for (int i = 1; i <= 10; i++) {
      Mockito.verify(view).setCurrent(i, i == 6);
    }
  }

  @Test
  public void testSetDisconnected() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(picks, beanFactory);
    draftStatus.getConnectedTeams().add(3);
    draftStatus.getConnectedTeams().add(9);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    for (int i = 1; i <= 10; i++) {
      Mockito.verify(view).setDisconnected(i, i != 3 && i != 9);
    }
  }

  @Test
  public void testSetRobot() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(picks, beanFactory);
    draftStatus.getRobotTeams().add(3);
    draftStatus.getRobotTeams().add(9);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    for (int i = 1; i <= 10; i++) {
      Mockito.verify(view).setRobot(i, i == 3 || i == 9);
    }
  }

  @Test
  public void testSetNextPickKeeper() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(picks, beanFactory);
    draftStatus.getNextPickKeeperTeams().add(3);
    draftStatus.getNextPickKeeperTeams().add(9);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    for (int i = 1; i <= 10; i++) {
      Mockito.verify(view).setKeeper(i, i == 3 || i == 9);
    }
  }

  @Test
  public void testStatusEmptyNotStarted() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(picks, beanFactory);
    draftStatus.setCurrentPickDeadline(0);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verify(view).setStatus("");
  }

  @Test
  public void testStatusCleared() {
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).setStatus("");
  }

  @Test
  public void testStatusSetOnDeck() {
    Mockito.when(teamsInfo.isOnDeck(Mockito.any(DraftStatus.class)))
        .thenReturn(true);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).setStatus("On deck!");
  }

  @Test
  public void testStatusSetYourPick() {
    Mockito.when(teamsInfo.isMyPick(Mockito.any(DraftStatus.class)))
        .thenReturn(true);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).setStatus("Your pick!");
  }

  @Test
  public void testStatusClearedDraftOver() {
    Mockito.when(teamsInfo.isMyPick(Mockito.any(DraftStatus.class)))
        .thenReturn(true);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(picks, beanFactory);
    draftStatus.setOver(true);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verify(view).setStatus("");
  }

  @Test
  public void testStatusAfterReset() {
    Mockito.when(teamsInfo.isOnDeck(Mockito.any(DraftStatus.class)))
        .thenReturn(true);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).setStatus("On deck!");
    Mockito.when(teamsInfo.isOnDeck(Mockito.any(DraftStatus.class)))
        .thenReturn(false);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(picks, beanFactory);
    draftStatus.setCurrentPickDeadline(0);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verify(view).setStatus("");
  }
}