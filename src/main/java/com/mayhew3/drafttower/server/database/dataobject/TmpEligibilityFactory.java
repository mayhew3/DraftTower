package com.mayhew3.drafttower.server.database.dataobject;

public class TmpEligibilityFactory implements TmpStatTableFactory {
  @Override
  public TmpStatTable createTmpStatTable() {
    return new TmpEligibility();
  }
}
