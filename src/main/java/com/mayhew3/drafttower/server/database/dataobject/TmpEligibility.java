package com.mayhew3.drafttower.server.database.dataobject;

public class TmpEligibility extends TmpStatTable {

  public FieldValueString eligible = registerStringField("Eligible");

  @Override
  public String getTableName() {
    return "tmp_eligibility";
  }
}
