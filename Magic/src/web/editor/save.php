<?php
require_once('../config.inc.php');
if (!$sandboxServer) {
    die('No sandbox server defined');
}

if (!isset($_REQUEST['spells'])) {
    die(json_encode(array('success' => false, 'message' => 'Missing spells parameter')));
}

$spells = $_REQUEST['spells'];
file_put_contents("$sandboxServer/plugins/Magic/spells.yml", $spells);
touch("$sandboxServer/plugins/Magic/data/updated.yml");

echo json_encode(array('success' => true, 'message' => 'Saved'));