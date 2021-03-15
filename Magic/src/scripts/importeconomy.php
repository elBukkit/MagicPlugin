#!/usr/bin/php
<?php

// expects a CSV file with two columns, first is an item key, second is worth

// Don't allow from Apache
if (PHP_SAPI !== 'cli') {
    die('Nope.');
}

if (count($argv) < 3) {
    die("Usage: importeconomy.php <economy.csv> <economy.yml>\n");
}

error_reporting(E_ALL);
ini_set('display_errors', 1);

$csvFile = $argv[1];
$configFile = $argv[2];

if (!file_exists($csvFile)) {
    die("File not found: $csvFile\n");
}

require_once('spyc.php');

$map = array();
if (file_exists($configFile)) {
    $map = spyc_load_file($configFile);
}

if (($handle = fopen($csvFile, "r")) !== FALSE) {
    while (($data = fgetcsv($handle, 1000, ",")) !== FALSE) {
        $num = count($data);
        if ($num < 2) {
            continue;
        }
        if (isset($map[$data[0]])) continue;
        $map[$data[0]] = array('worth' => floatval($data[1]));
    }
    fclose($handle);
} else {
    die("Failed to load file $csvFile\n");
}

ksort($map);
file_put_contents($configFile, spyc_dump($map));