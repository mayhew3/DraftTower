package com.mayhew3.drafttower.shared;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map.Entry;

import static com.mayhew3.drafttower.shared.Position.*;

/**
 * Tests for {@link RosterUtil}.
 */
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

  private void assertRoster(List<Entry<Position, Long>> expectedIds, List<DraftPick> picks) {
    Multimap<Position, DraftPick> roster = RosterUtil.constructRoster(picks);
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
  public void testConstructRosterBigPapi() throws Exception {
    assertRoster(
        Lists.newArrayList(
            Maps.immutableEntry(DH, 1l)),
        Lists.newArrayList(
            pick(1, DH)));
  }

  @Test
  public void testAllPositionsOpen() throws Exception {
    Assert.assertEquals(Sets.newHashSet(C, FB, SB, TB, SS, OF, DH, P),
        RosterUtil.getOpenPositions(ImmutableList.<DraftPick>of()));
  }

  @Test
  public void testGetOpenPositionsSingleEligibility() throws Exception {
    Assert.assertEquals(Sets.newHashSet(FB, SB, SS, OF, DH, P),
        RosterUtil.getOpenPositions(Lists.newArrayList(
            pick(1, C),
            pick(2, TB))));
  }

  @Test
  public void testGetOpenPositionsMultiEligibility() throws Exception {
    Assert.assertEquals(Sets.newHashSet(FB, SB, TB, SS, OF, DH, P),
        RosterUtil.getOpenPositions(Lists.newArrayList(
            pick(1, C),
            pick(2, SB, SS))));
  }

  @Test
  public void testGetOpenPositionsMultiSlotPosition() throws Exception {
    Assert.assertEquals(Sets.newHashSet(C, FB, SB, TB, SS, OF, DH, P),
        RosterUtil.getOpenPositions(Lists.newArrayList(
            pick(1, P),
            pick(2, P),
            pick(3, P),
            pick(4, P),
            pick(5, P),
            pick(6, P))));
    Assert.assertEquals(Sets.newHashSet(C, FB, SB, TB, SS, OF, DH),
        RosterUtil.getOpenPositions(Lists.newArrayList(
            pick(1, P),
            pick(2, P),
            pick(3, P),
            pick(4, P),
            pick(5, P),
            pick(6, P),
            pick(7, P))));
  }
}