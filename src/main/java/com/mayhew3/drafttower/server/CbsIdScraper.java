package com.mayhew3.drafttower.server;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gets player IDs from CBS data
 */
public class CbsIdScraper {

  public static final Pattern PLAYER_REGEX = Pattern.compile("/players/playerpage/(\\d+)\">([^<]+)</a>([^<]+)");

  public static void main(String[] args) throws Exception {
    FileWriter fileWriter = new FileWriter("database/cbsIds.csv");
    for (Entry<String, Integer> entry : getPlayerStringToCbsIdMap().entrySet()) {
      fileWriter.write("\"" + entry.getKey() + "\",\"" + entry.getValue() + "\"\n");
    }
    fileWriter.close();
  }

  public static Map<String, Integer> getPlayerStringToCbsIdMap() throws IOException {
    final Map<String, Integer> playerStringToCbsId = new HashMap<>();
    LineProcessor<Object> lineProcessor = new LineProcessor<Object>() {
      @Override
      public boolean processLine(String line) throws IOException {
        Matcher matcher = PLAYER_REGEX.matcher(line);
        if (matcher.find()) {
          int cbsId = Integer.parseInt(matcher.group(1));
          String playerString = matcher.group(2) + matcher.group(3);
          playerStringToCbsId.put(playerString, cbsId);
        }
        return true;
      }

      @Override
      public Object getResult() {
        return null;
      }
    };
    Files.readLines(new File("database/cbsBattersDump.txt"), Charset.defaultCharset(), lineProcessor);
    Files.readLines(new File("database/cbsPitchersDump.txt"), Charset.defaultCharset(), lineProcessor);
    return playerStringToCbsId;
  }
}