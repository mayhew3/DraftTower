<?php
  require_once "databases.php";
  require_once "draftManager.php";
/**
 * Created by IntelliJ IDEA.
 * User: Mayhew Seavey
 * Date: 3/19/11
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */

   // first round
   insertKeeper(1, 1, "Cabrera, Miguel 1B DET");
   insertKeeper(1, 2, "Hamilton, Josh CF TEX");
   insertKeeper(1, 3, "Braun, Ryan LF MIL");
   insertKeeper(1, 4, "Buchholz, Clay SP BOS");
   insertKeeper(1, 5, "Halladay, Roy SP PHI");
   insertKeeper(1, 6, "Pierre, Juan LF CHW");
   insertKeeper(1, 11, "Crawford, Carl LF BOS");
   insertKeeper(1, 12, "Gonzalez, Carlos LF COL");

   // second round
   insertKeeper(2, 1, "Tulowitzki, Troy SS COL");
   insertKeeper(2, 2, "Ramirez, Hanley SS FLA");
   insertKeeper(2, 9, "Gonzalez, Adrian 1B BOS");
   insertKeeper(2, 10, "Cano, Robinson 2B NYY");
   insertKeeper(2, 11, "Reyes, Jose SS NYM");
   insertKeeper(2, 12, "Choo, Shin-Soo RF CLE");

   // third round
   insertKeeper(3, 1, "Jimenez, Ubaldo SP COL");
   insertKeeper(3, 2, "Wright, David 3B NYM");
   insertKeeper(3, 3, "Lincecum, Tim SP SF");
   insertKeeper(3, 4, "Pedroia, Dustin 2B BOS");
   echo "Success!";
?>