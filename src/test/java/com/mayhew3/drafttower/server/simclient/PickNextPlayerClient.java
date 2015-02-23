package com.mayhew3.drafttower.server.simclient;

import com.mayhew3.drafttower.server.SimulatedClient;
import com.mayhew3.drafttower.shared.DraftCommand.Command;

import javax.inject.Inject;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Simulated client which waits its turn and then picks the next player.
 */
public class PickNextPlayerClient extends SimulatedClient {

  @Inject
  public PickNextPlayerClient() {
    super();
  }

  @Override
  public void performAction() {
    if (connection == null) {
      try {
        login(false);
      } catch (ServletException | IOException e) {
        exceptions.add(e);
      }
    } else {
      if (commissionerTeam.equals(username) && draftStatus.getCurrentPickDeadline() == 0) {
        sendDraftCommand(Command.START_DRAFT, null);
      } else if (draftStatus.getCurrentTeam() == teamDraftOrder) {
        sendDraftCommand(Command.DO_PICK, players.get(draftStatus.getPicks().size()).getPlayerId());
      }
    }
  }
}