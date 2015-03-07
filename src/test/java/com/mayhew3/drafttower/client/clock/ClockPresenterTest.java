package com.mayhew3.drafttower.client.clock;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventBus;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.client.DraftSocketHandler;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.client.events.LoginEvent;
import com.mayhew3.drafttower.client.events.SocketDisconnectEvent;
import com.mayhew3.drafttower.shared.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Tests for {@link ClockPresenter}.
 */
public class ClockPresenterTest {

  private static final Integer TEST_CLOCK_DIFF = 100;

  private boolean isCommissioner = false;
  private long currentTime = 0;

  private ClockPresenter presenter;
  private ClockView view;
  private BeanFactory beanFactory;

  @Before
  public void setUp() {
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    DraftSocketHandler socketHandler = Mockito.mock(DraftSocketHandler.class);
    Mockito.when(socketHandler.getServerClockDiff()).thenReturn(TEST_CLOCK_DIFF);
    TeamsInfo teamsInfo = Mockito.mock(TeamsInfo.class);
    Mockito.when(teamsInfo.isCommissionerTeam()).thenAnswer(new Answer<Boolean>() {
      @Override
      public Boolean answer(InvocationOnMock invocation) throws Throwable {
        return isCommissioner;
      }
    });
    CurrentTimeProvider timeProvider = Mockito.mock(CurrentTimeProvider.class);
    Mockito.when(timeProvider.getCurrentTimeMillis()).thenAnswer(new Answer<Long>() {
      @Override
      public Long answer(InvocationOnMock invocation) throws Throwable {
        return currentTime;
      }
    });
    presenter = new ClockPresenter(
        socketHandler,
        teamsInfo,
        Mockito.mock(EventBus.class),
        timeProvider);
    view = Mockito.mock(ClockView.class);
    presenter.setView(view);
  }

  @Test
  public void testUpdateNoStatusYet() {
    presenter.update();
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testUpdateNotStarted() {
    DraftStatus status = DraftStatusTestUtil.createDraftStatus(
        Lists.<DraftPick>newArrayList(), beanFactory);
    status.setCurrentPickDeadline(0);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(status, beanFactory)));
    Mockito.verify(view).clear();
    Mockito.verify(view).updatePaused(false, true);
    Mockito.verify(view, Mockito.never()).updateTime(
        Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean());
  }

  @Test
  public void testUpdateItsOver() {
    DraftStatus status = DraftStatusTestUtil.createDraftStatus(
        Lists.<DraftPick>newArrayList(), beanFactory);
    status.setOver(true);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(status, beanFactory)));
    Mockito.verify(view).clear();
  }

  @Test
  public void testUpdateNotPausedCanPlay() {
    DraftStatus status = DraftStatusTestUtil.createDraftStatus(
        Lists.<DraftPick>newArrayList(), beanFactory);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(status, beanFactory)));
    Mockito.verify(view).updatePaused(false, false);
  }

  @Test
  public void testUpdatePaused() {
    DraftStatus status = DraftStatusTestUtil.createDraftStatus(
        Lists.<DraftPick>newArrayList(), beanFactory);
    status.setPaused(true);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(status, beanFactory)));
    Mockito.verify(view).updatePaused(true, true);
    Mockito.verify(view, Mockito.never()).updateTime(
        Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean());
  }

  @Test
  public void testUpdatePlentyOfTime() {
    DraftStatus status = DraftStatusTestUtil.createDraftStatus(
        Lists.<DraftPick>newArrayList(), beanFactory);
    status.setCurrentPickDeadline(65100);
    currentTime = 5000;
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(status, beanFactory)));
    Mockito.verify(view).updateTime(1, 0, false);
    Mockito.verify(view).updatePaused(false, false);
  }

  @Test
  public void testUpdateLowTime() {
    DraftStatus status = DraftStatusTestUtil.createDraftStatus(
        Lists.<DraftPick>newArrayList(), beanFactory);
    status.setCurrentPickDeadline(65100);
    currentTime = 60000;
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(status, beanFactory)));
    Mockito.verify(view).updateTime(0, 5, true);
    Mockito.verify(view).updatePaused(false, false);
  }

  @Test
  public void testUpdateNegativeTime() {
    DraftStatus status = DraftStatusTestUtil.createDraftStatus(
        Lists.<DraftPick>newArrayList(), beanFactory);
    status.setCurrentPickDeadline(65100);
    currentTime = 70000;
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(status, beanFactory)));
    Mockito.verify(view).updateTime(0, 0, true);
    Mockito.verify(view).updatePaused(false, false);
  }

  @Test
  public void testDisconnect() {
    presenter.onDisconnect(new SocketDisconnectEvent());
    Mockito.verify(view).clear();
  }

  @Test
  public void testLoginNotCommish() {
    presenter.onLogin(Mockito.mock(LoginEvent.class));
    Mockito.verify(view).setPlayPauseVisible(false);
  }

  @Test
  public void testLoginCommish() {
    isCommissioner = true;
    presenter.onLogin(Mockito.mock(LoginEvent.class));
    Mockito.verify(view).setPlayPauseVisible(true);
  }
}