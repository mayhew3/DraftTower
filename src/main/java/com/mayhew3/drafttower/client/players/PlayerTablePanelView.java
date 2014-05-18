package com.mayhew3.drafttower.client.players;

import com.mayhew3.drafttower.shared.PlayerDataSet;

/**
 * Interface description...
 */
public interface PlayerTablePanelView {
  void setPositionFilter(PositionFilter positionFilter, boolean unfilledSelected);

  void setCopyRanksEnabled(boolean enabled, boolean defer);

  void updateDataSetButtons(PlayerDataSet playerDataSet);

  void updateUseForAutoPickCheckbox(boolean usersAutoPickWizardTable, boolean shouldBeEnabled);
}