package com.mayhew3.drafttower.shared;

import java.util.Map;

import static com.mayhew3.drafttower.shared.PlayerColumn.*;

/**
 * Data for bar graphs.
 */
public interface GraphsData {

  PlayerColumn[] GRAPH_STATS = new PlayerColumn[] {HR, RBI, OBP, SLG, RHR, SBCS, INN, K, ERA, WHIP, WL, S};

  Map<PlayerColumn, Float> getMyValues();
  void setMyValues(Map<PlayerColumn, Float> myValues);

  Map<PlayerColumn, Float> getAvgValues();
  void setAvgValues(Map<PlayerColumn, Float> avgValues);

  // Can't use Integer as key - see https://code.google.com/p/google-web-toolkit/issues/detail?id=7395
  Map<String, Float> getTeamPitchingValues();
  void setTeamPitchingValues(Map<String, Float> teamPitchingValues);

  Map<String, Float> getTeamBattingValues();
  void setTeamBattingValues(Map<String, Float> teamBattingValues);

  Map<String, Float> getTeamTotals();
  void setTeamTotals(Map<String, Float> teamTotals);
}