package com.mayhew3.drafttower.client.players.unclaimed;

import com.mayhew3.drafttower.shared.Player;

/**
 * Player/value pair.
 */
class PlayerValue {
  private Player player;
  private String value;

  PlayerValue(Player player, String value) {
    this.player = player;
    this.value = value;
  }

  Player getPlayer() {
    return player;
  }

  String getValue() {
    return value;
  }
}