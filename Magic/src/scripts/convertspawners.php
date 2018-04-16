#!/usr/bin/php
<?php

// Don't allow from Apache
if (PHP_SAPI !== 'cli') {
    die('Nope.');
}
require_once('spyc.php');

if (count($argv) < 3) {
    die("Usage: convertspawners.php <infolder> <outfolder>\n");
}

error_reporting(E_ALL);
ini_set('display_errors', 1);

$inFolder = $argv[1];
if (!file_exists($inFolder)) {
    die("Folder not found: $inFolder\n");
}

$outFolder = $argv[2];
if (!file_exists($outFolder)) {
    mkdir($outFolder);
}

$dir = new DirectoryIterator($inFolder);

function endsWith($haystack, $needle) {
    $length = strlen($needle);
    return $length === 0 || (substr($haystack, -$length) === $needle);
}

foreach ($dir as $fileinfo)  {
    $filename = $fileinfo->getFilename();
    if ($fileinfo->isDot() || !endsWith($filename, '.yml')) continue;

    echo "Converting: $filename\n";
    $spawners = spyc_load_file($inFolder . '/' . $filename);
    $newfile = array();
    foreach ($spawners as $key => $spawner) {
        if (!isset($spawner['item'])
            || !isset($spawner['item']['tags'])
            || !isset($spawner['item']['tags']['BlockEntityTag'])
            || !isset($spawner['item']['tags']['BlockEntityTag']['SpawnData'])
            || !isset($spawner['item']['tags']['BlockEntityTag']['SpawnData']['CustomName'])
            ) continue;

        $automaton = array();
        $item = $spawner['item'];
        if (isset($item['tags']['display']) && isset($item['tags']['display']['Name'])) {
            $automaton['name'] = $item['tags']['display']['Name'];
        }
        $entity = $spawner['item']['tags']['BlockEntityTag'];
        $automaton['spawn'] = array(
            'count' => $entity['SpawnCount'],
            'limit' => $entity['MaxNearbyEntities'],
            'radius' => $entity['SpawnRange'],
            'interval' => $entity['Delay'] * 1000 / 20,
            'player_range' => $entity['RequiredPlayerRange'],
            'limit_range' => $entity['RequiredPlayerRange'],
            'mobs' => $entity['SpawnData']['CustomName']
        );

        $newfile = array($key => $automaton);
    }

    file_put_contents($outFolder . "/" . $filename, spyc_dump($newfile));
}

echo "Done.\n";
