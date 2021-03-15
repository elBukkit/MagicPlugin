#!/usr/bin/php
<?php

// Don't allow from Apache
if (PHP_SAPI !== 'cli') {
    die('Nope.');
}

if (count($argv) < 2) {
    die("Usage: sortconfig.php <file.yml>\n");
}

error_reporting(E_ALL);
ini_set('display_errors', 1);

$configFile = $argv[1];

if (!file_exists($configFile)) {
    die("File not found: $configFile\n");
}

require_once('spyc.php');

$map = spyc_load_file($configFile);
ksort($map);
file_put_contents($configFile, spyc_dump($map));