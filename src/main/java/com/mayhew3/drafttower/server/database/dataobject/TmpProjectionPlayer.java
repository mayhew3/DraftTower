package com.mayhew3.drafttower.server.database.dataobject;

public abstract class TmpProjectionPlayer extends TmpStatTable {

  public FieldValueInteger playerID = registerIntegerField("PlayerID");
  public FieldValueInteger rank = registerIntegerField("Rank");
  public FieldValueInteger fpts = registerIntegerField("FPTS");
}
