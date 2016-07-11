<?php

error_reporting(E_ALL);
ini_set('display_errors', 1);

require_once('../../../web/spyc.php');

$inputFile = dirname(__FILE__) . '/../../../main/resources/defaults/spells.defaults.yml';
$outputFolder = dirname(__FILE__) . '/icon_skins';

if (!file_exists($inputFile))
{
    die("File not found: $inputFile\n");
}

$spells = spyc_load_file($inputFile);

foreach ($spells as $spellKey => $spell)
{
    if (!isset($spell['icon_url'])) continue;
    $url = $spell['icon_url'];
    if (strpos($url, 'textures.minecraft.net') !== FALSE) continue;
    if (file_exists($outputFolder . '/' . $spellKey . '.png'))
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

    imagepng($skin, $outputFolder . '/' . $spellKey . '.png');
    imagedestroy($source);
    imagedestroy($skin);
}