package com.mayhew3.drafttower.shared;

import java.util.Map;

import static com.mayhew3.drafttower.shared.PlayerColumn.*;

/**
 * Data for bar graphs.
 */
public interface GraphsData {

  PlayerColumn[] GRAPH_STATS = {
      HR, RBI, OBP, SLG, RHR, SBCS, INN, K, ERA, WHIP, WL, S
  };

  Map<PlayerColumn, Float> getMyValues();
  void setMyValues(Map<PlayerColumn, Float> myValues);

  Map<PlayerColumn, Float> getAvgValues();
  void setAvgValues(Map<PlayerColumn, Float> avgValues);
}