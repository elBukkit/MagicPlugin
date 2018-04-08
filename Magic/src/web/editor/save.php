<?php
require_once('../config.inc.php');
require_once('user.inc.php');
if (!$sandboxServer) die('No sandbox server defined');

$user = getUser();
if (!$user['id']) {
    die(json_encode(array('success' => false, 'message' => 'Not logged in')));
}

if (!isset($_REQUEST['spells'])) {
    die(json_encode(array('success' => false, 'message' => 'Missing spells parameter')));
}

$spells = $_REQUEST['spells'];
file_put_contents("$sandboxServer/plugins/Magic/spells.yml", $spells);

$updated = 'user_id: ' . $user['id'];
file_put_contents("$sandboxServer/plugins/Magic/data/updated.yml", $updated);

echo json_encode(array('success' => true, 'message' => 'Saved'));