package com.mayhew3.drafttower.client.players.unclaimed;

import com.mayhew3.drafttower.client.players.PositionFilter;
import com.mayhew3.drafttower.shared.PlayerDataSet;

/**
 * View interface for main player table.
 */
public interface UnclaimedPlayerTablePanelView {
  void setPositionFilter(PositionFilter positionFilter);

  void setCopyRanksEnabled(boolean enabled, boolean defer);

  void updateDataSetButtons(PlayerDataSet playerDataSet);

  void updateUseForAutoPickCheckbox(boolean usersAutoPickWizardTable, boolean shouldBeEnabled);

  void setCloserLimits(int minClosersValue, int maxClosersValue);
}