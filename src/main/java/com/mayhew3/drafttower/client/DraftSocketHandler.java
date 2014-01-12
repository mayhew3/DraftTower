package com.mayhew3.drafttower.client;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.client.DraftTowerGinModule.DraftSocketUrl;
import com.mayhew3.drafttower.client.events.*;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftCommand;
import com.mayhew3.drafttower.shared.DraftCommand.Command;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.ServletEndpoints;
import com.sksamuel.gwt.websockets.Websocket;
import com.sksamuel.gwt.websockets.WebsocketListener;

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
    WakeUpEvent.Handler {

  private static final int CLOCK_SYNC_CYCLES = 5;

  private static final int INITIAL_BACKOFF_MS = 5;
  private static final int MAX_BACKOFF_MS = 5000;

  private final BeanFactory beanFactory;
  private final Websocket socket;
  private final TeamsInfo teamsInfo;
  private final EventBus eventBus;

  private DraftStatus draftStatus;
  private long latestStatusSerialId = -1;

  private final List<Integer> serverClockDiffs = new ArrayList<>();
  private int serverClockDiff;

  private int backoff = INITIAL_BACKOFF_MS;
  private final List<String> queuedMsgs = new ArrayList<>();

  @Inject
  public DraftSocketHandler(BeanFactory beanFactory,
      @DraftSocketUrl String socketUrl,
      TeamsInfo teamsInfo,
      EventBus eventBus) {
    this.beanFactory = beanFactory;
    this.teamsInfo = teamsInfo;
    socket = new Websocket(socketUrl);
    socket.addListener(this);

    this.eventBus = eventBus;
    eventBus.addHandler(LoginEvent.TYPE, this);
    eventBus.addHandler(PlayPauseEvent.TYPE, this);
    eventBus.addHandler(PickPlayerEvent.TYPE, this);
    eventBus.addHandler(BackOutPickEvent.TYPE, this);
    eventBus.addHandler(ForcePickPlayerEvent.TYPE, this);
    eventBus.addHandler(WakeUpEvent.TYPE, this);
  }

  @Override
  public void onOpen() {
    backoff = INITIAL_BACKOFF_MS;
    eventBus.fireEvent(new SocketConnectEvent());
    sendDraftCommand(IDENTIFY);
    for (int i = 0; i < CLOCK_SYNC_CYCLES; i++) {
      Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
        @Override
        public boolean execute() {
          socket.send(ServletEndpoints.CLOCK_SYNC + Duration.currentTimeMillis());
          return false;
        }
      }, 2000 * i);
    }
    for (String queuedMsg : queuedMsgs) {
      sendMessage(queuedMsg);
    }
    queuedMsgs.clear();
  }

  @Override
  public void onMessage(String msg) {
    if (msg.startsWith(ServletEndpoints.CLOCK_SYNC)) {
      processClockSync(msg);
    } else {
      draftStatus = AutoBeanCodex.decode(beanFactory, DraftStatus.class, msg).as();
      if (latestStatusSerialId < draftStatus.getSerialId()) {
        eventBus.fireEvent(new DraftStatusChangedEvent(draftStatus));
        latestStatusSerialId = draftStatus.getSerialId();
      }
    }
  }

  @Override
  public void onClose() {
    eventBus.fireEvent(new SocketDisconnectEvent());
    Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
      @Override
      public boolean execute() {
        socket.open();
        return false;
      }
    }, backoff);
    backoff = Math.max(backoff * 2, MAX_BACKOFF_MS);
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

  public void sendMessage(String msg) {
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
    if (draftStatus.getCurrentPickDeadline() == 0) {
      sendDraftCommand(START_DRAFT);
    } else if (draftStatus.isPaused()) {
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

  public int getServerClockDiff() {
    if (serverClockDiffs.size() < CLOCK_SYNC_CYCLES && !serverClockDiffs.isEmpty()) {
      return serverClockDiffs.get(0);
    }
    return serverClockDiff;
  }

  private void processClockSync(String msg) {
    String[] response = msg.substring(ServletEndpoints.CLOCK_SYNC.length()).split(ServletEndpoints.CLOCK_SYNC_SEP);
    double currentTime = Duration.currentTimeMillis();
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