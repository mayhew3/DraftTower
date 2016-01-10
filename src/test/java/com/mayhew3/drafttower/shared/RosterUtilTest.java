package com.mayhew3.drafttower.shared;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.mayhew3.drafttower.shared.Position.*;

/**
 * Tests for {@link RosterUtil}.
 */
@SuppressWarnings("unchecked")
public class RosterUtilTest {

  private static final BeanFactory beanFactory = AutoBeanFactorySource.create(BeanFactory.class);

  private static DraftPick pick(long playerId, Position... eligibilities) {
    DraftPick pick = beanFactory.createDraftPick().as();
    pick.setPlayerId(playerId);
    pick.setEligibilities(Lists.transform(
        Lists.newArrayList(eligibilities), new Function<Position, String>() {
      @Override
      public String apply(Position input) {
        return input.getShortName();
      }
    }));
    return pick;
  }

  private static DraftPick pick(long playerId, int team, Position... eligibilities) {
    DraftPick pick = pick(playerId, eligibilities);
    pick.setTeam(team);
    return pick;
  }

  private void assertRoster(List<Entry<Position, Long>> expectedIds, List<DraftPick> picks) {
    Multimap<Position, DraftPick> roster = new RosterUtil().constructRoster(picks);
    Assert.assertEquals(roster.toString(), expectedIds.size(), roster.size());
    for (Entry<Position, Long> expectedId : expectedIds) {
      Assert.assertTrue(roster.toString(),
          Iterables.contains(Iterables.transform(roster.get(expectedId.getKey()),
              new Function<DraftPick, Long>() {
                @Override
                public Long apply(DraftPick input) {
                  return input.getPlayerId();
                }
              }), expectedId.getValue()));
    }
  }

  @Test
  public void testConstructRosterBasic() throws Exception {
    // Basic: one per position.
    assertRoster(
        Lists.newArrayList(
            Maps.immutableEntry(FB, 1l),
            Maps.immutableEntry(SB, 2l),
            Maps.immutableEntry(TB, 3l),
            Maps.immutableEntry(SS, 4l),
            Maps.immutableEntry(C, 5l),
            Maps.immutableEntry(OF, 6l),
            Maps.immutableEntry(OF, 7l),
            Maps.immutableEntry(OF, 8l),
            Maps.immutableEntry(DH, 9l),
            Maps.immutableEntry(P, 10l),
            Maps.immutableEntry(P, 11l),
            Maps.immutableEntry(P, 12l),
            Maps.immutableEntry(P, 13l),
            Maps.immutableEntry(P, 14l),
            Maps.immutableEntry(P, 15l),
            Maps.immutableEntry(P, 16l)),
        Lists.newArrayList(
            pick(1, FB),
            pick(2, SB),
            pick(3, TB),
            pick(4, SS),
            pick(5, C),
            pick(6, OF),
            pick(7, OF),
            pick(8, OF),
            pick(9, DH),
            pick(10, P),
            pick(11, P),
            pick(12, P),
            pick(13, P),
            pick(14, P),
            pick(15, P),
            pick(16, P)));
  }

  @Test
  public void testConstructRosterDH() throws Exception {
    assertRoster(
        Lists.newArrayList(
            Maps.immutableEntry(FB, 1l),
            Maps.immutableEntry(DH, 2l)),
        Lists.newArrayList(
            pick(1, FB),
            pick(2, FB)));
  }

  @Test
  public void testConstructRosterReserves() throws Exception {
    assertRoster(
        Lists.newArrayList(
            Maps.immutableEntry(FB, 1l),
            Maps.immutableEntry(DH, 2l),
            Maps.immutableEntry(RS, 3l),
            Maps.immutableEntry(P, 4l),
            Maps.immutableEntry(P, 5l),
            Maps.immutableEntry(P, 6l),
            Maps.immutableEntry(P, 7l),
            Maps.immutableEntry(P, 8l),
            Maps.immutableEntry(P, 9l),
            Maps.immutableEntry(P, 10l),
            Maps.immutableEntry(RS, 11l)),
        Lists.newArrayList(
            pick(1, FB),
            pick(2, FB),
            pick(3, FB),
            pick(4, P),
            pick(5, P),
            pick(6, P),
            pick(7, P),
            pick(8, P),
            pick(9, P),
            pick(10, P),
            pick(11, P)));
  }

  @Test
  public void testConstructRosterMultipleEligibility() throws Exception {
    assertRoster(
        Lists.newArrayList(
            Maps.immutableEntry(TB, 1l),
            Maps.immutableEntry(FB, 2l),
            Maps.immutableEntry(SB, 3l)),
        Lists.newArrayList(
            pick(1, FB, SB, TB),
            pick(2, FB),
            pick(3, SB)));
  }

  @Test
  public void testConstructRosterMultipleEligibilityPriority() throws Exception {
    assertRoster(
        Lists.newArrayList(
            Maps.immutableEntry(C, 1l)),
        Lists.newArrayList(
            pick(1, C, FB)));
    assertRoster(
        Lists.newArrayList(
            Maps.immutableEntry(C, 1l)),
        Lists.newArrayList(
            pick(1, FB, C)));
  }

  @Test
  public void testConstructRosterBigPapi() throws Exception {
    assertRoster(
        Lists.newArrayList(
            Maps.immutableEntry(DH, 1l)),
        Lists.newArrayList(
            pick(1, DH)));
  }

  @Test
  public void testAllPositionsOpen() throws Exception {
    Assert.assertEquals(EnumSet.of(C, FB, SB, TB, SS, OF, DH, P),
        new RosterUtil().getOpenPositions(ImmutableList.<DraftPick>of()));
  }

  @Test
  public void testNoPositionsOpen() throws Exception {
    Assert.assertEquals(EnumSet.noneOf(Position.class),
        new RosterUtil().getOpenPositions(Lists.newArrayList(
            pick(1, C),
            pick(2, FB),
            pick(3, SB),
            pick(4, SS),
            pick(5, TB),
            pick(6, OF),
            pick(7, OF),
            pick(8, OF),
            pick(9, OF),
            pick(10, P),
            pick(11, P),
            pick(12, P),
            pick(13, P),
            pick(14, P),
            pick(15, P),
            pick(16, P))));
  }

  @Test
  public void testNoPositionsOpenMultiPositionPlayers() throws Exception {
    Assert.assertEquals(EnumSet.noneOf(Position.class),
        new RosterUtil().getOpenPositions(Lists.newArrayList(
            pick(1, C, FB),
            pick(2, FB),
            pick(3, SB),
            pick(4, SS),
            pick(5, TB, FB, OF),
            pick(6, OF),
            pick(7, OF),
            pick(8, OF, FB),
            pick(9, FB),
            pick(10, P),
            pick(11, P),
            pick(12, P),
            pick(13, P),
            pick(14, P),
            pick(15, P),
            pick(16, P))));
  }

  @Test
  public void testGetOpenPositionsOneOpenOneReserve() throws Exception {
    Assert.assertEquals(EnumSet.of(SB),
        new RosterUtil().getOpenPositions(Lists.newArrayList(
            pick(1, C),
            pick(2, FB),
            pick(3, TB),
            pick(4, SS),
            pick(5, OF),
            pick(6, OF),
            pick(7, OF),
            pick(8, OF, FB),
            pick(9, TB, OF),
            pick(10, P),
            pick(11, P),
            pick(12, P),
            pick(13, P),
            pick(14, P),
            pick(15, P),
            pick(16, P))));
  }

  @Test
  public void testGetOpenPositionsSingleEligibility() throws Exception {
    Assert.assertEquals(EnumSet.of(FB, SB, SS, OF, DH, P),
        new RosterUtil().getOpenPositions(Lists.newArrayList(
            pick(1, C),
            pick(2, TB))));
  }

  @Test
  public void testGetOpenPositionsMultiEligibility() throws Exception {
    Assert.assertEquals(EnumSet.of(FB, SB, TB, SS, OF, DH, P),
        new RosterUtil().getOpenPositions(Lists.newArrayList(
            pick(1, C),
            pick(2, SB, SS))));
    Assert.assertEquals(EnumSet.of(C, FB, TB, OF, DH, P),
        new RosterUtil().getOpenPositions(Lists.newArrayList(
            pick(1, SB),
            pick(2, SB, SS))));
    Assert.assertEquals(EnumSet.of(C, FB, TB, OF, DH, P),
        new RosterUtil().getOpenPositions(Lists.newArrayList(
            pick(1, SB, SS),
            pick(2, SB))));
  }

  @Test
  public void testGetOpenPositionsMultiSlotPosition() throws Exception {
    Assert.assertEquals(EnumSet.of(C, FB, SB, TB, SS, OF, DH, P),
        new RosterUtil().getOpenPositions(Lists.newArrayList(
            pick(1, P),
            pick(2, P),
            pick(3, P),
            pick(4, P),
            pick(5, P),
            pick(6, P))));
    Assert.assertEquals(EnumSet.of(C, FB, SB, TB, SS, OF, DH),
        new RosterUtil().getOpenPositions(Lists.newArrayList(
            pick(1, P),
            pick(2, P),
            pick(3, P),
            pick(4, P),
            pick(5, P),
            pick(6, P),
            pick(7, P))));
  }

  @Test
  public void testGetOpenPositionsDHNotOpen() throws Exception {
    Assert.assertFalse(new RosterUtil().getOpenPositions(Lists.newArrayList(
        pick(1, FB),
        pick(2, FB)))
        .contains(DH));
  }

  @Test
  public void testAllPositionsOpenOptimal() throws Exception {
    Assert.assertEquals(EnumSet.of(C, FB, SB, TB, SS, OF, DH, P),
        new RosterUtil().getOptimalOpenPositions(ImmutableList.<DraftPick>of()));
  }

  @Test
  public void testNoPositionsOpenOptimal() throws Exception {
    Assert.assertEquals(EnumSet.noneOf(Position.class),
        new RosterUtil().getOptimalOpenPositions(Lists.newArrayList(
            pick(1, C),
            pick(2, FB),
            pick(3, SB),
            pick(4, SS),
            pick(5, TB),
            pick(6, OF),
            pick(7, OF),
            pick(8, OF),
            pick(9, OF),
            pick(10, P),
            pick(11, P),
            pick(12, P),
            pick(13, P),
            pick(14, P),
            pick(15, P),
            pick(16, P))));
  }

  @Test
  public void testNoPositionsOpenMultiPositionPlayersOptimal() throws Exception {
    Assert.assertEquals(EnumSet.noneOf(Position.class),
        new RosterUtil().getOptimalOpenPositions(Lists.newArrayList(
            pick(1, C, FB),
            pick(2, FB),
            pick(3, SB),
            pick(4, SS),
            pick(5, TB, FB, OF),
            pick(6, OF),
            pick(7, OF),
            pick(8, OF, FB),
            pick(9, FB),
            pick(10, P),
            pick(11, P),
            pick(12, P),
            pick(13, P),
            pick(14, P),
            pick(15, P),
            pick(16, P))));
  }

  @Test
  public void testGetOpenPositionsOneOpenOneReserveOptimal() throws Exception {
    Assert.assertEquals(EnumSet.of(SB),
        new RosterUtil().getOptimalOpenPositions(Lists.newArrayList(
            pick(1, C),
            pick(2, FB),
            pick(3, TB),
            pick(4, SS),
            pick(5, OF),
            pick(6, OF),
            pick(7, OF),
            pick(8, OF, FB),
            pick(9, TB, OF),
            pick(10, P),
            pick(11, P),
            pick(12, P),
            pick(13, P),
            pick(14, P),
            pick(15, P),
            pick(16, P))));
  }

  @Test
  public void testGetOpenPositionsSingleEligibilityOptimal() throws Exception {
    Assert.assertEquals(EnumSet.of(FB, SB, SS, OF, DH, P),
        new RosterUtil().getOptimalOpenPositions(Lists.newArrayList(
            pick(1, C),
            pick(2, TB))));
  }

  @Test
  public void testGetOpenPositionsMultiEligibilityOptimal() throws Exception {
    Assert.assertEquals(EnumSet.of(FB, SB, TB, OF, DH, P),
        new RosterUtil().getOptimalOpenPositions(Lists.newArrayList(
            pick(1, C),
            pick(2, SB, SS))));
    Assert.assertEquals(EnumSet.of(C, FB, TB, OF, DH, P),
        new RosterUtil().getOptimalOpenPositions(Lists.newArrayList(
            pick(1, SB),
            pick(2, SB, SS))));
    Assert.assertEquals(EnumSet.of(C, FB, TB, OF, DH, P),
        new RosterUtil().getOptimalOpenPositions(Lists.newArrayList(
            pick(1, SB, SS),
            pick(2, SB))));
  }

  @Test
  public void testGetOpenPositionsMultiSlotPositionOptimal() throws Exception {
    Assert.assertEquals(EnumSet.of(C, FB, SB, TB, SS, OF, DH, P),
        new RosterUtil().getOptimalOpenPositions(Lists.newArrayList(
            pick(1, P),
            pick(2, P),
            pick(3, P),
            pick(4, P),
            pick(5, P),
            pick(6, P))));
    Assert.assertEquals(EnumSet.of(C, FB, SB, TB, SS, OF, DH),
        new RosterUtil().getOptimalOpenPositions(Lists.newArrayList(
            pick(1, P),
            pick(2, P),
            pick(3, P),
            pick(4, P),
            pick(5, P),
            pick(6, P),
            pick(7, P))));
  }

  @Test
  public void testGetOpenPositionsDHNotOpenOptimal() throws Exception {
    Assert.assertFalse(new RosterUtil().getOptimalOpenPositions(Lists.newArrayList(
        pick(1, FB),
        pick(2, FB)))
        .contains(DH));
  }

  @Test
  public void testGetOpenPositionsOFBug() throws Exception {
    Assert.assertFalse(new RosterUtil().getOpenPositions(Lists.newArrayList(
        pick(1, P),
        pick(2, OF),
        pick(3, FB),
        pick(4, P),
        pick(5, FB, OF),
        pick(6, SS),
        pick(7, SB),
        pick(8, OF),
        pick(9, P),
        pick(10, P),
        pick(11, DH)))
        .contains(OF));
  }

  @Test
  public void testSplitEligibilities() {
    Assert.assertEquals(Lists.newArrayList("1B"), RosterUtil.splitEligibilities("1B"));
    Assert.assertEquals(Lists.newArrayList("1B", "2B"), RosterUtil.splitEligibilities("1B,2B"));
    Assert.assertEquals(Lists.newArrayList("2B", "DH"), RosterUtil.splitEligibilities("DH,2B"));
    Assert.assertEquals(Lists.newArrayList("DH"), RosterUtil.splitEligibilities(""));
  }

  @Test
  public void testGetNumFilled() {
    ArrayList<DraftPick> picks = Lists.newArrayList(
        pick(1, 1, P),
        pick(2, 1, P),
        pick(3, 1, FB),
        pick(4, 1, SB),
        pick(5, 1, OF),
        pick(6, 2, P),
        pick(7, 2, P),
        pick(8, 2, P),
        pick(9, 2, FB),
        pick(10, 2, OF),
        pick(11, 2, OF),
        pick(12, 10, TB)
    );
    Map<Position,Integer[]> numFilled = new RosterUtil().getNumFilled(picks, 12);
    Assert.assertEquals(2, numFilled.get(FB)[0].intValue());
    Assert.assertEquals(1, numFilled.get(SB)[0].intValue());
    Assert.assertEquals(0, numFilled.get(SS)[0].intValue());
    Assert.assertEquals(2, numFilled.get(P)[1].intValue());
    Assert.assertEquals(1, numFilled.get(P)[2].intValue());
    Assert.assertEquals(0, numFilled.get(P)[3].intValue());
    Assert.assertEquals(2, numFilled.get(OF)[0].intValue());
    Assert.assertEquals(1, numFilled.get(OF)[1].intValue());
    Assert.assertEquals(0, numFilled.get(OF)[2].intValue());
    Assert.assertEquals(1, numFilled.get(TB)[0].intValue());
    numFilled = new RosterUtil().getNumFilled(picks, 9);
    Assert.assertEquals(2, numFilled.get(FB)[0].intValue());
    Assert.assertEquals(1, numFilled.get(SB)[0].intValue());
    Assert.assertEquals(0, numFilled.get(SS)[0].intValue());
    Assert.assertEquals(2, numFilled.get(P)[1].intValue());
    Assert.assertEquals(1, numFilled.get(P)[2].intValue());
    Assert.assertEquals(0, numFilled.get(P)[3].intValue());
    Assert.assertEquals(1, numFilled.get(OF)[0].intValue());
    Assert.assertEquals(0, numFilled.get(OF)[1].intValue());
    Assert.assertEquals(0, numFilled.get(OF)[2].intValue());
    Assert.assertEquals(0, numFilled.get(TB)[0].intValue());
  }

  @Test
  public void testGetHighestValuePosition() {
    Assert.assertEquals("SS", RosterUtil.getHighestValuePosition(Lists.newArrayList("SS", "3B", "1B")));
    Assert.assertEquals("3B", RosterUtil.getHighestValuePosition(Lists.newArrayList("3B", "1B", "OF")));
    Assert.assertEquals("DH", RosterUtil.getHighestValuePosition(Lists.newArrayList("DH")));
    Assert.assertEquals("P", RosterUtil.getHighestValuePosition(Lists.newArrayList("P")));
    Assert.assertEquals("SS", RosterUtil.getHighestValuePosition(Lists.newArrayList("SS", "SS", "1B")));
  }
}