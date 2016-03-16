package com.mayhew3.drafttower.server.database.dataobject;

public class TmpProjectionPitcherFactory implements TmpStatTableFactory {
  @Override
  public TmpStatTable createTmpStatTable() {
    return new TmpProjectionPitcher();
  }
}
