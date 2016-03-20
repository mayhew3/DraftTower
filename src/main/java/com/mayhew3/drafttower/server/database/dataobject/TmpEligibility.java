package com.mayhew3.drafttower.server.database.dataobject;

public class TmpEligibility extends TmpStatTable {

  public FieldValueString eligible = registerStringField("Eligible");

  @Override
  protected String getTableName() {
    return "tmp_eligibility";
  }
}
