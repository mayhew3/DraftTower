package com.mayhew3.drafttower.shared;

import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;
import java.util.EnumSet;

import static com.mayhew3.drafttower.shared.Position.*;

/**
 * Tests for {@link PlayerColumn}.
 */
public class PlayerColumnTest {

  private BeanFactory beanFactory;

  @Before
  public void setUp() throws Exception {
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
  }

  @Test
  public void testNonNumericComparator() {
    Player p1 = beanFactory.createPlayer().as();
    p1.setName("aaa");
    Player p2 = beanFactory.createPlayer().as();
    p2.setName("zzz");
    Comparator<Player> desc = PlayerColumn.NAME.getComparator(false);
    Comparator<Player> asc = PlayerColumn.NAME.getComparator(true);
    assertComparators(p1, p2, desc, asc);
  }

  @Test
  public void testNumericComparator() {
    Player p1 = beanFactory.createPlayer().as();
    p1.setSBCS("-1");
    Player p2 = beanFactory.createPlayer().as();
    p2.setSBCS("1");
    Comparator<Player> desc = PlayerColumn.SBCS.getComparator(false);
    Comparator<Player> asc = PlayerColumn.SBCS.getComparator(true);
    assertComparators(p1, p2, desc, asc);
  }

  @Test
  public void testNumericComparatorNullValueMin() {
    Player p1 = beanFactory.createPlayer().as();
    p1.setSBCS(null);
    Player p2 = beanFactory.createPlayer().as();
    p2.setSBCS("1");
    Comparator<Player> desc = PlayerColumn.SBCS.getComparator(false);
    Comparator<Player> asc = PlayerColumn.SBCS.getComparator(true);
    assertComparators(p1, p2, desc, asc);
  }

  @Test
  public void testNumericComparatorNullValueMax() {
    Player p1 = beanFactory.createPlayer().as();
    p1.setWHIP(null);
    Player p2 = beanFactory.createPlayer().as();
    p2.setWHIP("1");
    Comparator<Player> desc = PlayerColumn.WHIP.getComparator(false);
    Comparator<Player> asc = PlayerColumn.WHIP.getComparator(true);
    assertComparators(p2, p1, desc, asc);
  }

  @Test
  public void testWizardComparatorSinglePosition() {
    Player p1 = beanFactory.createPlayer().as();
    p1.setWizard1B("-1");
    Player p2 = beanFactory.createPlayer().as();
    p2.setWizard1B("1");
    Comparator<Player> desc = PlayerColumn.getWizardComparator(false, EnumSet.of(FB));
    Comparator<Player> asc = PlayerColumn.getWizardComparator(true, EnumSet.of(FB));
    assertComparators(p1, p2, desc, asc);
  }

  @Test
  public void testWizardComparatorSinglePositionNullValue() {
    Player p1 = beanFactory.createPlayer().as();
    p1.setWizard2B("2");
    Player p2 = beanFactory.createPlayer().as();
    p2.setWizard1B("1");
    Comparator<Player> desc = PlayerColumn.getWizardComparator(false, EnumSet.of(FB));
    Comparator<Player> asc = PlayerColumn.getWizardComparator(true, EnumSet.of(FB));
    assertComparators(p1, p2, desc, asc);
  }

  @Test
  public void testWizardComparatorSinglePositionHasMultiPosition() {
    Player p1 = beanFactory.createPlayer().as();
    p1.setWizard1B("-1");
    p1.setWizard2B("2");
    Player p2 = beanFactory.createPlayer().as();
    p2.setWizard1B("1");
    Comparator<Player> desc = PlayerColumn.getWizardComparator(false, EnumSet.of(FB));
    Comparator<Player> asc = PlayerColumn.getWizardComparator(true, EnumSet.of(FB));
    assertComparators(p1, p2, desc, asc);
  }

  @Test
  public void testWizardComparatorMultiPosition() {
    Player p1 = beanFactory.createPlayer().as();
    p1.setWizard1B("-1");
    p1.setWizard2B("2");
    Player p2 = beanFactory.createPlayer().as();
    p2.setWizard1B("-2");
    p2.setWizard2B("3");
    Comparator<Player> desc = PlayerColumn.getWizardComparator(false, EnumSet.of(FB, SB));
    Comparator<Player> asc = PlayerColumn.getWizardComparator(true, EnumSet.of(FB, SB));
    assertComparators(p1, p2, desc, asc);
  }

  @Test
  public void testGetWizardSinglePosition() {
    Player p = beanFactory.createPlayer().as();
    p.setWizardP("1");
    Assert.assertEquals("1.0", PlayerColumn.getWizard(p, EnumSet.of(P)));
  }

  @Test
  public void testGetWizardMultiPosition() {
    Player p = beanFactory.createPlayer().as();
    p.setWizard1B("1");
    p.setWizard2B("2");
    Assert.assertEquals("2.0", PlayerColumn.getWizard(p, EnumSet.of(FB, SB, TB)));
  }

  @Test
  public void testGetWizardAllPositions() {
    Player p = beanFactory.createPlayer().as();
    p.setWizard1B("1");
    p.setWizard2B("2");
    p.setWizard3B("3");
    p.setWizardSS("4");
    p.setWizardC("5");
    p.setWizardDH("6");
    p.setWizardOF("7");
    Assert.assertEquals("7.0", PlayerColumn.getWizard(p, EnumSet.noneOf(Position.class)));
  }

  private void assertComparators(Player p1, Player p2, Comparator<Player> desc, Comparator<Player> asc) {
    Assert.assertTrue(desc.compare(p1, p2) > 0);
    Assert.assertTrue(desc.compare(p2, p1) < 0);
    Assert.assertTrue(desc.compare(p1, p1) == 0);
    Assert.assertTrue(asc.compare(p1, p2) < 0);
    Assert.assertTrue(asc.compare(p2, p1) > 0);
    Assert.assertTrue(asc.compare(p1, p1) == 0);
  }
}