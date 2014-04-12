package com.mayhew3.drafttower.client.depthcharts;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gwt.event.shared.EventBus;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.DraftStatusTestUtil;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.RosterUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Tests for {@link DepthChartsPresenter}.
 */
public class DepthChartsPresenterTest {

  private BeanFactory beanFactory;
  private DepthChartsPresenter presenter;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    RosterUtil rosterUtil = Mockito.mock(RosterUtil.class);
    Mockito.when(rosterUtil.constructRoster(Mockito.anyListOf(DraftPick.class)))
        .then(new Answer<Multimap<Position, DraftPick>>() {
          @Override
          public Multimap<Position, DraftPick> answer(InvocationOnMock invocation) {
            List<DraftPick> picks = (List<DraftPick>) invocation.getArguments()[0];
            ArrayListMultimap<Position, DraftPick> roster = ArrayListMultimap.create();
            for (DraftPick pick : picks) {
              roster.put(Position.fromShortName(pick.getEligibilities().get(0)),
                  pick);
            }
            return roster;
          }
        });
    presenter = new DepthChartsPresenter(Mockito.mock(EventBus.class),
        rosterUtil);
  }

  @Test
  public void testNoPicks() {
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createDraftStatus(Lists.<DraftPick>newArrayList(), beanFactory)));
    for (int i = 0; i < 10; i++) {
      for (Position position : Position.REAL_POSITIONS) {
        Assert.assertEquals(0, presenter.getPicks(i, position).size());
      }
    }
  }

  @Test
  public void testPartialRound() {
    ArrayList<DraftPick> picks = Lists.newArrayList();
    for (int i = 0; i < 5; i++) {
      picks.add(DraftStatusTestUtil.createDraftPick(i, Integer.toString(i), false, "P", beanFactory));
    }
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createDraftStatus(picks, beanFactory)));
    for (int i = 0; i < 10; i++) {
      for (Position position : Position.REAL_POSITIONS) {
        if (i < 5 && position == Position.P) {
          Assert.assertEquals(1, presenter.getPicks(i, position).size());
          Assert.assertEquals(Integer.toString(i),
              presenter.getPicks(i, position).iterator().next().getPlayerName());
        } else {
          Assert.assertEquals(0, presenter.getPicks(i, position).size());
        }
      }
    }
  }

  @Test
  public void testFullRound() {
    ArrayList<DraftPick> picks = Lists.newArrayList();
    for (int i = 0; i < 10; i++) {
      picks.add(DraftStatusTestUtil.createDraftPick(i, Integer.toString(i), false, "P", beanFactory));
    }
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createDraftStatus(picks, beanFactory)));
    for (int i = 0; i < 10; i++) {
      for (Position position : Position.REAL_POSITIONS) {
        if (position == Position.P) {
          Assert.assertEquals(1, presenter.getPicks(i, position).size());
          Assert.assertEquals(Integer.toString(i),
              presenter.getPicks(i, position).iterator().next().getPlayerName());
        } else {
          Assert.assertEquals(0, presenter.getPicks(i, position).size());
        }
      }
    }
  }

  @Test
  public void testMultipleRound() {
    ArrayList<DraftPick> picks = Lists.newArrayList();
    for (Position position : new Position[]{Position.P, Position.FB, Position.SB}) {
      for (int i = 0; i < 10; i++) {
        picks.add(DraftStatusTestUtil.createDraftPick(
            i, Integer.toString(i), false, position.getShortName(), beanFactory));
      }
    }
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createDraftStatus(picks, beanFactory)));
    for (int i = 0; i < 10; i++) {
      for (Position position : Position.REAL_POSITIONS) {
        if (EnumSet.of(Position.P, Position.FB, Position.SB).contains(position)) {
          Assert.assertEquals(1, presenter.getPicks(i, position).size());
          Assert.assertEquals(Integer.toString(i),
              presenter.getPicks(i, position).iterator().next().getPlayerName());
        } else {
          Assert.assertEquals(0, presenter.getPicks(i, position).size());
        }
      }
    }
  }
}