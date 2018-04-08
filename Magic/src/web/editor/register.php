<?php
require_once('../config.inc.php');
?>

<html>
<head>
    <title><?= $title ?> Registration</title>
    <link rel="shortcut icon" type="image/x-icon" href="favicon.ico">
    <link rel="stylesheet" href="common/css/smoothness/jquery-ui-1.10.3.custom.min.css"/>
    <link rel="stylesheet" href="common/css/common.css" />
    <link rel="stylesheet" href="common/css/loading.css" />
    <link rel="stylesheet" href="css/register.css"/>
    <script src="common/js/jquery-1.10.2.min.js"></script>
    <script src="common/js/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="js/register.js"></script>
    <?php if ($analytics) echo $analytics; ?>
</head>
<body>
<div id="registration">
    <div id="registrationTitle">Please log into the <span class="server"><?= $sandboxServerURL ?></span>s server and register</div>
    <div>
        <label for="userId">In-Game Name:</label><input type="text" id="userId">
    </div>
    <div>
        <button type="button" id="getCodeButton">Get Code</button>
    </div>

    <div id="codeDialog" title="Enter Code" style="display:none">
      <div style="margin-bottom: 0.5em">
        <span class="ui-icon ui-icon-circle-check" style="float:left; margin:0 7px 50px 0;"></span>
        <span>Please enter the following code in-game using</span>
      </div>
      <div class="code">
        /magic register <span id="codeDiv"></span>
      </div>
    </div>
</div>
</body>
</html>
