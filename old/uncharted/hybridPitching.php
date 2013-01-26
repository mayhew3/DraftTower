<?php
  require_once "databases.php";

  $sql = "SELECT * FROM BieberPitchComparison";
  $rs = query($sql);
?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Uncharted Territories</title>
<link href="standard2.css" rel="stylesheet" type="text/css" />
<!--[if IE]>
<style type="text/css"> 
/* place css fixes for all versions of IE in this conditional comment */
.twoColHybLt #sidebar1 { padding-top: 30px; }
.twoColHybLt #mainContent { padding-top: 15px; }
/* the above proprietary zoom property gives IE the hasLayout it may need to avoid several bugs */
</style>
<![endif]--></head>

<body class="twoColHybLt">

<div id="container">
  <div id="header">
    <img src="images/Banner3.jpg" alt="Uncharted Territories" />
  <!-- end #header --></div>
  <div id="sidebar1">
    <? include "navtable.php"?>
  <!-- end #sidebar1 --></div>
  <div id="mainContent">
    <div class="headerText"> Uncharted Territories: Scoring Systems </div>
   <br />

    <table>

      <?php
        while ($row = mysql_fetch_assoc($rs)) {

      ?>
       <tr>
           <td>
               <table>
                   <tr valign="middle">
                       <td rowspan="2" valign="middle" class="bordered"><?=$row['Year']?><br><?=$row['Period']?></td>
                       <td class="bordered"><?=$row['Team1']?></td>
                       <td class="bordered"><?=$row['BieberPitching1']?></td>
                       <td class="bordered"><?=$row['ShangPitching1']?></td>
                       <td rowspan="2" valign="middle" class="bordered"><?=$row['ModifiedDiff']?></td>
                       <td rowspan="2" valign="middle" class="bordered">
                           <a href="gameDetail.php?T1=<?=$row['Team1ID']."&T2=".$row['Team2ID']."&Period=".$row['Period']?>">Detail</a>
                       </td>
                   </tr>
                   <tr>
                       <td class="bordered"><?=$row['Team2']?></td>
                       <td class="bordered"><?=$row['BieberPitching2']?></td>
                       <td class="bordered"><?=$row['ShangPitching2']?></td>
                   </tr>
               </table>
           </td>
       </tr>


      <?php } ?>
    </table>
    
  <!-- end #mainContent --></div>
  <!-- This clearing element should immediately follow the #mainContent div in order to force the #container div to contain all child floats --><br class="clearfloat" />
<!-- end #container --></div>
</body>
</html>
