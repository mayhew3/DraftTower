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
  insertKeeper(1, 2, "Bautista, Jose LF TOR");
  insertKeeper(1, 4, "Cabrera, Miguel 1B DET");
  insertKeeper(1, 6, "Pujols, Albert 1B LAA");
  insertKeeper(1, 9, "Verlander, Justin SP DET");
  insertKeeper(1, 10, "Ellsbury, Jacoby CF BOS");
  insertKeeper(1, 12, "Kemp, Matt CF LAD");

  // second round
  insertKeeper(2, 9, "Votto, Joey 1B CIN");
  insertKeeper(2, 10, "Granderson, Curtis CF NYY");
  insertKeeper(2, 12, "Tulowitzki, Troy SS COL");

  // third round
  insertKeeper(3, 9, "Sabathia, CC SP NYY");
  insertKeeper(3, 10, "Cano, Robinson 2B NYY");
  insertKeeper(3, 12, "Gonzalez, Carlos LF COL");

  echo "Success!";
?>