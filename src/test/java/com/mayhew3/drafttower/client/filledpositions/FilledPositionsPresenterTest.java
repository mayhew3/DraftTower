package com.mayhew3.drafttower.client.filledpositions;

import com.google.common.collect.ImmutableMap;
import com.google.gwt.event.shared.EventBus;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.DraftStatusTestUtil;
import com.mayhew3.drafttower.RosterTestUtils;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.Position;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Tests for {@link FilledPositionsPresenter}.
 */
public class FilledPositionsPresenterTest {

  private BeanFactory beanFactory;
  private FilledPositionsPresenter presenter;
  private FilledPositionsView view;
  private ArrayList<DraftPick> picks;

  @Captor private ArgumentCaptor<Map<Position, Integer>> argumentCaptor;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    presenter = new FilledPositionsPresenter(10,
        Mockito.mock(EventBus.class),
        RosterTestUtils.createSimpleFakeRosterUtil());
    view = Mockito.mock(FilledPositionsView.class);
    presenter.setView(view);
    picks = new ArrayList<>();
  }

  @Test
  public void testNoPicks() {
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createDraftStatus(picks, beanFactory)));
    Mockito.verify(view).setCounts(argumentCaptor.capture());
    Map<Position, Integer> counts = argumentCaptor.getValue();
    for (Entry<Position, Integer> countsEntry : counts.entrySet()) {
      Assert.assertEquals(0, countsEntry.getValue().intValue());
    }
  }

  @Test
  public void testSomePicks() {
    String pickPositions[] = new String[] {
        "P", "P", "1B", "3B", "C", "OF", "OF", "OF", "SS", "P",
        "1B", "P", "1B", "2B", "1B", "OF", "OF", "C", "P", "P"
    };
    Map<Position, Integer> expected = new ImmutableMap.Builder<Position, Integer>()
        .put(Position.P, 6)
        .put(Position.FB, 4)
        .put(Position.SB, 1)
        .put(Position.TB, 1)
        .put(Position.SS, 1)
        .put(Position.OF, 5)
        .put(Position.C, 2)
        .build();
    for (int i = 0; i < pickPositions.length; i++) {
      picks.add(DraftStatusTestUtil.createDraftPick(
          (i % 10) + 1, "", false, pickPositions[i], beanFactory));
    }
    presenter.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createDraftStatus(picks, beanFactory)));
    Mockito.verify(view).setCounts(argumentCaptor.capture());
    Map<Position, Integer> counts = argumentCaptor.getValue();
    for (Entry<Position, Integer> countsEntry : counts.entrySet()) {
      int expectedValue = expected.containsKey(countsEntry.getKey())
          ? expected.get(countsEntry.getKey()) : 0;
      Assert.assertEquals(expectedValue, countsEntry.getValue().intValue());
    }
  }

  @Test
  public void testGetDenominator() {
    Assert.assertEquals(10, presenter.getDenominator(Position.FB));
    Assert.assertEquals(10, presenter.getDenominator(Position.C));
    Assert.assertEquals(30, presenter.getDenominator(Position.OF));
    Assert.assertEquals(70, presenter.getDenominator(Position.P));
  }
}