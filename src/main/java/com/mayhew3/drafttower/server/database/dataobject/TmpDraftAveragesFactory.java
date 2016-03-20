package com.mayhew3.drafttower.server.database.dataobject;

public class TmpDraftAveragesFactory implements TmpStatTableFactory {
  @Override
  public TmpStatTable createTmpStatTable() {
    return new TmpDraftAverages();
  }
}
