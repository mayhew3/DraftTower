package com.mayhew3.drafttower.server.database.dataobject;

public class ProjectionBatter extends DataObject {

  public FieldValueInteger dataSource = registerIntegerField("DataSource");
  public FieldValueInteger playerID = registerIntegerField("PlayerID");
  public FieldValueInteger draft = registerIntegerField("Draft");
  public FieldValueInteger rank = registerIntegerField("Rank");
  public FieldValueInteger _1B = registerIntegerField("1B");
  public FieldValueInteger _2B = registerIntegerField("2B");
  public FieldValueInteger _3B = registerIntegerField("3B");
  public FieldValueInteger ab = registerIntegerField("AB");
  public FieldValueInteger bb = registerIntegerField("BB");
  public FieldValueInteger cs = registerIntegerField("CS");
  public FieldValueInteger g = registerIntegerField("G");
  public FieldValueInteger h = registerIntegerField("H");
  public FieldValueInteger hr = registerIntegerField("HR");
  public FieldValueInteger ko = registerIntegerField("KO");
  public FieldValueInteger r = registerIntegerField("R");
  public FieldValueInteger rbi = registerIntegerField("RBI");
  public FieldValueInteger sb = registerIntegerField("SB");
  public FieldValueInteger fpts = registerIntegerField("FPTS");

  @Override
  protected String getTableName() {
    return "projectionsbatting";
  }
}
