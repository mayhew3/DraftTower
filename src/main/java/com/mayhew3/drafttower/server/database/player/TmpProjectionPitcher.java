package com.mayhew3.drafttower.server.database.player;

public class TmpProjectionPitcher extends TmpProjectionPlayer {

  public FieldValueInteger app = registerIntegerField("APP");
  public FieldValueInteger bbi = registerIntegerField("BBI");
  public FieldValueInteger bs = registerIntegerField("BS");
  public FieldValueInteger cg = registerIntegerField("CG");
  public FieldValueInteger er = registerIntegerField("ER");
  public FieldValueInteger gs = registerIntegerField("GS");
  public FieldValueInteger ha = registerIntegerField("HA");
  public FieldValueInteger hra = registerIntegerField("HRA");
  public FieldValueInteger k = registerIntegerField("K");
  public FieldValueInteger l = registerIntegerField("L");
  public FieldValueInteger qs = registerIntegerField("QS");
  public FieldValueInteger outs = registerIntegerField("OUTS");
  public FieldValueInteger s = registerIntegerField("S");
  public FieldValueInteger so = registerIntegerField("SO");
  public FieldValueInteger w = registerIntegerField("W");

  @Override
  protected String getTableName() {
    return "tmp_cbspitching";
  }
}
