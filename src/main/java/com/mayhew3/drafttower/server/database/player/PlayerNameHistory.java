package com.mayhew3.drafttower.server.database.player;

public class PlayerNameHistory extends DataObject {

  public FieldValueString playerString = registerStringField("PlayerString");
  public FieldValueInteger cbs_id = registerIntegerField("cbs_id");
  public FieldValueDate lastUpdated = registerDateField("LastUpdated");

  @Override
  protected String getTableName() {
    return "playernamehistory";
  }
}
