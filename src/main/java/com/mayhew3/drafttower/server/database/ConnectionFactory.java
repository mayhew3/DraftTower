package com.mayhew3.drafttower.server.database;

import java.net.URISyntaxException;
import java.sql.SQLException;

public abstract class ConnectionFactory {

  abstract SQLConnection createConnection() throws URISyntaxException, SQLException;
}
