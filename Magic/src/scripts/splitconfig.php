#!/usr/bin/php
<?php

// Don't allow from Apache
if (PHP_SAPI !== 'cli')
{
    die('Nope.');
}

if (count($argv) < 2) {
    die("Usage: splitconfig.php <spells.yml>\n");
}

error_reporting(E_ALL);
ini_set('display_errors', 1);

$spellConfig = $argv[1];

if (!file_exists($spellConfig))
{
    die("File not found: $spellConfig\n");
}

$targetFolder = dirname($argv[1]) . '/' . str_replace('.yml', '', $spellConfig);
if (!file_exists($targetFolder)) mkdir($targetFolder);

$handle = fopen($spellConfig, "r");
if ($handle) {
    $currentSpell = null;
    $currentSpellConfig = '';
    $currentComments = '';
    while (($line = fgets($handle)) !== false) {
        $trimmedLine = trim($line);
        $isEmpty = strlen($trimmedLine) == 0;
        $isComment = substr($line, 0, 1) == '#';
        if ($isComment) {
            $currentComments .= $line;
            continue;
        } else if (!$isEmpty && substr($line, 0, 1) != ' ' && substr($line, 0, 1) != '-') {
            $newSpell = substr(trim($line), 0, -1);
            $pieces = explode('|', $newSpell);
            $newSpell = $pieces[0];
            $transitioning = false;
            if ($newSpell != $currentSpell) {
                if ($currentSpell != null) {
                    echo "Splitting off $currentSpell\n";
                    file_put_contents($targetFolder . '/' . $currentSpell . '.yml', $currentSpellConfig);
                    $currentSpellConfig = $currentComments;
                    $currentComments = '';
                } else {
                    $currentSpellConfig = $currentComments . $currentSpellConfig;
                    $currentComments = '';
                }
                $currentSpell = $newSpell;
            } else {
                $currentSpellConfig = $currentComments . $currentSpellConfig;
                $currentComments = '';
            }
        }
        $currentSpellConfig .= $line;
    }
    if ($currentSpell != null) {
        echo "Splitting off $currentSpell\n";
        file_put_contents($targetFolder . '/' . $currentSpell . '.yml', $currentComments . $currentSpellConfig);
    }
    fclose($handle);
} else {
    die("Error opening file: $spellConfig\n");
}

echo "Wrote split configs to $targetFolder\n";
