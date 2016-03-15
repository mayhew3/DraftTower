package com.mayhew3.drafttower.server.database.dataobject;

public abstract class TmpProjectionPlayer extends DataObject {

  public FieldValueString player = registerStringField("Player");
  public FieldValueInteger playerID = registerIntegerField("PlayerID");
  public FieldValueInteger rank = registerIntegerField("Rank");
  public FieldValueInteger fpts = registerIntegerField("FPTS");
  public FieldValueDate statDate = registerDateField("StatDate");
}
