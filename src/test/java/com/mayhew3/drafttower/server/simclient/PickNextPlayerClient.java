package com.mayhew3.drafttower.server.simclient;

import com.mayhew3.drafttower.server.SimulatedClient;
import com.mayhew3.drafttower.shared.DraftCommand.Command;
import org.junit.Assert;

import javax.inject.Inject;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulated client which waits its turn and then picks the next player.
 */
public class PickNextPlayerClient extends SimulatedClient {

  private List<Exception> exceptions;

  @Inject
  public PickNextPlayerClient() {
    super();
    exceptions = new ArrayList<>();
  }

  @Override
  public void performAction() {
    if (teamToken == null) {
      try {
        login(false);
      } catch (ServletException | IOException e) {
        exceptions.add(e);
      }
    } else if ("1".equals(username) && draftStatus.getCurrentPickDeadline() == 0) {
      sendDraftCommand(Command.START_DRAFT, null);
    } else if (draftStatus.getCurrentTeam() == Integer.parseInt(username)) {
      sendDraftCommand(Command.DO_PICK, (long) draftStatus.getPicks().size());
    }
  }

  @Override
  public void verify() {
    if (!exceptions.isEmpty()) {
      Assert.fail("Client " + username + " failed to log in. First exception was: "
          + exceptions.get(0));
    }
  }
}