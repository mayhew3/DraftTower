<?php

if (IsDebug()) { ?>
  <span style="color:#F00;font-weight:bold">DEBUG MODE</span><br />
<br />

<? }

 if (isFullUser()) {
?>
Welcome, <?=$_SESSION['FirstName']?>!<br />
<a href="logout.php">Sign out</a><br />
<? } else { ?>
<a href="login.php">Sign in</a><br />
<? } ?>
<br />

<a href="index.php">Main</a><BR>

<br />
