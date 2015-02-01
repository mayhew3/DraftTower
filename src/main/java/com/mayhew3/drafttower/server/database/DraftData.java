package com.mayhew3.drafttower.server.database;

import static com.mayhew3.drafttower.server.database.InputGetter.grabInput;

public class DraftData {
  public static void main(String[] args) {

    DatabaseUtility.initConnection();

    boolean stillRunning = true;

    while (stillRunning) {
      String command = grabInput("Enter command: ");

      switch (command) {
        case "quit": stillRunning = false; break;
        case "exit": stillRunning = false; break;
        default: System.out.println("Command not found. Try again.");
      }
    }

    System.out.println("Exiting.");
  }

}
