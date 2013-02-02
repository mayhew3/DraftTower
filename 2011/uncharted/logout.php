<?php
  require_once "databases.php";
   
   unset($_SESSION['UserID']);
   unset($_SESSION['Role']);
   unset($_SESSION['FirstName']);

  // now that the user is logged out,
  // go to login page
  header('Location: login.php');
?>

