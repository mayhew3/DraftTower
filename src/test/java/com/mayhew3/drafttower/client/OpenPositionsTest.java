package com.mayhew3.drafttower.client;

import com.google.common.collect.Lists;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.DraftStatusTestUtil;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.RosterUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.EnumSet;
import java.util.List;

/**
 * Test for {@link OpenPositions}.
 */
public class OpenPositionsTest {

  private BeanFactory beanFactory;
  private OpenPositions openPositions;
  private RosterUtil rosterUtil;
  @Captor private ArgumentCaptor<List<DraftPick>> rosterUtilArgCaptor;
  private EnumSet<Position> rosterUtilReturnValue;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    TeamsInfo teamsInfo = Mockito.mock(TeamsInfo.class);
    Mockito.when(teamsInfo.getTeam()).thenReturn(4);
    rosterUtil = Mockito.mock(RosterUtil.class);
    rosterUtilReturnValue = EnumSet.of(Position.C, Position.SB, Position.OF);
    Mockito.when(rosterUtil.getOpenPositions(rosterUtilArgCaptor.capture()))
        .thenReturn(rosterUtilReturnValue);
    openPositions = new OpenPositions(teamsInfo, rosterUtil);
  }

  @Test
  public void testOnDraftStatusChanged() throws Exception {
    openPositions.onDraftStatusChanged(DraftStatusTestUtil.createDraftStatus(
        Lists.newArrayList(
            DraftStatusTestUtil.createDraftPick(1, "a", false, beanFactory),
            DraftStatusTestUtil.createDraftPick(2, "b", false, beanFactory),
            DraftStatusTestUtil.createDraftPick(3, "c", false, beanFactory),
            DraftStatusTestUtil.createDraftPick(4, "d", false, beanFactory),
            DraftStatusTestUtil.createDraftPick(1, "e", false, beanFactory),
            DraftStatusTestUtil.createDraftPick(2, "f", false, beanFactory),
            DraftStatusTestUtil.createDraftPick(3, "g", false, beanFactory),
            DraftStatusTestUtil.createDraftPick(4, "h", false, beanFactory)), beanFactory));
    List<DraftPick> filteredPicks = rosterUtilArgCaptor.getValue();
    Assert.assertEquals(2, filteredPicks.size());
    Assert.assertEquals(4, filteredPicks.get(0).getTeam());
    Assert.assertEquals(4, filteredPicks.get(1).getTeam());
    Assert.assertEquals(rosterUtilReturnValue, openPositions.get());
  }
}