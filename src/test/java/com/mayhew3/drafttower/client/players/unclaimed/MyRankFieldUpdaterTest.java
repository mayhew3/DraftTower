package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.TestPlayerGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test for {@link MyRankFieldUpdater}.
 */
public class MyRankFieldUpdaterTest {

  private TestPlayerGenerator playerGenerator;
  private MyRankFieldUpdater updater;
  private UnclaimedPlayerDataProvider presenter;

  @Before
  public void setUp() throws Exception {
    BeanFactory beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    playerGenerator = new TestPlayerGenerator(beanFactory);
    presenter = Mockito.mock(UnclaimedPlayerDataProvider.class);
    updater = new MyRankFieldUpdater(presenter);
  }

  @Test
  public void testUpdate() throws Exception {
    Player player = playerGenerator.generatePlayer(0, Position.C, 0);
    updater.update(0, player, "30");
    Mockito.verify(presenter).changePlayerRank(player, 30, 1);
  }

  @Test
  public void testUpdateNoOp() throws Exception {
    Player player = playerGenerator.generatePlayer(0, Position.C, 0);
    updater.update(0, player, "1");
    Mockito.verifyZeroInteractions(presenter);
  }

  @Test
  public void testUpdateBadInpu() throws Exception {
    Player player = playerGenerator.generatePlayer(1, Position.C, 1);
    updater.update(1, player, "foo");
    Mockito.verifyZeroInteractions(presenter);
  }
}