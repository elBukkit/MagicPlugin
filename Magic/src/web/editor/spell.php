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
$spellFolder = "$sandboxServer/plugins/Magic/spells";
$spellFile = file_get_contents($spellFolder . '/' . $_REQUEST['key'] . '.yml');

$spellFile = yaml_parse($spellFile);

foreach ($spellFile as $key => $spell) {
    unset($spell['creator_id']);
    unset($spell['creator_name']);
    $spellFile[$key] = $spell;
}
$spellFile = yaml_emit_clean($spellFile);

die(json_encode(array('success' => true, 'yml' => $spellFile)));
