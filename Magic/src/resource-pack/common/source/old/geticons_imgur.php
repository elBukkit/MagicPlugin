<?php

error_reporting(E_ALL);
ini_set('display_errors', 1);

require_once('../../../web/spyc.php');

$inputFile = dirname(__FILE__) . '/../../../main/resources/defaults/spells.defaults.yml';
//$inputFile = dirname(__FILE__) . '/../../../main/resources/examples/potter/spells.yml';
//$inputFile = dirname(__FILE__) . '/../../../main/resources/defaults/wands.defaults.yml';
$outputFolder = dirname(__FILE__) . '/imgur_skins';

if (!file_exists($inputFile))
{
    die("File not found: $inputFile\n");
}

$spells = spyc_load_file($inputFile);

foreach ($spells as $spellKey => $spell)
{
    $url = null;
    if (isset($spell['icon_url'])) {
        $url = $spell['icon_url'];
    } else if (isset($spell['icon'])) {
        $icon = $spell['icon'];
        if (strpos($icon, 'skull_item:http://') !== FALSE) {
            $url = substr($icon, 11);
        }
    }
    if (!$url) continue;
    if (strpos($url, 'http://i.imgur.com/') === FALSE) continue;
    $fileName = str_replace('http://i.imgur.com/', '', $url);
    if (file_exists($outputFolder . '/' . $fileName))
    {
        echo " skipping $spellKey, already downloaded\n";
        continue;
    }
    echo "Downloading $spellKey: $url\n";

    $source = imagecreatefrompng($url);
    $skin = imagecreatetruecolor(64, 32);
    imagealphablending($skin, false);

    $transparent = imagecolorallocatealpha($skin, 255, 255, 255, 127);
    imagefilledrectangle($skin, 0, 0, 64, 32, $transparent);
    imagealphablending($skin, true);

    imagecopyresampled($skin, $source, 0, 0, 0, 0, 32, 16, 32, 16);
    imagealphablending($skin, true);
    imagecopyresampled($skin, $source, 20, 22, 8, 8, 8, 8, 8, 8);
    imagealphablending($skin, true);
    imagecopyresampled($skin, $source, 32, 22, 8, 8, 8, 8, 8, 8);
    imagealphablending($skin, true);
    imagesavealpha($skin, true);

    imagepng($skin, $outputFolder . '/' . $fileName);
    imagedestroy($source);
    imagedestroy($skin);
}