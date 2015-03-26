package com.mayhew3.drafttower.server.database;

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
public class CbsDraftPosScraper {

  private static final Pattern PLAYER_REGEX = Pattern.compile("<td align=[\"']left[\"']>(\\d+)</td><td align=[\"']left[\"']><a class=[\"']playerLink[\"'] href=[\"']/players/playerpage/(\\d+)[\"']>");

  public static void main(String[] args) throws Exception {
    FileWriter fileWriter = new FileWriter("database/cbsDraftPos.csv");
    final Map<Integer, Integer> cbsIdToDraftPos = new HashMap<>();
    LineProcessor<Object> lineProcessor = new LineProcessor<Object>() {
      @Override
      public boolean processLine(String line) throws IOException {
        Matcher matcher = PLAYER_REGEX.matcher(line);
        if (matcher.find()) {
          int draftPos = Integer.parseInt(matcher.group(1));
          int cbsId = Integer.parseInt(matcher.group(2));
          cbsIdToDraftPos.put(cbsId, draftPos);
        }
        return true;
      }

      @Override
      public Object getResult() {
        return null;
      }
    };
    Files.readLines(new File("database/cbsDraftPosDump.txt"), Charset.defaultCharset(), lineProcessor);
    for (Entry<Integer, Integer> entry : cbsIdToDraftPos.entrySet()) {
      fileWriter.write(entry.getKey() + "," + entry.getValue());
      fileWriter.write("\n");
    }
    fileWriter.close();
  }
}