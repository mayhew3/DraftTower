package com.mayhew3.drafttower.server.database.dataobject;

public class Player extends DataObject {

  public FieldValueInteger cbs_id = registerIntegerField("cbs_id");
  public FieldValueString playerString = registerStringField("PlayerString");
  public FieldValueString newPlayerString = registerStringField("NewPlayerString");
  public FieldValueString firstName = registerStringField("FirstName");
  public FieldValueString lastName = registerStringField("LastName");
  public FieldValueString mlbTeam = registerStringField("MLBTeam");
  public FieldValueString position = registerStringField("Position");
  public FieldValueString eligibility = registerStringField("Eligibility");
  public FieldValueString injury = registerStringField("Injury");
  public FieldValueDate createTime = registerDateField("CreateTime");
  public FieldValueDate updateTime = registerDateField("UpdateTime");
  public FieldValueInteger matchPending = registerIntegerField("MatchPending");

  @Override
  public String getTableName() {
    return "players";
  }
}
