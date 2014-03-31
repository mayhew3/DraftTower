package com.mayhew3.drafttower.shared;

import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link QueueEntryPredicate}.
 */
public class QueueEntryPredicateTest {

  private BeanFactory beanFactory;

  @Before
  public void setUp() throws Exception {
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
  }

  @Test
  public void testApplyPositive() throws Exception {
    QueueEntry queueEntry = beanFactory.createQueueEntry().as();
    queueEntry.setPlayerId(1);
    Assert.assertTrue(new QueueEntryPredicate(1).apply(queueEntry));
  }

  @Test
  public void testApplyNegative() throws Exception {
    QueueEntry queueEntry = beanFactory.createQueueEntry().as();
    queueEntry.setPlayerId(1);
    Assert.assertFalse(new QueueEntryPredicate(2).apply(queueEntry));
  }
}