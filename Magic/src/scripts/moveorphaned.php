#!/usr/bin/php
<?php

// Don't allow from Apache
if (PHP_SAPI !== 'cli')
{
    die('Nope.');
}

if (count($argv) < 3) {
    echo "Specify path to rp root folder\n";
    die("Usage: moveorphaned.php <from> <to>\n");
}

function endsWith($haystack, $needle){
    $length = strlen($needle);
    if ($length == 0) {
        return true;
    }
    return (substr($haystack, -$length) === $needle);
}


function startsWith ($string, $startString){
    $len = strlen($startString);
    return (substr($string, 0, $len) === $startString);
}

$fromFolder = $argv[1];
$toFolder = $argv[2];

// First collect all textures, we're only looking at "custom" folders
$customTextureFolder = $fromFolder . '/assets/minecraft/textures';

function collectTextures($folder, $path = '') {
    $dir = new DirectoryIterator($folder . $path);
    $textures = array();
    foreach ($dir as $fileinfo) {
        $filename = $fileinfo->getFilename();
        if ($fileinfo->isDot()) continue;
        if ($fileinfo->isDir()) {
            $folderTextures = collectTextures($folder, $path . '/' . $filename);
            $textures = array_merge($textures, $folderTextures);
            continue;
        }
        if (!endsWith($filename, '.png')) continue;
        $fullPath = substr($path . '/' . $filename, 1);
        $textures[$fullPath] = true;
    }
    return $textures;
}

$textures = collectTextures($customTextureFolder);

// Iterate over all models
$customModelFolder = $fromFolder . '/assets/minecraft/models';
function collectModelTextures($folder){
    global $textures;
    $dir = new DirectoryIterator($folder);
    $used = array();
    foreach ($dir as $fileinfo) {
        $filename = $fileinfo->getFilename();
        if ($fileinfo->isDot()) continue;
        if ($fileinfo->isDir()) {
            $folderTextures = collectModelTextures($folder . '/' . $filename);
            $used = array_merge($used, $folderTextures);
            continue;
        }
        if (!endsWith($filename, '.json')) continue;
        $model = json_decode(file_get_contents($fileinfo->getPathname()), true);
        if (isset($model['textures'])) {
            $modelTextures = $model['textures'];
            foreach ($modelTextures as $key => $texture) {
                $texture = $texture . '.png';
                if (!isset($textures[$texture]) && !startsWith($texture, 'minecraft:')) {
                    echo "Custom model $filename uses missing texture: $texture\n";
                    continue;
                }
                $used[$texture] = true;
            }
        }
    }
    return $used;
}
$used = collectModelTextures($customModelFolder);

$relocate = array_diff_key($textures, $used);
if (count($relocate) > 0) {
    echo "Relocating " . count($relocate) . " textures\n";
} else {
    echo "Nothing to relocate!\n";
}

$toTextureFolder = $toFolder . '/assets/minecraft/textures/';
$fromTextureFolder = $fromFolder . '/assets/minecraft/textures/';
foreach ($relocate as $filename => $nothing) {
    $fromFilename = $fromTextureFolder . $filename;
    $toFilename = $toTextureFolder . $filename;
    rename($fromFilename, $toFilename);
    echo "Moved $fromFilename to $toFilename\n";
}


