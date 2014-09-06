package com.mayhew3.drafttower.client;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Provider;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.client.events.*;
import com.mayhew3.drafttower.client.websocket.Websocket;
import com.mayhew3.drafttower.shared.*;
import com.mayhew3.drafttower.shared.DraftCommand.Command;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;

/**
 * Tests for {@link DraftSocketHandler}.
 */
public class DraftSocketHandlerTest {

  private BeanFactory beanFactory;
  private Websocket socket;
  private int socketState = 1;
  private DraftSocketHandler handler;
  private EventBus eventBus;
  private double currentTime;
  private OpenPositions openPositions;

  @Before
  public void setUp() {
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    socket = Mockito.mock(Websocket.class);
    TeamsInfo teamsInfo = Mockito.mock(TeamsInfo.class);
    Mockito.when(teamsInfo.getTeamToken()).thenReturn("teamToken");
    openPositions = Mockito.mock(OpenPositions.class);
    eventBus = Mockito.mock(EventBus.class);
    SchedulerWrapper scheduler = Mockito.mock(SchedulerWrapper.class);
    final ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        runnableArgumentCaptor.getValue().run();
        return null;
      }
    }).when(scheduler).schedule(runnableArgumentCaptor.capture(), Mockito.anyInt());
    Provider<Double> currentTimeProvider = new Provider<Double>() {
      @Override
      public Double get() {
        return currentTime;
      }
    };
    handler = new DraftSocketHandler(
        beanFactory,
        socket,
        teamsInfo,
        openPositions,
        eventBus,
        scheduler,
        currentTimeProvider);

    Mockito.reset(socket, eventBus);
    Mockito.when(socket.getState()).thenAnswer(new Answer<Integer>() {
      @Override
      public Integer answer(InvocationOnMock invocation) throws Throwable {
        return socketState;
      }
    });
  }

  @Test
  public void testOnOpen() {
    InOrder inOrder = Mockito.inOrder(socket);
    handler.onOpen();
    Mockito.verify(eventBus).fireEvent(Mockito.isA(SocketConnectEvent.class));
    inOrder.verify(socket, Mockito.calls(1)).send(
        getExpectedDraftCommandPayload(Command.IDENTIFY, null));
    inOrder.verify(socket, Mockito.calls(5)).send(Mockito.matches("sync\\d+.\\d+"));
  }

  @Test
  public void testOnOpenSendsQueuedCommands() {
    InOrder inOrder = Mockito.inOrder(socket);
    socketState = 0;
    handler.sendMessage("foo1");
    handler.sendMessage("foo2");
    handler.sendMessage("foo3");
    socketState = 1;
    handler.onOpen();
    inOrder.verify(socket, Mockito.calls(6)).send(Mockito.anyString());
    inOrder.verify(socket, Mockito.calls(1)).send("foo1");
    inOrder.verify(socket, Mockito.calls(1)).send("foo2");
    inOrder.verify(socket, Mockito.calls(1)).send("foo3");
  }

  @Test
  public void testOnOpenDoesntSendQueuedCommandsWhenSocketDies() {
    Mockito.doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        socketState = 0;
        return null;
      }
    }).when(socket).send(Mockito.matches("sync\\d+.\\d+"));
    socketState = 0;
    handler.sendMessage("foo1");
    handler.sendMessage("foo2");
    handler.sendMessage("foo3");
    socketState = 1;
    try {
      handler.onOpen();
      Assert.fail("expected exception");
    } catch (RuntimeException e) {
      // ok
    }
    Assert.assertFalse(handler.queuedMsgs.isEmpty());
  }

  @Test
  public void testOnDraftStatusMessage() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.newArrayList(DraftStatusTestUtil.createDraftPick(1, "name", false, beanFactory)),
        beanFactory);
    draftStatus.setSerialId(1);
    handler.onMessage(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(draftStatus)).getPayload());
    Mockito.verify(openPositions).onDraftStatusChanged(Mockito.argThat(new AutoBeanMatcher<>(draftStatus)));
    ArgumentCaptor<DraftStatusChangedEvent> eventArgumentCaptor = ArgumentCaptor.forClass(DraftStatusChangedEvent.class);
    Mockito.verify(eventBus).fireEvent(eventArgumentCaptor.capture());
    Assert.assertTrue(AutoBeanUtils.deepEquals(
        AutoBeanUtils.getAutoBean(draftStatus),
        AutoBeanUtils.getAutoBean(eventArgumentCaptor.getValue().getStatus())));
  }

  @Test
  public void testOnDraftStatusMessageIgnoresDuplicate() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.newArrayList(DraftStatusTestUtil.createDraftPick(1, "name", false, beanFactory)),
        beanFactory);
    draftStatus.setSerialId(1);
    handler.onMessage(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(draftStatus)).getPayload());
    Mockito.reset(openPositions, eventBus);
    handler.onMessage(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(draftStatus)).getPayload());
    Mockito.verifyZeroInteractions(openPositions, eventBus);
  }

  @Test
  public void testOnDraftStatusMessageIgnoresOutOfOrder() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.newArrayList(DraftStatusTestUtil.createDraftPick(1, "name", false, beanFactory)),
        beanFactory);
    draftStatus.setSerialId(2);
    handler.onMessage(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(draftStatus)).getPayload());
    Mockito.reset(openPositions, eventBus);
    draftStatus.setSerialId(1);
    handler.onMessage(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(draftStatus)).getPayload());
    Mockito.verifyZeroInteractions(openPositions, eventBus);
  }

  @Test
  public void testClockSync() {
    // Clock skew is reported at 0 if no syncs have taken place.
    Assert.assertEquals(0, handler.getServerClockDiff());
    // Assume server skew of +20ms.
    // First sync: latency 50ms (20ms there, 30ms back).
    currentTime = 50;
    handler.onMessage("sync0.0-40");
    // Not enough data to run full algorithm yet; getter returns first result.
    Assert.assertEquals(15, handler.getServerClockDiff());
    // Second sync: latency 80ms (40ms there, 40ms back).
    currentTime = 130;
    handler.onMessage("sync50.0-110");
    // Third sync: latency 30ms (20ms there, 10ms back).
    currentTime = 160;
    handler.onMessage("sync130.0-170");
    // Fourth sync: latency 75ms (40ms there, 35ms back).
    currentTime = 235;
    handler.onMessage("sync160.0-220");
    // Fifth sync: latency 1500ms (50ms there, 1450ms back).
    currentTime = 1735;
    handler.onMessage("sync235.0-305");
    // Recorded diffs were: 15 20 25 22 -680
    // Median is 22
    // Last value should be discarded because >1 stdDev
    // Mean of remaining values is 82 / 4 = 20
    Assert.assertEquals(20, handler.getServerClockDiff());
  }

  @Test
  public void testOnCloseBadTeamToken() {
    InOrder inOrder = Mockito.inOrder(eventBus);
    handler.onClose(SocketTerminationReason.BAD_TEAM_TOKEN);
    inOrder.verify(eventBus, Mockito.calls(1)).fireEvent(Mockito.isA(SocketDisconnectEvent.class));
    inOrder.verify(eventBus, Mockito.calls(1)).fireEvent(Mockito.isA(ReloadWindowEvent.class));
    Mockito.verifyNoMoreInteractions(eventBus, socket);
  }

  @Test
  public void testOnCloseTeamAlreadyConnected() {
    InOrder inOrder = Mockito.inOrder(eventBus);
    handler.onClose(SocketTerminationReason.TEAM_ALREADY_CONNECTED);
    inOrder.verify(eventBus, Mockito.calls(1)).fireEvent(Mockito.isA(SocketDisconnectEvent.class));
    inOrder.verify(eventBus, Mockito.calls(1)).fireEvent(Mockito.isA(ReloadWindowEvent.class));
    Mockito.verifyNoMoreInteractions(eventBus, socket);
  }

  @Test
  public void testOnCloseOtherReason() {
    handler.onClose(SocketTerminationReason.UNKNOWN_REASON);
    Mockito.verify(eventBus).fireEvent(Mockito.isA(SocketDisconnectEvent.class));
    Mockito.verify(socket).open();
    Mockito.verifyNoMoreInteractions(eventBus, socket);
  }

  @Test
  public void testOnLogin() {
    handler.onLogin(new LoginEvent(null));
    Mockito.verify(socket).open();
  }

  @Test
  public void testOnPlayPauseNotStarted() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(new ArrayList<DraftPick>(), beanFactory);
    draftStatus.setSerialId(1);
    draftStatus.setCurrentPickDeadline(0);
    handler.onMessage(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(draftStatus)).getPayload());
    handler.onPlayPause(new PlayPauseEvent());
    Mockito.verify(socket).send(getExpectedDraftCommandPayload(Command.START_DRAFT, null));
  }

  @Test
  public void testOnPlayPauseNotPaused() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(new ArrayList<DraftPick>(), beanFactory);
    draftStatus.setSerialId(1);
    draftStatus.setCurrentPickDeadline(1);
    handler.onMessage(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(draftStatus)).getPayload());
    handler.onPlayPause(new PlayPauseEvent());
    Mockito.verify(socket).send(getExpectedDraftCommandPayload(Command.PAUSE, null));
  }

  @Test
  public void testOnPlayPausePaused() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(new ArrayList<DraftPick>(), beanFactory);
    draftStatus.setSerialId(1);
    draftStatus.setCurrentPickDeadline(1);
    draftStatus.setPaused(true);
    handler.onMessage(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(draftStatus)).getPayload());
    handler.onPlayPause(new PlayPauseEvent());
    Mockito.verify(socket).send(getExpectedDraftCommandPayload(Command.RESUME, null));
  }

  @Test
  public void testOnPlayerPicked() {
    handler.onPlayerPicked(new PickPlayerEvent(5));
    Mockito.verify(socket).send(getExpectedDraftCommandPayload(Command.DO_PICK, 5l));
  }

  @Test
  public void testOnBackOutPick() {
    handler.onBackOutPick(new BackOutPickEvent());
    Mockito.verify(socket).send(getExpectedDraftCommandPayload(Command.BACK_OUT, null));
  }

  @Test
  public void testOnForcePick() {
    handler.onForcePick(new ForcePickPlayerEvent(5l));
    Mockito.verify(socket).send(getExpectedDraftCommandPayload(Command.FORCE_PICK, 5l));
  }

  @Test
  public void testOnForcePickNoPlayer() {
    handler.onForcePick(new ForcePickPlayerEvent(null));
    Mockito.verify(socket).send(getExpectedDraftCommandPayload(Command.FORCE_PICK, null));
  }

  @Test
  public void testOnWakeUp() {
    handler.onWakeUp(new WakeUpEvent());
    Mockito.verify(socket).send(getExpectedDraftCommandPayload(Command.WAKE_UP, null));
  }

  private String getExpectedDraftCommandPayload(Command commandType, Long playerId) {
    AutoBean<DraftCommand> commandBean = beanFactory.createDraftCommand();
    commandBean.as().setCommandType(commandType);
    commandBean.as().setTeamToken("teamToken");
    if (playerId != null) {
      commandBean.as().setPlayerId(playerId);
    }
    return AutoBeanCodex.encode(commandBean).getPayload();
  }
}