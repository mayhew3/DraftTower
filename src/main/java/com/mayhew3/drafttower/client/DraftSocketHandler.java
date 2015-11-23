package com.mayhew3.drafttower.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.client.GinBindingAnnotations.CurrentTime;
import com.mayhew3.drafttower.client.events.*;
import com.mayhew3.drafttower.client.websocket.Websocket;
import com.mayhew3.drafttower.client.websocket.WebsocketListener;
import com.mayhew3.drafttower.shared.*;
import com.mayhew3.drafttower.shared.DraftCommand.Command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mayhew3.drafttower.shared.DraftCommand.Command.*;

/**
 * Class which handles communicating draft status and actions with the server.
 */
@Singleton
public class DraftSocketHandler implements
    WebsocketListener,
    LoginEvent.Handler,
    PlayPauseEvent.Handler,
    PickPlayerEvent.Handler,
    BackOutPickEvent.Handler,
    ForcePickPlayerEvent.Handler,
    WakeUpEvent.Handler,
    ResetDraftEvent.Handler,
    ClearCachesEvent.Handler,
    DisconnectClientEvent.Handler {

  private static final int CLOCK_SYNC_CYCLES = 5;

  private static final int INITIAL_BACKOFF_MS = 5;
  private static final int MAX_BACKOFF_MS = 5000;

  private final BeanFactory beanFactory;
  private final Websocket socket;
  private final SchedulerWrapper scheduler;
  private final Provider<Double> currentTimeProvider;
  private final TeamsInfo teamsInfo;
  private final OpenPositions openPositions;
  private final EventBus eventBus;

  private ClientDraftStatus draftStatus;
  private long latestStatusSerialId = -1;

  private final List<Integer> serverClockDiffs = new ArrayList<>();
  private int serverClockDiff;

  private int backoff = INITIAL_BACKOFF_MS;
  @VisibleForTesting final List<String> queuedMsgs = new ArrayList<>();

  @Inject
  public DraftSocketHandler(BeanFactory beanFactory,
      Websocket socket,
      TeamsInfo teamsInfo,
      OpenPositions openPositions,
      EventBus eventBus,
      SchedulerWrapper scheduler,
      @CurrentTime Provider<Double> currentTimeProvider) {
    this.beanFactory = beanFactory;
    this.teamsInfo = teamsInfo;
    this.openPositions = openPositions;
    this.socket = socket;
    this.scheduler = scheduler;
    this.currentTimeProvider = currentTimeProvider;
    socket.addListener(this);

    this.eventBus = eventBus;
    eventBus.addHandler(LoginEvent.TYPE, this);
    eventBus.addHandler(PlayPauseEvent.TYPE, this);
    eventBus.addHandler(PickPlayerEvent.TYPE, this);
    eventBus.addHandler(BackOutPickEvent.TYPE, this);
    eventBus.addHandler(ForcePickPlayerEvent.TYPE, this);
    eventBus.addHandler(WakeUpEvent.TYPE, this);
    eventBus.addHandler(ResetDraftEvent.TYPE, this);
    eventBus.addHandler(ClearCachesEvent.TYPE, this);
    eventBus.addHandler(DisconnectClientEvent.TYPE, this);
  }

  @Override
  public void onOpen() {
    eventBus.fireEvent(new SocketConnectEvent());
    sendDraftCommand(IDENTIFY);
    for (int i = 0; i < CLOCK_SYNC_CYCLES; i++) {
      scheduler.schedule(new Runnable() {
        @Override
        public void run() {
          socket.send(ServletEndpoints.CLOCK_SYNC + currentTimeProvider.get());
        }
      }, i * 2000);
    }
    for (String queuedMsg : queuedMsgs) {
      if (socket.getState() != 1) {
        throw new RuntimeException("Socket died while sending queued messages on open");
      }
      sendMessage(queuedMsg);
    }
    queuedMsgs.clear();
  }

  @Override
  public void onMessage(String msg) {
    if (msg.startsWith(ServletEndpoints.CLOCK_SYNC)) {
      processClockSync(msg);
    } else {
      backoff = INITIAL_BACKOFF_MS;
      draftStatus = AutoBeanCodex.decode(beanFactory, ClientDraftStatus.class, msg).as();
      long serialId = draftStatus.getDraftStatus().getSerialId();
      if (latestStatusSerialId < serialId) {
        openPositions.onDraftStatusChanged(draftStatus.getDraftStatus());
        eventBus.fireEvent(new DraftStatusChangedEvent(draftStatus));
        latestStatusSerialId = serialId;
      }
    }
  }

  @Override
  public void onClose(SocketTerminationReason reason) {
    eventBus.fireEvent(new SocketDisconnectEvent());
    if (reason.shouldReload()) {
      eventBus.fireEvent(new ReloadWindowEvent());
    } else {
      int scheduleDelay = backoff;
      backoff = Math.max(backoff * 2, MAX_BACKOFF_MS);
      scheduler.schedule(new Runnable() {
        @Override
        public void run() {
          socket.open();
        }
      }, scheduleDelay);
    }
  }

  private void sendDraftCommand(DraftCommand.Command commandType) {
    AutoBean<DraftCommand> commandBean = createDraftCommand(commandType);
    sendMessage(AutoBeanCodex.encode(commandBean).getPayload());
  }

  private void sendDraftCommand(Command commandType, Long playerId) {
    AutoBean<DraftCommand> commandBean = createDraftCommand(commandType);
    commandBean.as().setPlayerId(playerId);
    sendMessage(AutoBeanCodex.encode(commandBean).getPayload());
  }

  private AutoBean<DraftCommand> createDraftCommand(Command commandType) {
    AutoBean<DraftCommand> commandBean = beanFactory.createDraftCommand();
    DraftCommand command = commandBean.as();
    command.setCommandType(commandType);
    command.setTeamToken(teamsInfo.getTeamToken());
    return commandBean;
  }

  @VisibleForTesting void sendMessage(String msg) {
    if (socket.getState() == 1) {
      socket.send(msg);
    } else {
      queuedMsgs.add(msg);
    }
  }

  @Override
  public void onLogin(LoginEvent event) {
    socket.open();
  }

  @Override
  public void onPlayPause(PlayPauseEvent event) {
    if (draftStatus.getDraftStatus().getCurrentPickDeadline() == 0) {
      sendDraftCommand(START_DRAFT);
    } else if (draftStatus.getDraftStatus().isPaused()) {
      sendDraftCommand(RESUME);
    } else {
      sendDraftCommand(PAUSE);
    }
  }

  @Override
  public void onPlayerPicked(PickPlayerEvent event) {
    sendDraftCommand(DO_PICK, event.getPlayerId());
  }

  @Override
  public void onBackOutPick(BackOutPickEvent event) {
    sendDraftCommand(BACK_OUT);
  }

  @Override
  public void onForcePick(ForcePickPlayerEvent event) {
    sendDraftCommand(FORCE_PICK, event.getPlayerId());
  }

  @Override
  public void onWakeUp(WakeUpEvent event) {
    sendDraftCommand(WAKE_UP);
  }

  @Override
  public void onResetDraft(ResetDraftEvent event) {
    sendDraftCommand(RESET_DRAFT);
  }

  @Override
  public void onDisconnectClient(DisconnectClientEvent event) {
    sendDraftCommand(DISCONNECT_CLIENT, (long) event.getTeam());
  }

  @Override
  public void onClearCaches(ClearCachesEvent event) {
    sendDraftCommand(CLEAR_CACHES);
  }

  public int getServerClockDiff() {
    if (serverClockDiffs.size() < CLOCK_SYNC_CYCLES && !serverClockDiffs.isEmpty()) {
      return serverClockDiffs.get(0);
    }
    return serverClockDiff;
  }

  private void processClockSync(String msg) {
    String[] response = msg.substring(ServletEndpoints.CLOCK_SYNC.length()).split(ServletEndpoints.CLOCK_SYNC_SEP);
    double currentTime = currentTimeProvider.get();
    double latency = currentTime - Double.parseDouble(response[0]);
    double serverTime = Long.parseLong(response[1]) + (latency / 2);
    serverClockDiffs.add((int) (serverTime - currentTime));
    if (serverClockDiffs.size() >= CLOCK_SYNC_CYCLES) {
      Collections.sort(serverClockDiffs);
      int median = serverClockDiffs.get(CLOCK_SYNC_CYCLES / 2 + 1);
      int squareDiffs = 0;
      for (int value : serverClockDiffs) {
        squareDiffs += (value - median) * (value - median);
      }
      double stdDev = Math.sqrt(squareDiffs / (double) CLOCK_SYNC_CYCLES);
      int total = 0;
      int denom = 0;
      for (int value : serverClockDiffs) {
        if (Math.abs(value - median) <= stdDev) {
          total += value;
          denom++;
        }
      }
      serverClockDiff = total / denom;
    }
  }
}