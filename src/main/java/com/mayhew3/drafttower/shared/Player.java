package com.mayhew3.drafttower.shared;

/**
 * Data object for a player.
 */
public interface Player {

  // Marker constant for best draft pick.
  public static final long BEST_DRAFT_PICK = -1;

  long getPlayerId();
  void setPlayerId(long playerId);

  long getCBSId();
  void setCBSId(long cbsId);

  String getInjury();
  void setInjury(String injury);

  String getName();
  void setName(String value);

  String getTeam();
  void setTeam(String value);

  String getEligibility();
  void setEligibility(String value);

  String getG();
  void setG(String value);

  String getAB();
  void setAB(String value);

  String getOBP();
  void setOBP(String value);

  String getSLG();
  void setSLG(String value);

  String getRHR();
  void setRHR(String value);

  String getRBI();
  void setRBI(String value);

  String getHR();
  void setHR(String value);

  String getSBCS();
  void setSBCS(String value);

  String getINN();
  void setINN(String value);

  String getERA();
  void setERA(String value);

  String getWHIP();
  void setWHIP(String value);

  String getWL();
  void setWL(String value);

  String getK();
  void setK(String value);

  String getS();
  void setS(String value);

  String getRank();
  void setRank(String value);

  String getDraft();
  void setDraft(String value);

  String getWizard();
  void setWizard(String value);

  String getMyRank();
  void setMyRank(String value);
}