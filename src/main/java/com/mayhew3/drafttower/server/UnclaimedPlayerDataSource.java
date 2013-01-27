package com.mayhew3.drafttower.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.shared.*;

import java.util.List;

/**
 * Looks up unclaimed players in the database.
 */
@Singleton
public class UnclaimedPlayerDataSource {

  private final BeanFactory beanFactory;

  @Inject
  public UnclaimedPlayerDataSource(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  public UnclaimedPlayerListResponse lookup(UnclaimedPlayerListRequest request) {
    UnclaimedPlayerListResponse response = beanFactory.createUnclaimedPlayerListResponse().as();

    // TODO(m3)
    List<Player> players = Lists.newArrayList();
    for (int i = 0; i < request.getRowCount() && request.getRowStart() + i < 300; i++) {
      Player player = beanFactory.createPlayer().as();
      player.setPlayerId(1);
      player.setColumnValues(ImmutableMap.<PlayerColumn, String>builder()
          .put(PlayerColumn.NAME, "Joakim Soria " + (request.getRowStart() + i + 1))
          .put(PlayerColumn.POS, "RP")
          .put(PlayerColumn.ELIG, "P")
          .put(PlayerColumn.INN, "50")
          .put(PlayerColumn.K, "53")
          .put(PlayerColumn.ERA, "2.70")
          .put(PlayerColumn.WHIP, "1.12")
          .put(PlayerColumn.WL, "2")
          .put(PlayerColumn.S, "33")
          .put(PlayerColumn.RANK, "142")
          .put(PlayerColumn.RATING, "-0.65")
          .build());
      players.add(player);
    }
    response.setPlayers(players);
    response.setTotalPlayers(300);

    return response;
  }
}