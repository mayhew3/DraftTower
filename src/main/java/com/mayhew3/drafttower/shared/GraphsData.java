package com.mayhew3.drafttower.shared;

import java.util.Map;

/**
 * Data for bar graphs.
 */
public interface GraphsData {

  Map<PlayerColumn, Float> getMyValues();
  void setMyValues(Map<PlayerColumn, Float> myValues);

  Map<PlayerColumn, Float> getAvgValues();
  void setAvgValues(Map<PlayerColumn, Float> avgValues);
}