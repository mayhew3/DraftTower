package com.mayhew3.drafttower.shared;

import com.google.common.base.Optional;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Tests for {@link PlayerDataSet}.
 */
public class PlayerDataSetTest {
  @Test
  public void testGetDataSetWithName() throws Exception {
    Assert.assertEquals(Optional.of(PlayerDataSet.CBSSPORTS),
        PlayerDataSet.getDataSetWithName(PlayerDataSet.CBSSPORTS.getDisplayName()));
    Assert.assertEquals(Optional.<PlayerDataSet>absent(),
        PlayerDataSet.getDataSetWithName("ZiPS"));
  }
}