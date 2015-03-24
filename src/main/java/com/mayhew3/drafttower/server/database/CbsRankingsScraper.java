package com.mayhew3.drafttower.server.database;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gets player IDs from CBS data
 */
public class CbsRankingsScraper {

  private static final Pattern DATA_TABLE_REGEX = Pattern.compile("<table class=[\"']data[\"']");
  private static final Pattern RANKING_REGEX = Pattern.compile("<td +align=[\"']left[\"']>(\\d+)</td><td +align=[\"']left[\"']><a href=[\"']/fantasybaseball/players/playerpage/(\\d+)[\"']>");

  public static void main(String[] args) throws Exception {
    FileWriter fileWriter = new FileWriter("database/cbsRankings.csv");
    final Set<Integer> allIds = new HashSet<>();
    final Map<Integer, Integer> cbsIdToFirstRanking = new HashMap<>();
    final Map<Integer, Integer> cbsIdToSecondRanking = new HashMap<>();
    LineProcessor<Object> lineProcessor = new LineProcessor<Object>() {
      @Override
      public boolean processLine(String line) throws IOException {
        if (DATA_TABLE_REGEX.matcher(line).find()) {
          String[] splitByTable = line.split(DATA_TABLE_REGEX.pattern());
          readRankings(splitByTable[1], cbsIdToFirstRanking, allIds);
          readRankings(splitByTable[2], cbsIdToSecondRanking, allIds);
        }
        return true;
      }

      @Override
      public Object getResult() {
        return null;
      }
    };
    Files.readLines(new File("database/cbsRankingsDump.txt"), Charset.defaultCharset(), lineProcessor);
    for (Integer id : allIds) {
      fileWriter.write(id + ",");
      if (cbsIdToFirstRanking.containsKey(id)) {
        fileWriter.write("" + cbsIdToFirstRanking.get(id));
      }
      fileWriter.write(",");
      if (cbsIdToSecondRanking.containsKey(id)) {
        fileWriter.write("" + cbsIdToSecondRanking.get(id));
      }
      fileWriter.write("\n");
    }
    fileWriter.close();
  }

  private static void readRankings(String tableSource, Map<Integer, Integer> rankingMap, Set<Integer> allIds) {
    Matcher matcher = RANKING_REGEX.matcher(tableSource);
    while (matcher.find()) {
      int rank = Integer.parseInt(matcher.group(1));
      int cbsId = Integer.parseInt(matcher.group(2));
      allIds.add(cbsId);
      rankingMap.put(cbsId, rank);
    }
  }

}