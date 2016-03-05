package com.mayhew3.drafttower.server.database;

import com.google.common.collect.Lists;
import org.joda.time.DateMidnight;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DailyPlayer {

  static String[] nonStatNames = {
      "Player",
      "Team",
      "Rank",
      "StatDate"
  };
  Map<String, Object> fields;

  public DailyPlayer(DateMidnight statDate) {
    fields = new HashMap<>();
    fields.put("StatDate", statDate.toDate());
  }

  private static Integer getIntegerValue(String value) {
    try {
      return Integer.valueOf(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  protected abstract List<String> getSpecificFieldNames();

  protected abstract PreparedStatement getPreparedStatement();


  public void addFields(List<String> fieldNames, List<String> fieldValues) {
    for (int i = 0; i < fieldNames.size(); i++) {
      String fieldName = fieldNames.get(i);

      if (getValue(fieldName) == null) {

        String fieldValueStr = fieldValues.get(i);

        fieldValueStr = fieldValueStr.replace("\"", "").trim();

        Object value;
        Integer integerValue = getIntegerValue(fieldValueStr);

        if (integerValue != null) {
          value = integerValue;
        } else {
          value = fieldValueStr;
        }

        fields.put(fieldName, value);
      }
    }
  }

  public String getPlayerName() {
    return (String) getValue("Player");
  }

  public Object getValue(String fieldName) {
    return fields.get(fieldName);
  }

  public void updateDatabase(DatabaseConnection utility) throws SQLException {
    List<Object> values = getValuesList();
    List<String> specificFieldNames = getSpecificFieldNames();

    if (values.size() != nonStatNames.length + specificFieldNames.size()) {
      List<String> extraFieldValues = new ArrayList<>();
      for (String fieldName : fields.keySet()) {
        if (!Lists.newArrayList(specificFieldNames).contains(fieldName) &&
            !Lists.newArrayList(nonStatNames).contains(fieldName)) {
          extraFieldValues.add(fieldName);
        }
      }

      List<String> missingFieldValues = new ArrayList<>();
      for (String fieldName : specificFieldNames) {
        if (getValue(fieldName) == null) {
          missingFieldValues.add(fieldName);
        }
      }

      throw new RuntimeException("Missing fields: " + missingFieldValues + ". Extra fields: " + extraFieldValues);
    }
    utility.executePreparedUpdateWithParamsWithoutClose(getPreparedStatement(), values);
  }

  private List<Object> getValuesList() {
    List<Object> objects = new ArrayList<>();
    for (String fieldName : nonStatNames) {
      objects.add(getValue(fieldName));
    }

    for (String fieldName : getSpecificFieldNames()) {
      objects.add(getValue(fieldName));
    }

    return objects;
  }
}
