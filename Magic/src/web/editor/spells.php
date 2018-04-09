<?php
header('Content-Type: application/json');
require_once('../config.inc.php');
if (!$sandboxServer) {
    die(json_encode(array('success' => false, 'message' => 'No sandbox server defined')));
}

function endsWith($haystack, $needle)
{
    $length = strlen($needle);
    return $length === 0 || (substr($haystack, -$length) === $needle);
}

$spells = array();
$spellFolder = "$sandboxServer/plugins/Magic/spells";
$spellFiles = scandir($spellFolder);
foreach ($spellFiles as $spellFile) {
    if (!endsWith($spellFile, '.yml')) continue;

    $spellConfig = yaml_parse_file($spellFolder . '/' . $spellFile);
    $spellKeys = array_keys($spellConfig);

    // TODO: Spell levels
    if (count($spellKeys) != 1) continue;

    $spellKey = $spellKeys[0];
    if ($spellFile != $spellKey . '.yml') continue;

    $spellConfig = $spellConfig[$spellKey];
    $creatorId = isset($spellConfig['creator_id']) ? $spellConfig['creator_id'] : '';
    $creatorName = isset($spellConfig['creator_name']) ? $spellConfig['creator_name'] : '';
    $spellName = isset($spellConfig['name']) ? $spellConfig['name'] : '';
    $spellDescription = isset($spellConfig['description']) ? $spellConfig['description'] : '';

    $spell = array(
        'key' => $spellKey,
        'creator_id' => $creatorId,
        'creator_name' => $creatorName,
        'name' => $spellName,
        'description' => $spellDescription
    );
    array_push($spells, $spell);
}

$defaultMessages = yaml_parse_file("$magicRootFolder/defaults/messages/spells.yml");
$defaultMessages = $defaultMessages['spells'];

$defaultsFolder = "$magicRootFolder/defaults/spells";
$defaultFiles = scandir($defaultsFolder);
foreach ($defaultFiles as $spellFile) {
    if (!endsWith($spellFile, '.yml')) continue;

    $spellConfig = yaml_parse_file($defaultsFolder . '/' . $spellFile);
    $spellKeys = array_keys($spellConfig);
    if (count($spellKeys) == 0) continue;
    $spellKey = $spellKeys[0];

    $spellConfig = $spellConfig[$spellKey];
    if ($spellConfig['hidden']) continue;

    $creatorId = isset($spellConfig['creator_id']) ? $spellConfig['creator_id'] : '';
    $creatorName = isset($spellConfig['creator_name']) ? $spellConfig['creator_name'] : '';
    $spellName = isset($spellConfig['name']) ? $spellConfig['name'] : '';
    $spellDescription = isset($spellConfig['description']) ? $spellConfig['description'] : '';

    if (!$spellName && isset($defaultMessages[$spellKey]) && isset($defaultMessages[$spellKey]['name'])) {
        $spellName = $defaultMessages[$spellKey]['name'];
    }
    if (!$spellDescription && isset($defaultMessages[$spellKey]) && isset($defaultMessages[$spellKey]['description'])) {
        $spellDescription = $defaultMessages[$spellKey]['description'];
    }

    $spell = array(
        'key' => 'default.' . $spellKey,
        'creator_id' => $creatorId,
        'creator_name' => $creatorName,
        'name' => $spellName,
        'description' => $spellDescription
    );
    array_push($spells, $spell);
}
die(json_encode(array('success' => true, 'spells' => $spells)));
