package com.mayhew3.drafttower.server.database.dataobject;

public class TmpDraftAverages extends TmpStatTable {

  public FieldValueBigDecimal avg_pick = registerBigDecimalField("Avg Pick");
  public FieldValueBigDecimal percent_drafted = registerBigDecimalField("% Drafted");
  public FieldValueString hi_lo = registerStringField("HI/LO");
  public FieldValueInteger playerID = registerIntegerField("PlayerID");
  public FieldValueInteger rank = registerIntegerField("Rank");

  @Override
  public String getTableName() {
    return "cbs_draftaverages";
  }
}
