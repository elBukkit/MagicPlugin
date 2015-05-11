<?php

error_reporting(E_ALL);
ini_set('display_errors', 1);

require_once('../../../web/spyc.php');

$inputFile = dirname(__FILE__) . '/../../../main/resources/defaults/spells.defaults.yml';
$mapFile = dirname(__FILE__) . '/imgur_map.yml';

if (!file_exists($mapFile))
{
    die("File not found: $mapFile\n");
}

if (!file_exists($inputFile))
{
    die("File not found: $inputFile\n");
}

$images = spyc_load_file($mapFile);

$config = file_get_contents($inputFile);
foreach ($images as $old => $new)
{
    $config = str_replace($old, $new, $config);
}

file_put_contents($inputFile, $config);