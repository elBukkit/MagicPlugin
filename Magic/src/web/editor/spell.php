<?php
header('Content-Type: application/json');
require_once('../config.inc.php');
require_once('yaml.inc.php');

if (!$sandboxServer) {
    die(json_encode(array('success' => false, 'message' => 'No sandbox server defined')));
}
if (!isset($_REQUEST['key'])) {
    die(json_encode(array('success' => false, 'message' => 'Missing key parameter')));
}

$key = $_REQUEST['key'];
if (strpos($key, 'default.') === 0) {
    $key = substr($key, 8);
    $defaultsFolder = "$magicRootFolder/defaults/spells";
    $spellFile = file_get_contents($defaultsFolder . '/' . $key . '.yml');
} else {
    $spellFolder = "$sandboxServer/plugins/Magic/spells";
    $spellFile = file_get_contents($spellFolder . '/' . $key . '.yml');
}

$spellFile = yaml_parse($spellFile);

foreach ($spellFile as $key => $spell) {
    unset($spell['creator_id']);
    unset($spell['creator_name']);
    $spellFile[$key] = $spell;
}
$spellFile = yaml_emit_clean($spellFile);

die(json_encode(array('success' => true, 'yml' => $spellFile)));
