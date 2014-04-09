package com.mayhew3.drafttower.client.audio;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventBus;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.DraftStatus;
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
    picks = Lists.newArrayList(
        createDraftPick(3, "previous pick", false),
        createDraftPick(4, "last pick", false));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
  }

  @Test
  public void testDraftStatusChangeNoNewPicks() {
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNoNewPicksOnDeck() {
    onDeck = true;
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNoNewPicksOnClock() {
    onClock = true;
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNewPick() {
    Mockito.when(teamsInfo.getShortTeamName(5)).thenReturn("team 5 name");
    picks.add(createDraftPick(5, "player name", false));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.verify(view).play("team 5 name selects player name. ");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNewPickOnDeck() {
    onDeck = true;
    Mockito.when(teamsInfo.getShortTeamName(5)).thenReturn("team 5 name");
    picks.add(createDraftPick(5, "player name", false));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.verify(view).play("team 5 name selects player name. my team. you're on deck");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNewPickOnClock() {
    onClock = true;
    Mockito.when(teamsInfo.getShortTeamName(5)).thenReturn("team 5 name");
    picks.add(createDraftPick(5, "player name", false));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.verify(view).play("team 5 name selects player name. my team. you're on the clock");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeBackOutPick() {
    picks.remove(picks.size() - 1);
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testDraftStatusChangeBackOutOnDeck() {
    onDeck = true;
    picks.remove(picks.size() - 1);
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.verify(view).play("my team. you're on deck");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeBackOutOnClock() {
    onClock = true;
    picks.remove(picks.size() - 1);
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.verify(view).play("my team. you're on the clock");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeTwoNewPicks() {
    Mockito.when(teamsInfo.getShortTeamName(6)).thenReturn("team 6 name");
    picks.add(createDraftPick(5, "player name", false));
    picks.add(createDraftPick(6, "second player name", false));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.verify(view).play("team 6 name selects second player name. ");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeTwoNewPicksOnDeck() {
    onDeck = true;
    Mockito.when(teamsInfo.getShortTeamName(6)).thenReturn("team 6 name");
    picks.add(createDraftPick(5, "player name", false));
    picks.add(createDraftPick(6, "second player name", false));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.verify(view).play("team 6 name selects second player name. my team. you're on deck");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeTwoNewPicksOnClock() {
    onClock = true;
    Mockito.when(teamsInfo.getShortTeamName(6)).thenReturn("team 6 name");
    picks.add(createDraftPick(5, "player name", false));
    picks.add(createDraftPick(6, "second player name", false));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.verify(view).play("team 6 name selects second player name. my team. you're on the clock");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNewPickThenKeepers() {
    Mockito.when(teamsInfo.getShortTeamName(5)).thenReturn("team 5 name");
    picks.add(createDraftPick(5, "player name", false));
    picks.add(createDraftPick(6, "second player name", true));
    picks.add(createDraftPick(7, "third player name", true));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.verify(view).play("team 5 name selects player name. ");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNewPickThenKeepersOnDeck() {
    onDeck = true;
    Mockito.when(teamsInfo.getShortTeamName(5)).thenReturn("team 5 name");
    picks.add(createDraftPick(5, "player name", false));
    picks.add(createDraftPick(6, "second player name", true));
    picks.add(createDraftPick(7, "third player name", true));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.verify(view).play("team 5 name selects player name. my team. you're on deck");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeNewPickThenKeepersOnClock() {
    onClock = true;
    Mockito.when(teamsInfo.getShortTeamName(5)).thenReturn("team 5 name");
    picks.add(createDraftPick(5, "player name", false));
    picks.add(createDraftPick(6, "second player name", true));
    picks.add(createDraftPick(7, "third player name", true));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.verify(view).play("team 5 name selects player name. my team. you're on the clock");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeKeepersOnly() {
    picks.clear();
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.reset(view);
    picks.add(createDraftPick(5, "player name", true));
    picks.add(createDraftPick(6, "second player name", true));
    picks.add(createDraftPick(7, "third player name", true));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testDraftStatusChangeKeepersOnlyOnDeck() {
    picks.clear();
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.reset(view);
    onDeck = true;
    picks.add(createDraftPick(5, "player name", true));
    picks.add(createDraftPick(6, "second player name", true));
    picks.add(createDraftPick(7, "third player name", true));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.verify(view).play("my team. you're on deck");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeKeepersOnlyOnClock() {
    picks.clear();
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.reset(view);
    onClock = true;
    picks.add(createDraftPick(5, "player name", true));
    picks.add(createDraftPick(6, "second player name", true));
    picks.add(createDraftPick(7, "third player name", true));
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(createDraftStatus(picks)));
    Mockito.verify(view).play("my team. you're on the clock");
    Mockito.verifyNoMoreInteractions(view);
  }

  @Test
  public void testDraftStatusChangeZeroDeadline() {
    picks.add(createDraftPick(5, "player name", false));
    DraftStatus draftStatus = createDraftStatus(picks);
    draftStatus.setCurrentPickDeadline(0);
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(draftStatus));
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testDraftStatusChangeZeroDeadlineOnDeck() {
    onDeck = true;
    picks.add(createDraftPick(5, "player name", false));
    DraftStatus draftStatus = createDraftStatus(picks);
    draftStatus.setCurrentPickDeadline(0);
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(draftStatus));
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testDraftStatusChangeZeroDeadlineOnClock() {
    onClock = true;
    picks.add(createDraftPick(5, "player name", false));
    DraftStatus draftStatus = createDraftStatus(picks);
    draftStatus.setCurrentPickDeadline(0);
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(draftStatus));
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testDraftStatusChangeItsOver() {
    DraftStatus draftStatus = createDraftStatus(picks);
    draftStatus.setOver(true);
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(draftStatus));
    Mockito.verify(view).playItsOver();
    Mockito.verifyNoMoreInteractions(view);
    audioPresenter.onDraftStatusChanged(new DraftStatusChangedEvent(draftStatus));
    Mockito.verifyNoMoreInteractions(view);
  }

  private DraftStatus createDraftStatus(List<DraftPick> picks) {
    DraftStatus draftStatus = beanFactory.createDraftStatus().as();
    draftStatus.setPicks(Lists.newArrayList(picks));
    draftStatus.setCurrentPickDeadline(1);
    draftStatus.setCurrentTeam(picks.isEmpty()
        ? 1
        : picks.get(picks.size() - 1).getTeam() + 1);
    return draftStatus;
  }
  
  private DraftPick createDraftPick(int team, String name, boolean isKeeper) {
    DraftPick draftPick = beanFactory.createDraftPick().as();
    draftPick.setTeam(team);
    draftPick.setEligibilities(ImmutableList.of("P"));
    draftPick.setKeeper(isKeeper);
    draftPick.setPlayerName(name);
    return draftPick;
  }
}