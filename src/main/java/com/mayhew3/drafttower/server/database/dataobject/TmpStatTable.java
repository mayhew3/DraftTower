package com.mayhew3.drafttower.server.database.dataobject;

public abstract class TmpStatTable extends DataObject {

  public FieldValueString player = registerStringField("Player");
  public FieldValueDate statDate = registerDateField("StatDate");
}
