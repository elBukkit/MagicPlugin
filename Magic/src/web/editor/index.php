<?php
require_once('../config.inc.php');
if (!$sandboxServer) die('No sandbox server defined');

$userId = isset($_COOKIE['user_id']) ? $_COOKIE['user_id'] : '';
$userCode = isset($_COOKIE['user_code']) ? $_COOKIE['user_code'] : '';

$registeredFile = "$sandboxServer/plugins/Magic/data/registered.yml";
if (!$userId || !$userCode || !file_exists($registeredFile)) {
    include 'register.php';
    die();
}

require_once('common/spyc.php');
$registered = $config = spyc_load_file($registeredFile);
if (!isset($registered[$userId]) || $registered[$userId]['code'] !== $userCode) {
    include 'register.php';
    die();
}

?>

<html>
<head>
    <title><?= $title ?> Editor</title>
    <link rel="shortcut icon" type="image/x-icon" href="favicon.ico">
    <link rel="stylesheet" href="common/css/smoothness/jquery-ui-1.10.3.custom.min.css"/>
    <link rel="stylesheet" href="common/css/common.css" />
    <link rel="stylesheet" href="common/css/loading.css" />
    <link rel="stylesheet" href="css/editor.css"/>
    <script src="common/js/jquery-1.10.2.min.js"></script>
    <script src="common/js/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="js/editor.js"></script>
    <?php if ($analytics) echo $analytics; ?>
</head>
<body>
<div id="container">
<div id="header">
    <button type="button" id="saveButton">Save</button>
</div>
<textarea id="editor">
<?php echo file_get_contents("$sandboxServer/plugins/Magic/spells.yml") ?>
</textarea>
</div>
</body>
</html>
