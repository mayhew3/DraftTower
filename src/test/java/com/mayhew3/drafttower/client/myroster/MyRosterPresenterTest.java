package com.mayhew3.drafttower.client.myroster;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventBus;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.DraftStatusTestUtil;
import com.mayhew3.drafttower.RosterTestUtils;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.client.myroster.MyRosterPresenter.PickAndPosition;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.Position;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@link MyRosterPresenter}.
 */
public class MyRosterPresenterTest {

  private BeanFactory beanFactory;
  private MyRosterPresenter presenter;
  private List<DraftPick> picks = new ArrayList<>();

  @Before
  public void setUp() {
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    TeamsInfo teamsInfo = Mockito.mock(TeamsInfo.class);
    Mockito.when(teamsInfo.getTeam()).thenReturn(4);
    presenter = new MyRosterPresenter(
        teamsInfo,
        Mockito.mock(EventBus.class),
        RosterTestUtils.createSimpleFakeRosterUtil());
  }

  @Test
  public void testNoPicks() {
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createDraftStatus(picks, beanFactory)));
    List<PickAndPosition> list = presenter.getList();
    Assert.assertEquals(getEmptyRoster(), list);
  }

  @Test
  public void testNoPicksByMe() {
    picks.add(DraftStatusTestUtil.createDraftPick(1, "", false, beanFactory));
    picks.add(DraftStatusTestUtil.createDraftPick(2, "", false, beanFactory));
    picks.add(DraftStatusTestUtil.createDraftPick(3, "", false, beanFactory));
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createDraftStatus(picks, beanFactory)));
    List<PickAndPosition> list = presenter.getList();
    Assert.assertEquals(getEmptyRoster(), list);
  }

  @Test
  public void testOnePickByMe() {
    picks.add(DraftStatusTestUtil.createDraftPick(1, "", false, beanFactory));
    picks.add(DraftStatusTestUtil.createDraftPick(2, "", false, beanFactory));
    picks.add(DraftStatusTestUtil.createDraftPick(3, "", false, beanFactory));
    DraftPick myPick = DraftStatusTestUtil.createDraftPick(4, "", false, "SS", beanFactory);
    picks.add(myPick);
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createDraftStatus(picks, beanFactory)));
    List<PickAndPosition> list = presenter.getList();
    ArrayList<PickAndPosition> expected = getEmptyRoster();
    expected.set(4, new PickAndPosition(myPick, Position.SS));
    Assert.assertEquals(expected, list);
  }

  @Test
  public void testMultiplePicksByMe() {
    String positions[] = new String[] {"1B", "3B", "P", "P"};
    DraftPick myPicks[] = new DraftPick[4];
    for (int i = 0; i < 40; i++) {
      int team = (i % 10) + 1;
      if (team == 4) {
        DraftPick draftPick = DraftStatusTestUtil.createDraftPick(team, "", false, positions[i / 10], beanFactory);
        myPicks[i / 10] = draftPick;
        picks.add(draftPick);
      } else {
        picks.add(DraftStatusTestUtil.createDraftPick(team, "", false, beanFactory));
      }
    }
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createDraftStatus(picks, beanFactory)));
    List<PickAndPosition> list = presenter.getList();
    ArrayList<PickAndPosition> expected = getEmptyRoster();
    expected.set(1, new PickAndPosition(myPicks[0], Position.FB));
    expected.set(3, new PickAndPosition(myPicks[1], Position.TB));
    expected.set(9, new PickAndPosition(myPicks[2], Position.P));
    expected.set(10, new PickAndPosition(myPicks[3], Position.P));
    Assert.assertEquals(expected, list);
  }

  private ArrayList<PickAndPosition> getEmptyRoster() {
    return Lists.newArrayList(
        new PickAndPosition(null, Position.C),
        new PickAndPosition(null, Position.FB),
        new PickAndPosition(null, Position.SB),
        new PickAndPosition(null, Position.TB),
        new PickAndPosition(null, Position.SS),
        new PickAndPosition(null, Position.OF),
        new PickAndPosition(null, Position.OF),
        new PickAndPosition(null, Position.OF),
        new PickAndPosition(null, Position.DH),
        new PickAndPosition(null, Position.P),
        new PickAndPosition(null, Position.P),
        new PickAndPosition(null, Position.P),
        new PickAndPosition(null, Position.P),
        new PickAndPosition(null, Position.P),
        new PickAndPosition(null, Position.P),
        new PickAndPosition(null, Position.P),
        new PickAndPosition(null, Position.RS),
        new PickAndPosition(null, Position.RS),
        new PickAndPosition(null, Position.RS),
        new PickAndPosition(null, Position.RS),
        new PickAndPosition(null, Position.RS),
        new PickAndPosition(null, Position.RS));
  }
}