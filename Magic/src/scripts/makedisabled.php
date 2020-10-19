#!/usr/bin/php
<?php

// Don't allow from Apache
if (PHP_SAPI !== 'cli')
{
    die('Nope.');
}

if (count($argv) < 2) {
    die("Usage: makedisabled.php <spells folder>\n");
}



function endsWith($haystack, $needle){
    $length = strlen($needle);
    if ($length == 0) {
        return true;
    }

    return (substr($haystack, -$length) === $needle);
}


function makeDisabled($inputFile, $outputFile) {
    if (file_exists($outputFile)) return;
    echo "$inputFile to $outputFile\n";
    $source = imagecreatefrompng($inputFile);
    $transparent = imagecolorallocatealpha($source, 0, 0, 0, 64);
    $width = imagesx($source);
    $height = imagesy($source);
    imagealphablending($source, true);
    imagefilledrectangle($source, 0, 0, $width, $height, $transparent);
    imagealphablending($source, true);
    imagesavealpha($source, true);
    imagepng($source, $outputFile);
    imagedestroy($source);
}

$inputFolder = $argv[1];
$outputFolder = $inputFolder . '_disabled';
if (!file_exists($outputFolder)) {
    mkdir($outputFolder);
}
$iterator = new DirectoryIterator($inputFolder);
foreach ($iterator as $fileInfo) {
    if ($fileInfo->isDot()) continue;
    $filename = $fileInfo->getFilename();
    if (!endsWith($filename, '.png')) continue;
    $outputFile = $outputFolder . '/' . $filename;
    $inputFile = $fileInfo->getPathname();
    makeDisabled($inputFile, $outputFile);
}

