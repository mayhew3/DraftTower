package com.mayhew3.drafttower.shared;

import java.util.List;

/**
 * Response object for data to populate an unclaimed player table.
 */
public interface UnclaimedPlayerListResponse {

  List<Player> getPlayers();
  void setPlayers(List<Player> players);

  int getTotalPlayers();
  void setTotalPlayers(int totalPlayers);
}