<?php
  require_once "databases.php";
/**
 * Created by IntelliJ IDEA.
 * User: Mayhew Seavey
 * Date: 3/19/11
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */

  $sql = "SELECT * FROM TmpEligibility";
  $rs = query($sql);

  while ($row = mysql_fetch_assoc($rs)) {
    $eligibilitiesString = $row['EligStr'];
    $eligibilities = explode(",", $eligibilitiesString);

    $playerID = $row['PlayerID'];
    $year = $row['Year'];

    foreach ($eligibilities as $position) {
      $sql = "SELECT 1 FROM Eligibilities WHERE PlayerID = $playerID AND Year = $year AND Position = '$position'";
      $rs2 = query($sql);
      if (mysql_num_rows($rs2) == 0) {
        $sql = "INSERT INTO Eligibilities (PlayerID, Year, Position)
                VALUES ($playerID, $year, '$position')";
        echo "$sql<br>";
        query($sql);
      }
    }
  }

?>