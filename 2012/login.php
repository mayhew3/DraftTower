<?php
  require_once "databases.php";
  disconnect();
  connect_users();

   if ((!is_null($_GET["type"]) && ($_GET["type"] == "guest"))) {
      $_SESSION["UserID"] = "guest";
    $_SESSION["FirstName"] = "Guest";
    $_SESSION["Role"] = "guest";
    header("Location: index.php");
   } elseif ($_POST["Message"] == "True") {
 
     $usrID = urlencode($_POST["UserID"]); 
     $PWord = urlencode($_POST["PWord"]); 
 
  $strSQL = "SELECT u.FirstName, u.Pword, ur.Role
        FROM UserRole ur
        INNER JOIN Users u
          ON ur.User = u.user
        WHERE u.user = '".$usrID."' AND ur.Site = 'Uncharted'";

  $rs = query($strSQL);

  if (mysql_numrows($rs) == 0) {
    $fail_error = "User ID '".$usrID."' not found. Please use a valid ID.";
  } else {
    $row = mysql_fetch_assoc($rs);
    $pwTmp = $row["Pword"];
    if ($pwTmp == $PWord) {
      $_SESSION["UserID"] = $usrID;
      $_SESSION["FirstName"] = $row["FirstName"];
      $_SESSION["Role"] = $row["Role"];
      header("Location: draftWizard.php");
    } else {
      $fail_error = "Invalid password for User '".$usrID."'. Please try again.";
    }
  }
 
    } 
  ?> 

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Uncharted Territories</title>
<link href="standard2.css" rel="stylesheet" type="text/css" />
</head>

<body class="twoColHybLt">

<div id="container">

  <div id="mainContent">
    <h2> Login </h2>
    
    <form METHOD="POST" ACTION="login.php">
        <table>
        <tr><td ALIGN="Right">
        User ID: <input TYPE="Text" NAME="UserID" CLASS="inField"><br>
        Password: <input TYPE="Password" NAME="PWord" CLASS="inField">
        </td>
        <td VALIGN="Bottom">
        <input TYPE="HIDDEN" NAME="Message" VALUE="True"> 
        <input TYPE="Submit" NAME="Submit" Value="Enter">
        </td></tr>
        </table> 
    </form>
    
<BR /><BR />
<?=$fail_error?>
  <!-- end #mainContent --></div>
  
<!-- end #container --></div>
</body>
</html>
