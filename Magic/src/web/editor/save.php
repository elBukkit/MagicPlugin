<?php
header('Content-Type: application/json');
require_once('../config.inc.php');
require_once('user.inc.php');
require_once('yaml.inc.php');
if (!$sandboxServer) {
    die(json_encode(array('success' => false, 'message' => 'No sandbox server defined')));
}

$user = getUser();
if (!$user['id']) {
    die(json_encode(array('success' => false, 'message' => 'Not logged in')));
}

if (!isset($_REQUEST['spell'])) {
    die(json_encode(array('success' => false, 'message' => 'Missing spell parameter')));
}

$spell = $_REQUEST['spell'];
$spell = yaml_parse($spell);
if (!$spell) {
    die(json_encode(array('success' => false, 'message' => 'Invalid spell')));
}

if (count($spell) != 1) {
    die(json_encode(array('success' => false, 'message' => 'Currently only one spell per file is supported')));
}

$key = array_keys($spell)[0];
$spellFile = "$sandboxServer/plugins/Magic/spells/$key.yml";
if (file_exists($spellFile)) {
    $existing = file_get_contents($spellFile);
    $existing = yaml_parse($existing);

    if (count($existing) != 1 || !isset($existing[$key])
        || !isset($existing[$key]['creator_id']) || $existing[$key]['creator_id'] != $user['id']) {
        die(json_encode(array('success' => false, 'message' => 'Spell exists and you are not the original creator, please try another key name')));
    }
}

$spell['owner_id'] = $user['id'];
$spell['owner_name'] = $user['name'];

$spell = yaml_emit_clean($spell);

file_put_contents($spellFile, $spell);

$updated = 'user_id: ' . $user['id'];
file_put_contents("$sandboxServer/plugins/Magic/data/updated.yml", $updated);

echo json_encode(array('success' => true, 'message' => 'Saved'));