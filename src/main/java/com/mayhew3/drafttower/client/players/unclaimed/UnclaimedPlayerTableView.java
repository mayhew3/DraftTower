package com.mayhew3.drafttower.client.players.unclaimed;

import com.mayhew3.drafttower.client.players.PlayerTableView;
import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.TableSpec;

/**
 * Interface for unclaimed player table.
 */
public interface UnclaimedPlayerTableView extends PlayerTableView<Player> {
  void initColumnSort(TableSpec tableSpec);

  PlayerColumn getSortedPlayerColumn();

  void playerDataSetUpdated();

  void positionFilterUpdated(boolean reSort);
}