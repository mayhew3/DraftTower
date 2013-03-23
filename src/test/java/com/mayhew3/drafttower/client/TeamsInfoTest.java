package com.mayhew3.drafttower.client;

import com.google.common.collect.ImmutableSet;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.LoginResponse;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Class description...
 */
public class TeamsInfoTest {

  private TeamsInfo teamsInfo;
  private BeanFactory beanFactory;
  private LoginResponse loginResponse;

  @Before
  public void setUp() throws Exception {
    teamsInfo = new TeamsInfo(10);
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    loginResponse = beanFactory.createLoginResponse().as();
    loginResponse.setTeam(5);
    teamsInfo.setLoginResponse(loginResponse);
  }

  @Test
  public void testIsOnDeckNoKeepers() throws Exception {
    DraftStatus draftStatus = beanFactory.createDraftStatus().as();
    draftStatus.setNextPickKeeperTeams(ImmutableSet.<Integer>of());

    draftStatus.setCurrentTeam(4);
    Assert.assertTrue(teamsInfo.isOnDeck(draftStatus));

    draftStatus.setCurrentTeam(5);
    Assert.assertFalse(teamsInfo.isOnDeck(draftStatus));

    draftStatus.setCurrentTeam(6);
    Assert.assertFalse(teamsInfo.isOnDeck(draftStatus));

    loginResponse.setTeam(1);
    draftStatus.setCurrentTeam(10);
    Assert.assertTrue(teamsInfo.isOnDeck(draftStatus));
  }

  @Test
  public void testIsOnDeckKeepers() throws Exception {
    DraftStatus draftStatus = beanFactory.createDraftStatus().as();

    draftStatus.setCurrentTeam(3);
    draftStatus.setNextPickKeeperTeams(ImmutableSet.of(4));
    Assert.assertTrue(teamsInfo.isOnDeck(draftStatus));

    draftStatus.setCurrentTeam(2);
    Assert.assertFalse(teamsInfo.isOnDeck(draftStatus));

    draftStatus.setNextPickKeeperTeams(ImmutableSet.of(3, 4));
    Assert.assertTrue(teamsInfo.isOnDeck(draftStatus));

    loginResponse.setTeam(3);
    draftStatus.setCurrentTeam(9);
    draftStatus.setNextPickKeeperTeams(ImmutableSet.of(10, 1, 2));
    Assert.assertTrue(teamsInfo.isOnDeck(draftStatus));
  }
}