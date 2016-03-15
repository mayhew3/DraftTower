package com.mayhew3.drafttower.server.database.player;

public abstract class TmpProjectionPlayer extends Player {

  public FieldValueString player = registerStringField("Player");
  public FieldValueInteger playerID = registerIntegerField("PlayerID");
  public FieldValueInteger rank = registerIntegerField("Rank");
  public FieldValueInteger fpts = registerIntegerField("FPTS");
  public FieldValueDate statDate = registerDateField("StatDate");
}
