package com.mayhew3.drafttower.server.database.dataobject;

public class TmpEligibility extends DataObject {

  public FieldValueString player = registerStringField("Player");
  public FieldValueString eligible = registerStringField("Eligible");
  public FieldValueDate statDate = registerDateField("StatDate");

  @Override
  protected String getTableName() {
    return "tmp_eligibility";
  }
}
