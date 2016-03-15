package com.mayhew3.drafttower.server.database.player;

public class CbsID extends DataObject {

  public FieldValueInteger cbs_id = registerIntegerField("cbs_id");
  public FieldValueString playerString = registerStringField("PlayerString");
  public FieldValueString injuryNote = registerStringField("InjuryNote");
  public FieldValueDate dateAdded = registerDateField("DateAdded");
  public FieldValueDate dateModified = registerDateField("DateModified");

  @Override
  protected String getTableName() {
    return "cbsids";
  }
}
