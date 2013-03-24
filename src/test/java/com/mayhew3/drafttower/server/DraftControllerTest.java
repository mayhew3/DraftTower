package com.mayhew3.drafttower.server;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;
import com.mayhew3.drafttower.server.BindingAnnotations.Keepers;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftStatus;
import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests for {@link DraftController}.
 */
public class DraftControllerTest {

  @Rule
  public final GuiceBerryRule guiceBerry = new GuiceBerryRule(GuiceBerryEnv.class);

  @Inject private DraftController draftController;
  @Inject private DraftStatus draftStatus;
  @Inject @Keepers private ListMultimap<Integer, Integer> keepers;
  @Inject private BeanFactory beanFactory;

  @Test
  public void testBackOutLastPickSkipsKeepers() throws Exception {
    draftStatus.setPicks(Lists.newArrayList(
        beanFactory.createDraftPick().as(),
        beanFactory.createDraftPick().as(),
        beanFactory.createDraftPick().as(),
        beanFactory.createDraftPick().as()));
    draftStatus.setCurrentTeam(5);
    keepers.put(4, 0);
    draftController.backOutLastPick();
    Assert.assertEquals(3, draftStatus.getCurrentTeam());
    Assert.assertEquals(2, draftStatus.getPicks().size());
  }
}