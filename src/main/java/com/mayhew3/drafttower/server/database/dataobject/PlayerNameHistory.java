package com.mayhew3.drafttower.server.database.dataobject;

public class PlayerNameHistory extends DataObject {

  public FieldValueString playerString = registerStringField("PlayerString");
  public FieldValueInteger cbs_id = registerIntegerField("cbs_id");
  public FieldValueDate lastUpdated = registerDateField("LastUpdated");
  public FieldValueDate dateChanged = registerDateField("DateChanged");

  @Override
  public String getTableName() {
    return "playernamehistory";
  }
}
