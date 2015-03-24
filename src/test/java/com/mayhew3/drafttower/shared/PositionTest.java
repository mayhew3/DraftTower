package com.mayhew3.drafttower.shared;

import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumSet;

import static com.mayhew3.drafttower.shared.Position.*;

/**
 * Tests for {@link Position}.
 */
public class PositionTest {

  private BeanFactory beanFactory;

  @Before
  public void setUp() throws Exception {
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
  }

  @Test
  public void testApplySinglePositionPositive() {
    Player player = beanFactory.createPlayer().as();
    player.setEligibility("C");
    Assert.assertTrue(Position.apply(player, EnumSet.of(C)));
  }

  @Test
  public void testApplySinglePositionNegative() {
    Player player = beanFactory.createPlayer().as();
    player.setEligibility("C");
    Assert.assertFalse(Position.apply(player, EnumSet.of(FB)));
  }

  @Test
  public void testApplyDHPositive() {
    Player player = beanFactory.createPlayer().as();
    player.setEligibility("C");
    Assert.assertTrue(Position.apply(player, EnumSet.of(DH)));
  }

  @Test
  public void testApplyDHPlusOtherPositionsPositive() {
    Player player = beanFactory.createPlayer().as();
    player.setEligibility("C");
    Assert.assertTrue(Position.apply(player, EnumSet.of(DH, FB)));
  }

  @Test
  public void testApplyDHNegative() {
    Player player = beanFactory.createPlayer().as();
    player.setEligibility("P");
    Assert.assertFalse(Position.apply(player, EnumSet.of(DH)));
  }

  @Test
  public void testApplyMultiPositionPositive() {
    Player player = beanFactory.createPlayer().as();
    player.setEligibility("C,1B");
    Assert.assertTrue(Position.apply(player, EnumSet.of(C, SB)));
  }

  @Test
  public void testApplyMultiPositionNegative() {
    Player player = beanFactory.createPlayer().as();
    player.setEligibility("C,1B");
    Assert.assertFalse(Position.apply(player, EnumSet.of(SB, TB)));
  }

  @Test
  public void testApplyAardsmaBug() {
    Player player = beanFactory.createPlayer().as();
    player.setEligibility(null);
    Assert.assertFalse(Position.apply(player, EnumSet.of(SB, TB)));
  }
}