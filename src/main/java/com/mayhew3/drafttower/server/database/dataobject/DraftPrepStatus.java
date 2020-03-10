package com.mayhew3.drafttower.server.database.dataobject;

public class DraftPrepStatus extends DataObject {

  public FieldValueString lastCompletedStep = registerStringField("last_completed_step");

  @Override
  public String getTableName() {
    return "draft_prep_status";
  }
}
