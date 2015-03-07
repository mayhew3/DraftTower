package com.mayhew3.drafttower.client.audio;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventBus;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.DraftStatusTestUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;

/**
 * Unit tests for {@link AudioPresenter}.
 */
public class AudioPresenterTest {

  private BeanFactory beanFactory;
  private AudioPresenter audioPresenter;
  private TeamsInfo teamsInfo;
  private AudioView view;
  private List<DraftPick> picks;
  private boolean onDeck;
  private boolean onClock;

  @Before
  public void setUp() {
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    teamsInfo = Mockito.mock(TeamsInfo.class);
    Mockito.when(teamsInfo.getShortTeamName(Mockito.anyInt()))
        .thenReturn("team name");
    Mockito.when(teamsInfo.getTeam()).thenReturn(2);
    Mockito.when(teamsInfo.getShortTeamName(2))
        .thenReturn("my team");
    Mockito.when(teamsInfo.isOnDeck(Mockito.any(DraftStatus.class)))
        .then(new Answer<Boolean>() {
          @Override
          public Boolean answer(InvocationOnMock invocation) throws Throwable {
            return onDeck;
          }
        });
    Mockito.when(teamsInfo.isMyPick(Mockito.any(DraftStatus.class)))
        .then(new Answer<Boolean>() {
          @Override
          public Boolean answer(InvocationOnMock invocation) throws Throwable {
            return onClock;
          }
        });
    
    audioPresenter = new AudioPresenter(teamsInfo,
        Mockito.mock(EventBus.class));
    view = Mockito.mock(AudioView.class);
    audioPresenter.setAudioView(view);
    audioPresenter.setSpeechControlView(Mockito.mock(SpeechControlView.class));
    picks = Lists.newArrayList(
        DraftStatusTestUtil.createDraftPick(3, "previous pick", false, beanFactory),
        DraftStatusTestUtil.createDraftPick(4, "last pick", false, beanFactory));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
  }

  @Test
  public void testDraftStatusChangeNoNewPicks() {
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNoNewPicksOnDeck() {
    onDeck = true;
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNoNewPicksOnClock() {
    onClock = true;
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNewPick() {
    Mockito.when(teamsInfo.getShortTeamName(5)).thenReturn("team 5 name");
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", false, beanFactory));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).play("team 5 name selects player name. ");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNewPickVolumeOff() {
    audioPresenter.toggleLevel();
    audioPresenter.toggleLevel();
    Mockito.when(teamsInfo.getShortTeamName(5)).thenReturn("team 5 name");
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", false, beanFactory));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNewPickVolumeLow() {
    audioPresenter.toggleLevel();
    Mockito.when(teamsInfo.getShortTeamName(5)).thenReturn("team 5 name");
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", false, beanFactory));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNewPickOnDeckVolumeOff() {
    audioPresenter.toggleLevel();
    audioPresenter.toggleLevel();
    onDeck = true;
    Mockito.when(teamsInfo.getShortTeamName(5)).thenReturn("team 5 name");
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", false, beanFactory));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNewPickOnDeckVolumeLow() {
    audioPresenter.toggleLevel();
    onDeck = true;
    Mockito.when(teamsInfo.getShortTeamName(5)).thenReturn("team 5 name");
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", false, beanFactory));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).play("my team. you're on deck");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNewPickOnDeck() {
    onDeck = true;
    Mockito.when(teamsInfo.getShortTeamName(5)).thenReturn("team 5 name");
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", false, beanFactory));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).play("team 5 name selects player name. my team. you're on deck");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNewPickOnClockVolumeOff() {
    audioPresenter.toggleLevel();
    audioPresenter.toggleLevel();
    onClock = true;
    Mockito.when(teamsInfo.getShortTeamName(5)).thenReturn("team 5 name");
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", false, beanFactory));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNewPickOnClockVolumeLow() {
    audioPresenter.toggleLevel();
    onClock = true;
    Mockito.when(teamsInfo.getShortTeamName(5)).thenReturn("team 5 name");
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", false, beanFactory));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).play("my team. you're on the clock");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNewPickOnClock() {
    onClock = true;
    Mockito.when(teamsInfo.getShortTeamName(5)).thenReturn("team 5 name");
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", false, beanFactory));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).play("team 5 name selects player name. my team. you're on the clock");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeBackOutPick() {
    picks.remove(picks.size() - 1);
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testDraftStatusChangeBackOutOnDeck() {
    onDeck = true;
    picks.remove(picks.size() - 1);
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).play("my team. you're on deck");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeBackOutOnClock() {
    onClock = true;
    picks.remove(picks.size() - 1);
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).play("my team. you're on the clock");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeTwoNewPicks() {
    Mockito.when(teamsInfo.getShortTeamName(6)).thenReturn("team 6 name");
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", false, beanFactory));
    picks.add(DraftStatusTestUtil.createDraftPick(6, "second player name", false, beanFactory));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).play("team 6 name selects second player name. ");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeTwoNewPicksOnDeck() {
    onDeck = true;
    Mockito.when(teamsInfo.getShortTeamName(6)).thenReturn("team 6 name");
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", false, beanFactory));
    picks.add(DraftStatusTestUtil.createDraftPick(6, "second player name", false, beanFactory));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).play("team 6 name selects second player name. my team. you're on deck");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeTwoNewPicksOnClock() {
    onClock = true;
    Mockito.when(teamsInfo.getShortTeamName(6)).thenReturn("team 6 name");
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", false, beanFactory));
    picks.add(DraftStatusTestUtil.createDraftPick(6, "second player name", false, beanFactory));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).play("team 6 name selects second player name. my team. you're on the clock");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNewPickThenKeepers() {
    Mockito.when(teamsInfo.getShortTeamName(5)).thenReturn("team 5 name");
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", false, beanFactory));
    picks.add(DraftStatusTestUtil.createDraftPick(6, "second player name", true, beanFactory));
    picks.add(DraftStatusTestUtil.createDraftPick(7, "third player name", true, beanFactory));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).play("team 5 name selects player name. ");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNewPickThenKeepersOnDeck() {
    onDeck = true;
    Mockito.when(teamsInfo.getShortTeamName(5)).thenReturn("team 5 name");
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", false, beanFactory));
    picks.add(DraftStatusTestUtil.createDraftPick(6, "second player name", true, beanFactory));
    picks.add(DraftStatusTestUtil.createDraftPick(7, "third player name", true, beanFactory));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).play("team 5 name selects player name. my team. you're on deck");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNewPickThenKeepersOnClock() {
    onClock = true;
    Mockito.when(teamsInfo.getShortTeamName(5)).thenReturn("team 5 name");
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", false, beanFactory));
    picks.add(DraftStatusTestUtil.createDraftPick(6, "second player name", true, beanFactory));
    picks.add(DraftStatusTestUtil.createDraftPick(7, "third player name", true, beanFactory));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).play("team 5 name selects player name. my team. you're on the clock");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeKeepersOnly() {
    picks.clear();
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.reset(view);
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", true, beanFactory));
    picks.add(DraftStatusTestUtil.createDraftPick(6, "second player name", true, beanFactory));
    picks.add(DraftStatusTestUtil.createDraftPick(7, "third player name", true, beanFactory));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testDraftStatusChangeKeepersOnlyOnDeck() {
    picks.clear();
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.reset(view);
    onDeck = true;
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", true, beanFactory));
    picks.add(DraftStatusTestUtil.createDraftPick(6, "second player name", true, beanFactory));
    picks.add(DraftStatusTestUtil.createDraftPick(7, "third player name", true, beanFactory));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).play("my team. you're on deck");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeKeepersOnlyOnClock() {
    picks.clear();
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.reset(view);
    onClock = true;
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", true, beanFactory));
    picks.add(DraftStatusTestUtil.createDraftPick(6, "second player name", true, beanFactory));
    picks.add(DraftStatusTestUtil.createDraftPick(7, "third player name", true, beanFactory));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(picks, beanFactory)));
    Mockito.verify(view).play("my team. you're on the clock");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeZeroDeadline() {
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", false, beanFactory));
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(picks, beanFactory);
    draftStatus.setCurrentPickDeadline(0);
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testDraftStatusChangeZeroDeadlineOnDeck() {
    onDeck = true;
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", false, beanFactory));
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(picks, beanFactory);
    draftStatus.setCurrentPickDeadline(0);
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testDraftStatusChangeZeroDeadlineOnClock() {
    onClock = true;
    picks.add(DraftStatusTestUtil.createDraftPick(5, "player name", false, beanFactory));
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(picks, beanFactory);
    draftStatus.setCurrentPickDeadline(0);
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testDraftStatusChangeItsOver() {
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(picks, beanFactory);
    draftStatus.setOver(true);
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verify(view).playItsOver();
    Mockito.verifyNoMoreInteractions(view);
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verifyNoMoreInteractions(view);
  }
}