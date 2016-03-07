package com.mayhew3.drafttower.server.database;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

public class ProjectionsUploaderRunner {

  public static void main(String... args) throws URISyntaxException, SQLException, IOException {
    SQLConnection connection = new MySQLConnectionFactory().createConnection();
    ProjectionsUploader projectionsUploader = new ProjectionsUploader(connection);
    projectionsUploader.updateDatabase();
  }
}
