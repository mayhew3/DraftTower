package com.mayhew3.drafttower.shared;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;

/**
 * Convenience methods for faking roster utils during tests.
 */
public class RosterTestUtils {

  @SuppressWarnings("unchecked")
  public static RosterUtil createSimpleFakeRosterUtil() {
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
    return rosterUtil;
  }
}