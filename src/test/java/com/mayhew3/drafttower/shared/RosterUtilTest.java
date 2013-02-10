package com.mayhew3.drafttower.shared;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
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

  private void assertPicks(List<Entry<Position, Long>> expectedIds, List<DraftPick> picks) {
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
    assertPicks(
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
            pick(10, SP),
            pick(11, RP),
            pick(12, SP),
            pick(13, SP),
            pick(14, RP),
            pick(15, SP),
            pick(16, SP)));
  }

  @Test
  public void testConstructRosterDH() throws Exception {
    assertPicks(
        Lists.newArrayList(
            Maps.immutableEntry(FB, 1l),
            Maps.immutableEntry(DH, 2l)),
        Lists.newArrayList(
            pick(1, FB),
            pick(2, FB)));
  }

  @Test
  public void testConstructRosterReserves() throws Exception {
    assertPicks(
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
            pick(4, SP),
            pick(5, SP),
            pick(6, SP),
            pick(7, SP),
            pick(8, SP),
            pick(9, SP),
            pick(10, SP),
            pick(11, SP)));
  }

  @Test
  public void testConstructRosterMultipleEligibility() throws Exception {
    assertPicks(
        Lists.newArrayList(
            Maps.immutableEntry(TB, 1l),
            Maps.immutableEntry(FB, 2l),
            Maps.immutableEntry(SB, 3l)),
        Lists.newArrayList(
            pick(1, FB, SB, TB),
            pick(2, FB),
            pick(3, SB)));
  }
}