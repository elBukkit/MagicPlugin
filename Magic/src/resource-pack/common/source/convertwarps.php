#!/usr/bin/php
<?php

// Don't allow from Apache
if (PHP_SAPI !== 'cli')
{
    die('Nope.');
}

if (count($argv) < 2) {
    die("Usage: splitconfig.php <warps.csv>\n");
}

error_reporting(E_ALL);
ini_set('display_errors', 1);

$warpCSV =  dirname(__FILE__) . '/' . $argv[1];

if (!file_exists($warpCSV))
{
    die("File not found: $warpCSV\n");
}

$targetFolder = dirname(__FILE__) . '/warps';
if (!file_exists($targetFolder)) mkdir($targetFolder);

$handle = fopen($warpCSV, "r");
if ($handle) {
    while (($line = fgets($handle)) !== false) {
        $trimmedLine = trim($line);
        $pieces = explode(',', $line);
        $warpName = str_replace('"', '', $pieces[0]);
        $warpWorld = str_replace('"', '', $pieces[1]);
        $warpX = str_replace('"', '', $pieces[3]);
        $warpY = str_replace('"', '', $pieces[4]);
        $warpZ = str_replace('"', '', $pieces[5]);
        $warpPitch = str_replace('"', '', $pieces[6]);
        $warpYaw = str_replace('"', '', $pieces[7]);
        $warpYaw = str_replace("\n", '', $warpYaw);

        $warpConfig = "world: " . $warpWorld . "\n";
        $warpConfig .= "x: " . $warpX . "\n";
        $warpConfig .= "y: " . $warpY . "\n";
        $warpConfig .= "z: " . $warpZ. "\n";
        $warpConfig .= "yaw: " . $warpYaw . "\n";
        $warpConfig .= "pitch: " . $warpPitch . "\n";
        $warpConfig .= "name: " . $warpName . "\n";

        echo "Writing warps/$warpName.yml\n";
        file_put_contents($targetFolder . '/' . $warpName . '.yml', $warpConfig);
    }
    fclose($handle);
} else {
    die("Error opening file: $warpCSV\n");
}

echo "Wrote warps to $targetFolder\n";
