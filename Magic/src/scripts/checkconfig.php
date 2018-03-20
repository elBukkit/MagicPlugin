#!/usr/bin/php
<?php

// Don't allow from Apache
if (PHP_SAPI !== 'cli')
{
    die('Nope.');
}

if (count($argv) < 2) {
    die("Usage: checkconfig.php <spells.yml>\n");
}

error_reporting(E_ALL);
ini_set('display_errors', 1);

$spellConfig =  dirname(__FILE__) . '/' . $argv[1];

if (!file_exists($spellConfig))
{
    die("File not found: $spellConfig\n");
}

$handle = fopen($spellConfig, "r");
if ($handle) {
    $keys = array();
    while (($line = fgets($handle)) !== false) {
        $trimmedLine = trim($line);
        $isEmpty = strlen($trimmedLine) == 0;
        $isComment = substr($line, 0, 1) == '#';
        if (!$isComment && !$isEmpty && substr($line, 0, 1) != ' ' && substr($line, 0, 1) != '-') {
            $newKey = substr(trim($line), 0, -1);
            if (isset($keys[$newKey])) {
                echo "Duplicate entry: $newKey\n";
            } else {
                $keys[$newKey] = true;
            }
        }
    }
    fclose($handle);
} else {
    die("Error opening file: $spellConfig\n");
}

echo "Done.\n";
