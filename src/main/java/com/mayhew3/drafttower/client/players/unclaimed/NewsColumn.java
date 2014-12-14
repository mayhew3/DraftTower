package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.user.cellview.client.Column;
import com.mayhew3.drafttower.shared.Player;

/**
 * Table column for player news link.
 */
class NewsColumn extends Column<Player, String> {
  public NewsColumn() {
    super(new ClickableTextCell());
  }

  @Override
  public String getValue(Player object) {
    return "?";
  }
}