#!/usr/bin/php
<?php

// Don't allow from Apache
if (PHP_SAPI !== 'cli')
{
    die('Nope.');
}

if (count($argv) < 2) {
    die("Usage: rpicons.php <config.yml> <start>\n");
}

error_reporting(E_ALL);
ini_set('display_errors', 1);

$spellConfig =  dirname(__FILE__) . '/' . $argv[1];
$currentDurability = 1;
if (count($argv) > 2) {
    $currentDurability = $argv[2];
}

if (!file_exists($spellConfig))
{
    die("File not found: $spellConfig\n");
}

$iconType = 'diamond_axe';
// IMG_NEAREST_NEIGHBOUR, IMG_BILINEAR_FIXED, IMG_BICUBIC, IMG_BICUBIC_FIXED
$interpolationType = IMG_NEAREST_NEIGHBOUR;
$maxDurability = 1562;
$targetFolder = dirname(__FILE__) . '/target';
if (!file_exists($targetFolder)) mkdir($targetFolder);
$spellConfigOut = $targetFolder . '/spells.yml';

$modelsFolder = $targetFolder . '/models';
if (!file_exists($modelsFolder)) mkdir($modelsFolder);

$texturesFolder = $targetFolder . '/textures';
if (!file_exists($texturesFolder)) mkdir($texturesFolder);

$iconJson = $modelsFolder . "/$iconType.json";
$spellsFolder = $modelsFolder . '/spells';
if (!file_exists($spellsFolder)) mkdir($spellsFolder);
$spellsTextureFolder = $texturesFolder . '/spells';
if (!file_exists($spellsTextureFolder)) mkdir($spellsTextureFolder);
$cacheFolder = $targetFolder . '/cache';
if (!file_exists($cacheFolder)) mkdir($cacheFolder);

$iconMap = array();
$outputFile = "";
$handle = fopen($spellConfig, "r");
if ($handle) {
    $currentSpell = null;
    $currentIcon = null;
    $currentURL = null;
    $currentSpellConfigNoIcon = '';
    $currentSpellConfig = '';
    $iconIndent = '';
    while (($line = fgets($handle)) !== false) {
        if (substr($line, 0, 1) != ' ') {
            if ($currentSpell != null) {
                echo "Skipping $currentSpell, has no icon\n";
            }
            $outputFile .= $currentSpellConfig;
            $currentSpell = null;
            if (substr($line, 0, 1) != '#') {
                $currentSpell = substr(trim($line), 0, -1);
            }
            $currentSpellConfig = '';
            $currentSpellConfigNoIcon = '';
            $currentIcon = null;
            $currentURL = null;
            $outputFile .= $line;
            continue;
        }   
        if (!$currentSpell) {
            $outputFile .= $line;
            continue;
        }

        $currentSpellConfig .= $line;
        
        $trimmed = trim($line);
        if (substr($trimmed, 0, 9) === 'icon_url:') {
            $currentURL = trim(substr($trimmed, 9));
            if ($currentIcon != null) {
                echo "Converting $currentSpell\n";
                $outputFile .= getIcon($currentSpell, $currentURL, $iconIndent);
                $outputFile .= $line . $currentSpellConfigNoIcon;
                $currentSpell = null;
                $currentSpellConfig = '';
            }
        }
        if (substr($trimmed, 0, 5) === 'icon:') {
            $currentIcon = trim(substr($trimmed, 5));
            $spaces = strspn($line, ' ');
            $iconIndent = str_repeat(' ', $spaces);
            if (strpos($currentIcon, 'diamond_axe') !== FALSE) {
                $parts = explode(':', $currentIcon);
                if (count($parts) == 2) {
                    $durability = $parts[1];
                    echo "Skipping $currentSpell, already converted as #$durability, new id#$currentDurability\n";
                    $iconMap[$currentDurability] = $currentSpell;
                    $currentSpell = null;
                    $outputFile .= $iconIndent . "icon: $iconType:$currentDurability\n";
                    $currentSpellConfig = $currentSpellConfigNoIcon;
                    $currentDurability++;
                    continue;
                }
            }
            if (strpos($currentIcon, ':http') !== FALSE) {
                $pieces = explode(':', $currentIcon, 2);
                $currentURL = $pieces[1];
            }
            if ($currentURL != null) {
                echo "Converting $currentSpell\n";
                $outputFile .= getIcon($currentSpell, $currentURL, $iconIndent);
                $outputFile .= $currentSpellConfigNoIcon;
                $currentSpellConfig = '';
                $currentSpell = null;
            }
            continue;
        }

        $currentSpellConfigNoIcon .= $line;
    }
    $outputFile .= $currentSpellConfig;
    fclose($handle);
} else {
    die("Error opening file: $spellConfig\n");
}

file_put_contents($spellConfigOut, $outputFile);
echo "Wrote config to $spellConfigOut\n";
$outputFile = null;
$jsonFile = <<<END
{
  "parent": "item/handheld",
  "textures": {
    "layer0": "items/diamond_axe"
  },
  "overrides": [\n
END;
foreach ($iconMap as $durability => $spell) {
    if ($durability >= $maxDurability) {
        echo "ERROR: Exceeded max durability of $maxDurability\n";
        break;
    }
    $damage = $durability / $maxDurability;
    $jsonFile .= <<<LINE
    {"predicate": {"damaged": 0, "damage": $damage}, "model": "item/spells/$spell"},\n
LINE;
}
$jsonFile .= <<<END
    {"predicate": {"damaged": 1, "damage": 0}, "model": "item/diamond_axe"}
  ]
}
END;
file_put_contents($iconJson, $jsonFile);
echo "Wrote icon config to $iconJson\n";

function getIcon($spell, $url, $iconIndent) {
    global $cacheFolder;
    global $iconMap;
    global $iconType;
    global $spellsTextureFolder;
    global $spellsFolder;
    global $interpolationType;
    global $currentDurability;
    $cacheFile = $cacheFolder . '/' . urlencode($url);
    if (!file_exists($cacheFile)) {
        $iconFile = file_get_contents($url);
        file_put_contents($cacheFile, $iconFile);
    }
    $sourceImage = imagecreatefrompng($cacheFile);
    $icon = imagecreatetruecolor(8, 8);
    imagecopyresampled($icon, $sourceImage, 0, 0, 8, 8, 8, 8, 8, 8);
    $icon = imagescale($icon, 32, 32, $interpolationType);
    
    imagepng($icon, $spellsTextureFolder . '/' . $spell . '.png');
    imagedestroy($sourceImage);
    imagedestroy($icon);
    
    $spellJson = <<<END
{
    "parent" : "item/custom/icon",
    "textures": {
        "particle": "items/spells/$spell",
        "texture": "items/spells/$spell"
    }
}
END;
    file_put_contents($spellsFolder . '/' . $spell . '.json', $spellJson);

    $durability = $currentDurability + 1;
    $currentDurability++;
    $iconMap[$durability] = $spell;
    return $iconIndent . "icon: $iconType:$durability\n";
}