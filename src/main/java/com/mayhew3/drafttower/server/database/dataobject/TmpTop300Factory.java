package com.mayhew3.drafttower.server.database.dataobject;

public class TmpTop300Factory implements TmpStatTableFactory {
  @Override
  public TmpStatTable createTmpStatTable() {
    return new TmpTop300();
  }
}
