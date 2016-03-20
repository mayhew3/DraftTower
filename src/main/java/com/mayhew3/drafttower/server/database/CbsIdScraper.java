package com.mayhew3.drafttower.server.database;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.mayhew3.drafttower.server.database.dataobject.CbsID;
import com.mayhew3.drafttower.server.database.dataobject.PlayerNameHistory;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gets player IDs from CBS data
 */
public class CbsIdScraper {

  private static final Logger logger = Logger.getLogger(CbsIdScraper.class.getName());

  private static final Pattern PLAYER_REGEX = Pattern.compile("/players/playerpage/(\\d+)[\"']>([^<]+)</a> <span class=[\"']playerPositionAndTeam[\"']>([^<]+)");
  private static final Pattern INJURY_REGEX = Pattern.compile("<span title=[\"']([^\"']+)[\"']><a href=[\"']/players/playerpage/\\d+[\"'] subtab=[\"']Injury Report[\"']");

  private SQLConnection connection;
  private LocalDate localDate;

  public CbsIdScraper(SQLConnection connection, LocalDate localDate) {
    this.connection = connection;
    this.localDate = localDate;
  }

  public void updateDatabase() throws IOException {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMdd");
    String dateString = simpleDateFormat.format(localDate.toDate());

    LineProcessor<Object> lineProcessor = new LineProcessor<Object>() {
      @Override
      public boolean processLine(String line) throws IOException {
        Matcher matcher = PLAYER_REGEX.matcher(line);
        if (matcher.find()) {
          int id = Integer.parseInt(matcher.group(1));

          try {
            CbsID cbsID = findExistingPlayer(id);
            String playerString = matcher.group(2) + " " + matcher.group(3).replace("| ", "");

            cbsID.playerString.changeValue(playerString);

            matcher = INJURY_REGEX.matcher(line);
            if (matcher.find()) {
              String injuryNote = matcher.group(1);
              cbsID.injuryNote.changeValue(injuryNote);
            }

            if (cbsID.isForInsert()) {
              logger.log(Level.INFO, "Inserting new player: " + cbsID.playerString.getValue());
            } else {
              if (cbsID.hasChanged()) {
                if (cbsID.playerString.isChanged()) {
                  createHistoryEvent(cbsID);
                }
                logger.log(Level.INFO, "Updating player: '" + cbsID.playerString.getOriginalValue() + "' -> '" + cbsID.playerString.getChangedValue() + "'");
                cbsID.dateModified.changeValue(localDate.toDate());
              }
            }

            cbsID.commit(connection);

          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
        return true;
      }

      @Override
      public Object getResult() {
        return null;
      }
    };

    String battersInputFilename = "database/" + localDate.getYear() + "/battersList" + dateString + ".html";
    String pitchersInputFilename = "database/" + localDate.getYear() + "/pitchersList" + dateString + ".html";

    Files.readLines(new File(battersInputFilename), Charset.defaultCharset(), lineProcessor);
    Files.readLines(new File(pitchersInputFilename), Charset.defaultCharset(), lineProcessor);

  }

  private void createHistoryEvent(CbsID cbsID) throws SQLException {
    PlayerNameHistory playerNameHistory = new PlayerNameHistory();
    playerNameHistory.initializeForInsert();

    playerNameHistory.cbs_id.changeValue(cbsID.cbs_id.getValue());
    playerNameHistory.playerString.changeValue(cbsID.playerString.getOriginalValue());
    playerNameHistory.lastUpdated.changeValue(cbsID.dateModified.getOriginalValue());

    playerNameHistory.commit(connection);
  }

  private CbsID findExistingPlayer(Integer id) throws SQLException {
    CbsID cbsID = new CbsID();

    String sql = "SELECT * FROM cbsids WHERE cbs_id = ?";
    ResultSet resultSet = connection.prepareAndExecuteStatementFetch(sql, id);

    if (resultSet.next()) {
      cbsID.initializeFromDBObject(resultSet);
    } else {
      cbsID.initializeForInsert();
      cbsID.cbs_id.changeValue(id);
      cbsID.dateAdded.changeValue(localDate.toDate());
      cbsID.dateModified.changeValue(localDate.toDate());
    }

    return cbsID;
  }

}