package com.mayhew3.drafttower.server.database.dataobject;

public class TmpProjectionBatterFactory implements TmpStatTableFactory {
  @Override
  public TmpStatTable createTmpStatTable() {
    return new TmpProjectionBatter();
  }
}
