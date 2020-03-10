package com.mayhew3.drafttower.server.database;

import java.io.IOException;
import java.sql.SQLException;

public interface DraftDataStep {
  void updateDatabase() throws IOException, SQLException;
  String getStepName();
}
