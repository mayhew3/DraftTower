package com.mayhew3.drafttower.server.database;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

public class PlayerInfo {
  public String firstName;
  public String lastName;
  public String MLBTeam;
  public String Position;

  @Override
  public String toString() {
    return lastName + ", " + firstName + " " + MLBTeam + " " + Position;
  }

  private static PlayerInfo parseFromString(int id, String playerString) throws FailedPlayer {
    PlayerInfo playerInfo = new PlayerInfo();

    String[] commaParts = playerString.split(", ");
    if (commaParts.length != 2) {
      throw new FailedPlayer(id, "Found player without exactly one comma.");
    }

    playerInfo.lastName = commaParts[0];

    String remainingString = commaParts[1];

    List<String> spaceParts = Lists.newArrayList(remainingString.split(" "));
    int numParts = spaceParts.size();


    if (numParts < 3) {
      throw new FailedPlayer(id, "Found player with fewer than 3 symbols after the comma: '" +
          remainingString + "', Player " + playerString);
    }

    playerInfo.MLBTeam = Iterables.getLast(spaceParts);
    spaceParts.remove(playerInfo.MLBTeam);

    playerInfo.Position = Iterables.getLast(spaceParts);
    spaceParts.remove(playerInfo.Position);

    if (playerInfo.MLBTeam.length() < 2) {
      throw new FailedPlayer(id, "Incorrect team name '" + playerInfo.MLBTeam + "', from remainder string '" + remainingString + "'");
    }

    if (playerInfo.Position.length() < 1) {
      throw new FailedPlayer(id, "Incorrect position '" + playerInfo.Position + "', from remainder string '" + remainingString + "'");
    }


    if (spaceParts.size() < 1) {
      throw new FailedPlayer(id, "Found no parts remaining in the first name piece.");
    }

    Joiner joiner = Joiner.on(" ");
    playerInfo.firstName = joiner.join(spaceParts);
    return playerInfo;
  }

}
