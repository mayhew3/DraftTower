package com.mayhew3.drafttower.server.database;

import org.joda.time.LocalDate;

public class CbsIdScraperRunner {
  public static void main(String... args) throws Exception {
    LocalDate localDate = new LocalDate(2016, 3, 6);
    CbsIdScraper cbsIdScraper = new CbsIdScraper(new MySQLConnectionFactory().createConnection(), localDate);
    cbsIdScraper.updateDatabase();
  }
}
