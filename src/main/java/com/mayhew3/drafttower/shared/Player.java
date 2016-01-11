package com.mayhew3.drafttower.shared;

/**
 * Data object for a player.
 */
public interface Player extends DraggableItem {

  // Marker constant for best draft pick.
  long BEST_DRAFT_PICK = -1;

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

  String getGS();
  void setGS(String value);

  String getAB();
  void setAB(String value);

  String getBA();
  void setBA(String value);

  String getOBP();
  void setOBP(String value);

  String getSLG();
  void setSLG(String value);

  String getRHR();
  void setRHR(String value);

  String getR();
  void setR(String value);

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

  String getBBI();
  void setBBI(String value);

  String getHA();
  void setHA(String value);

  String getHRA();
  void setHRA(String value);

  String getS();
  void setS(String value);

  String getH();
  void setH(String value);

  String getSB();
  void setSB(String value);

  String getBB();
  void setBB(String value);

  String getKO();
  void setKO(String value);

  String getW();
  void setW(String value);

  String getL();
  void setL(String value);

  String getPoints();
  void setPoints(String value);

  String getRank();
  void setRank(String value);

  String getDraft();
  void setDraft(String value);

  String getWizardP();
  void setWizardP(String value);

  String getWizardC();
  void setWizardC(String value);

  String getWizard1B();
  void setWizard1B(String value);

  String getWizard2B();
  void setWizard2B(String value);

  String getWizard3B();
  void setWizard3B(String value);

  String getWizardSS();
  void setWizardSS(String value);

  String getWizardOF();
  void setWizardOF(String value);

  String getWizardDH();
  void setWizardDH(String value);

  String getMyRank();
  void setMyRank(String value);

  boolean isFavorite();
  void setFavorite(boolean favorite);
}