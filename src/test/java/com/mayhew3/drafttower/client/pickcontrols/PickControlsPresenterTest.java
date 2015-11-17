package com.mayhew3.drafttower.client.pickcontrols;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventBus;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.client.events.PlayerSelectedEvent;
import com.mayhew3.drafttower.client.players.queue.QueueDataProvider;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.DraftStatusTestUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

/**
 * Tests for {@link PickControlsPresenter}.
 */
public class PickControlsPresenterTest {

  private static final int MY_TEAM = 4;

  private BeanFactory beanFactory;
  private PickControlsPresenter presenter;
  private PickControlsView view;
  private TeamsInfo teamsInfo;
  private QueueDataProvider queueDataProvider;

  @Before
  public void setUp() {
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    teamsInfo = Mockito.mock(TeamsInfo.class);
    Mockito.when(teamsInfo.isLoggedIn()).thenReturn(true);
    Mockito.when(teamsInfo.getTeam()).thenReturn(MY_TEAM);
    Mockito.when(teamsInfo.isCommissionerTeam()).thenReturn(false);
    queueDataProvider = Mockito.mock(QueueDataProvider.class);
    Mockito.when(queueDataProvider.isPlayerQueued(Mockito.anyLong())).thenReturn(false);
    Mockito.when(queueDataProvider.isPlayerQueued(2)).thenReturn(true);
    presenter = new PickControlsPresenter(
        teamsInfo,
        queueDataProvider,
        Mockito.mock(EventBus.class));
    view = Mockito.mock(PickControlsView.class);
    presenter.setView(view);
  }

  @Test
  public void testPickDisabledOnDraftStatusChangeNotStarted() {
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.reset(view);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(new ArrayList<DraftPick>(), beanFactory);
    draftStatus.setCurrentTeam(MY_TEAM);
    draftStatus.setCurrentPickDeadline(0);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verify(view).setPickEnabled(false);
  }

  @Test
  public void testPickDisabledOnDraftStatusChangeNotLoggedIn() {
    Mockito.when(teamsInfo.isLoggedIn()).thenReturn(false);
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.reset(view);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(new ArrayList<DraftPick>(), beanFactory);
    draftStatus.setCurrentTeam(MY_TEAM);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verify(view).setPickEnabled(false);
  }

  @Test
  public void testPickDisabledOnDraftStatusChangeNotMyTurn() {
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(new ArrayList<DraftPick>(), beanFactory)));
    Mockito.verify(view).setPickEnabled(false);
  }

  @Test
  public void testPickDisabledOnSelectionNotMyTurn() {
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(new ArrayList<DraftPick>(), beanFactory)));
    Mockito.reset(view);
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.verify(view).setPickEnabled(false);
  }

  @Test
  public void testPickDisabledOnDraftStatusChangeMyTurnNoSelection() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(new ArrayList<DraftPick>(), beanFactory);
    draftStatus.setCurrentTeam(MY_TEAM);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verify(view).setPickEnabled(false);
  }

  @Test
  public void testPickDisabledOnDraftStatusChangeMyTurnPaused() {
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.reset(view);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(new ArrayList<DraftPick>(), beanFactory);
    draftStatus.setCurrentTeam(MY_TEAM);
    draftStatus.setPaused(true);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verify(view).setPickEnabled(false);
  }

  @Test
  public void testPickDisabledOnSelectionMyTurnPaused() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(new ArrayList<DraftPick>(), beanFactory);
    draftStatus.setCurrentTeam(MY_TEAM);
    draftStatus.setPaused(true);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.reset(view);
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.verify(view).setPickEnabled(false);
  }

  @Test
  public void testPickEnabledOnDraftStatusChangeMyTurn() {
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.reset(view);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(new ArrayList<DraftPick>(), beanFactory);
    draftStatus.setCurrentTeam(MY_TEAM);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verify(view).setPickEnabled(true);
  }

  @Test
  public void testPickEnabledOnSelectionMyTurn() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(new ArrayList<DraftPick>(), beanFactory);
    draftStatus.setCurrentTeam(MY_TEAM);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.reset(view);
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.verify(view).setPickEnabled(true);
  }

  @Test
  public void testPickDisabledOnDraftStatusChangeSelectedPlayerPicked() {
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.reset(view);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.newArrayList(DraftStatusTestUtil.createDraftPick(1, "", false, "P", 1, beanFactory)),
        beanFactory);
    draftStatus.setCurrentTeam(MY_TEAM);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verify(view).setPickEnabled(false);
  }

  @Test
  public void testPickDisabledOnPick() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(new ArrayList<DraftPick>(), beanFactory);
    draftStatus.setCurrentTeam(MY_TEAM);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.reset(view);
    presenter.pick();
    Mockito.verify(view).setPickEnabled(false);
  }

  @Test
  public void testPickDisabledOnEnqueue() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(new ArrayList<DraftPick>(), beanFactory);
    draftStatus.setCurrentTeam(MY_TEAM);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.reset(view);
    presenter.enqueue();
    Mockito.verify(view).setPickEnabled(false);
  }

  @Test
  public void testPickDisabledOnForcePick() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(new ArrayList<DraftPick>(), beanFactory);
    draftStatus.setCurrentTeam(MY_TEAM);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.reset(view);
    presenter.forcePick();
    Mockito.verify(view).setPickEnabled(false);
  }

  @Test
  public void testEnqueueDisabledNoSelection() {
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(new ArrayList<DraftPick>(), beanFactory)));
    Mockito.verify(view).setEnqueueEnabled(false);
  }

  @Test
  public void testEnqueueEnabledOnSelection() {
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.verify(view).setEnqueueEnabled(true);
  }

  @Test
  public void testEnqueueEnabledOnDraftStatusChangeWithSelection() {
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.reset(view);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(new ArrayList<DraftPick>(), beanFactory)));
    Mockito.verify(view).setEnqueueEnabled(true);
  }

  @Test
  public void testEnqueueDisabledOnSelectionAlreadyQueued() {
    presenter.onPlayerSelected(new PlayerSelectedEvent(2, ""));
    Mockito.verify(view).setEnqueueEnabled(false);
  }

  @Test
  public void testEnqueueEnabledOnDraftStatusChangeSelectionAlreadyQueued() {
    presenter.onPlayerSelected(new PlayerSelectedEvent(2, ""));
    Mockito.reset(view);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(new ArrayList<DraftPick>(), beanFactory)));
    Mockito.verify(view).setEnqueueEnabled(false);
  }

  @Test
  public void testEnqueueDisabledOnDraftStatusChangeSelectedPlayerPicked() {
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.reset(view);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.newArrayList(DraftStatusTestUtil.createDraftPick(1, "", false, "P", 1, beanFactory)),
        beanFactory);
    draftStatus.setCurrentTeam(MY_TEAM);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verify(view).setEnqueueEnabled(false);
  }

  @Test
  public void testEnqueueDisabledOnPick() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(new ArrayList<DraftPick>(), beanFactory);
    draftStatus.setCurrentTeam(MY_TEAM);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.reset(view);
    presenter.pick();
    Mockito.verify(view).setEnqueueEnabled(false);
  }

  @Test
  public void testEnqueueDisabledOnEnqueue() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(new ArrayList<DraftPick>(), beanFactory);
    draftStatus.setCurrentTeam(MY_TEAM);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.reset(view);
    presenter.enqueue();
    Mockito.verify(view).setEnqueueEnabled(false);
  }

  @Test
  public void testEnqueueDisabledOnForcePick() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(new ArrayList<DraftPick>(), beanFactory);
    draftStatus.setCurrentTeam(MY_TEAM);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.reset(view);
    presenter.forcePick();
    Mockito.verify(view).setEnqueueEnabled(false);
  }

  @Test
  public void testForcePickDisabledOnDraftStatusChangeNotStarted() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(new ArrayList<DraftPick>(), beanFactory);
    draftStatus.setCurrentPickDeadline(0);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verify(view).setForcePickEnabled(false);
  }

  @Test
  public void testForcePickEnabledOnDraftStatusChange() {
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(new ArrayList<DraftPick>(), beanFactory)));
    Mockito.verify(view).setForcePickEnabled(true);
  }

  @Test
  public void testForcePickInvisibleOnDraftStatusChangeNotCommish() {
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(new ArrayList<DraftPick>(), beanFactory)));
    Mockito.verify(view).setForcePickVisible(false);
  }

  @Test
  public void testForcePickVisibleOnDraftStatusChangeCommish() {
    Mockito.when(teamsInfo.isCommissionerTeam()).thenReturn(true);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(new ArrayList<DraftPick>(), beanFactory)));
    Mockito.verify(view).setForcePickVisible(true);
  }

  @Test
  public void testResetDraftInvisibleOnDraftStatusChangeNotCommish() {
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(new ArrayList<DraftPick>(), beanFactory)));
    Mockito.verify(view).setForcePickVisible(false);
  }

  @Test
  public void testResetDraftVisibleOnDraftStatusChangeCommish() {
    Mockito.when(teamsInfo.isCommissionerTeam()).thenReturn(true);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(new ArrayList<DraftPick>(), beanFactory)));
    Mockito.verify(view).setResetVisible(true);
  }

  @Test
  public void testWakeUpInvisibleOnDraftStatusChangeNotRobot() {
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(new ArrayList<DraftPick>(), beanFactory)));
    Mockito.verify(view).setWakeUpVisible(false);
  }

  @Test
  public void testWakeUpVisibleOnDraftStatusChangeRobot() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(new ArrayList<DraftPick>(), beanFactory);
    draftStatus.getRobotTeams().add(MY_TEAM);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verify(view).setWakeUpVisible(true);
  }

  @Test
  public void testSetPlayerNameOnSelection() {
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, "player name"));
    Mockito.verify(view).setSelectedPlayerName("player name");
  }

  @Test
  public void testClearPlayerNameOnDraftStatusChangeSelectedPlayerPicked() {
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.reset(view);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.newArrayList(DraftStatusTestUtil.createDraftPick(1, "", false, "P", 1, beanFactory)),
        beanFactory);
    draftStatus.setCurrentTeam(MY_TEAM);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verify(view).clearSelectedPlayerName();
  }

  @Test
  public void testPlayerNameNotClearedOnDraftStatusChangeDifferentPlayerPicked() {
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.reset(view);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.newArrayList(DraftStatusTestUtil.createDraftPick(1, "", false, "P", 2, beanFactory)),
        beanFactory);
    draftStatus.setCurrentTeam(MY_TEAM);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verify(view, Mockito.never()).clearSelectedPlayerName();
  }

  @Test
  public void testPlayerNameNotClearedOnDraftStatusChangeNoSelection() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.newArrayList(DraftStatusTestUtil.createDraftPick(1, "", false, "P", 2, beanFactory)),
        beanFactory);
    draftStatus.setCurrentTeam(MY_TEAM);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verify(view, Mockito.never()).clearSelectedPlayerName();
  }

  @Test
  public void testClearPlayerNameOnPick() {
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.reset(view);
    presenter.pick();
    Mockito.verify(view).clearSelectedPlayerName();
  }

  @Test
  public void testClearPlayerNameOnEnqueue() {
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.reset(view);
    presenter.enqueue();
    Mockito.verify(view).clearSelectedPlayerName();
  }

  @Test
  public void testClearPlayerNameOnForcePick() {
    presenter.onPlayerSelected(new PlayerSelectedEvent(1, ""));
    Mockito.reset(view);
    presenter.forcePick();
    Mockito.verify(view).clearSelectedPlayerName();
  }
}