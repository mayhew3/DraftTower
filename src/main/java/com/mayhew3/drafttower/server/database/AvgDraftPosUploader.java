package com.mayhew3.drafttower.server.database;

import com.google.common.collect.Lists;
import com.mayhew3.drafttower.server.database.dataobject.TmpDraftAveragesFactory;
import com.mayhew3.drafttower.server.database.dataobject.TmpEligibilityFactory;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class AvgDraftPosUploader implements DraftDataStep {
  private SQLConnection connection;
  private LocalDate statsDate;

  public static void main(String... args) throws URISyntaxException, SQLException, IOException {
    LocalDate statsDate = DraftPrepRunner.statsDate;
    SQLConnection connection = new MySQLConnectionFactory().createConnection();
    AvgDraftPosUploader avgDraftPosUploader = new AvgDraftPosUploader(connection, statsDate);
    avgDraftPosUploader.updateDatabase();
  }

  public AvgDraftPosUploader(SQLConnection connection, LocalDate statsDate) {
    this.connection = connection;
    this.statsDate = statsDate;
  }

  @Override
  public void updateDatabase() throws IOException, SQLException {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMdd");

    String pathname = "database/" + statsDate.getYear() + "/avgDraft" + simpleDateFormat.format(statsDate.toDate()) + ".csv";

    File file = new File(pathname);
    FileReader fileReader = new FileReader(file);

    PlayerListParser playerListParser = new PlayerListParser(fileReader, new TmpDraftAveragesFactory(), statsDate, Lists.newArrayList("Rank", "Avg Pick", "HI/LO", "% Drafted"), connection);
    playerListParser.uploadPlayersToDatabase();
  }

  @Override
  public String getStepName() {
    return "AvgDraftPosUploader";
  }

}
