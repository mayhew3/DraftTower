<?php
  require_once "databases.php";
/**
 * Created by IntelliJ IDEA.
 * User: Mayhew Seavey
 * Date: 3/19/11
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */

  $sql = "SELECT * FROM YearlyBatting WHERE Year = 2012";
  $rs = query($sql);

  while ($row = mysql_fetch_assoc($rs)) {
    $eligibilitiesString = $row['Eligibility'];
    $eligibilities = explode(",", $eligibilitiesString);

    $playerID = $row['PlayerID'];

    $nonDH = array();

    foreach ($eligibilities as $eligibility) {
      $eligibility = trim($eligibility);
      if ($eligibility != "DH") {
        $nonDH[] = $eligibility;
      }
    }

    $after = implode(",", $nonDH);

    $sql = "UPDATE YearlyBatting
            SET Eligibility = '$after'
            WHERE PlayerID = $playerID";
    query($sql);
    echo ".";
  }
  echo "Done.";

?>