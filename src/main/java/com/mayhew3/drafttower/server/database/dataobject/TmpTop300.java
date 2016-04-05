package com.mayhew3.drafttower.server.database.dataobject;

public class TmpTop300 extends TmpStatTable {

  public FieldValueInteger rank = registerIntegerField("Rank");
  public FieldValueInteger expert = registerIntegerField("Expert");

  @Override
  protected String getTableName() {
    return "tmp_top300";
  }
}
