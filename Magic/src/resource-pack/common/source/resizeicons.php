#!/usr/bin/php
<?php

// Don't allow from Apache
if (PHP_SAPI !== 'cli')
{
    die('Nope.');
}

if (count($argv) < 2) {
    die("Usage: rpicons.php <folder>\n");
}

error_reporting(E_ALL);
ini_set('display_errors', 1);

$iconFolder = $argv[1];

if (!file_exists($iconFolder))
{
    die("Folder not found: $iconFolder\n");
}

$targetFolder = dirname(__FILE__) . '/target';
if (!file_exists($targetFolder)) mkdir($targetFolder);

$sourceFiles = scandir($iconFolder);
foreach ($sourceFiles as $sourceFile) {
    if (strpos($sourceFile, '.png') === FALSE) continue;

    $sourceImage = imagecreatefrompng($iconFolder . '/' . $sourceFile);
    $sourceWidth = imagesx($sourceImage);
    $sourceHeight = imagesy($sourceImage);
    $icon = imagecreatetruecolor(32, 32);
    imagecopyresampled($icon, $sourceImage, 0, 0, 0, 0, 32, 32, $sourceWidth, $sourceHeight);
    imagepng($icon, $targetFolder . '/' . $sourceFile);
    imagedestroy($sourceImage);
    imagedestroy($icon);
    
    echo "Resized $sourceFile\n";
}
