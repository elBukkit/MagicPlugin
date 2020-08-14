#!/usr/bin/php
<?php

// Don't allow from Apache
if (PHP_SAPI !== 'cli')
{
    die('Nope.');
}

if (count($argv) < 2) {
    die("Usage: updateskulls.php <folder>\n");
}

$folder = $argv[1];
if (!file_exists($folder)) {
    die("Folder does not exist: $folder\n");
}

require_once('spyc.php');

function endsWith($haystack, $needle) {
    $length = strlen($needle);
    return $length === 0 || (substr($haystack, -$length) === $needle);
}

function startsWith ($string, $startString){
    $len = strlen($startString);
    return (substr($string, 0, $len) === $startString);
}

$dir = new DirectoryIterator($folder);


function getCurrentSkin($username) {
    $uuidFetcher = curl_init();
    curl_setopt($uuidFetcher, CURLOPT_FRESH_CONNECT, TRUE);
    $skinURL = 'https://api.mojang.com/users/profiles/minecraft/' . $username;
    curl_setopt($uuidFetcher, CURLOPT_URL, $skinURL);
    $headers = array(
        "Cache-Control: no-cache",
    );
    curl_setopt($uuidFetcher, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($uuidFetcher, CURLOPT_RETURNTRANSFER, true);
    $result = curl_exec($uuidFetcher);
    if (!$result) {
        echo "Failed to retrieve uuid, retrying in 2 minutes";
        sleep(2 * 60);
        $result = curl_exec($uuidFetcher);
        if (!$result)
        {
            echo("Failed to retrieve uuid for $username\n");
            return null;
        }
    }

    $result = json_decode($result, true);
    $uuid = $result['id'];

    $skinChecker = curl_init();
    curl_setopt($skinChecker, CURLOPT_FRESH_CONNECT, TRUE);
    $skinURL = 'https://sessionserver.mojang.com/session/minecraft/profile/' . $uuid;
    curl_setopt($skinChecker, CURLOPT_URL, $skinURL);
    $headers = array(
        "Cache-Control: no-cache",
    );
    curl_setopt($skinChecker, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($skinChecker, CURLOPT_RETURNTRANSFER, true);
    $result = curl_exec($skinChecker);
    if (!$result) {
        echo "Failed to retrieve skin, retrying in 2 minutes";
        sleep(2 * 60);
        $result = curl_exec($skinChecker);
        if (!$result)
        {
            echo("Failed to retrieve profile $skinURL\n");
            return null;
        }
    }
    $result = json_decode($result, true);
    if (!isset($result['properties'])) {
        var_dump($result);
        echo("Profile has no properties\n");
        return null;
    }
    $properties = $result['properties'];
    $textureURL = null;
    foreach ($properties as $property) {
        if ($property['name'] != 'textures') continue;
        $texture = json_decode(base64_decode($property['value']), true);
        if (isset($texture['textures'])) {
            $textures = $texture['textures'];
            if (isset($textures['SKIN'])) {
                $textureURL = $textures['SKIN']['url'];
                break;
            }
        }
    }
    if (!$textureURL) {
        echo("Failed to find profile texture\n");
        return null;
    }
    return $textureURL;
}

foreach ($dir as $fileinfo)  {
    $filename = $fileinfo->getFilename();
    if ($fileinfo->isDot() || !endsWith($filename, '.yml') || !startsWith($filename, "skull_")) continue;

    $skull = spyc_load_file($folder . '/' . $filename);
    foreach ($skull as $key => $value) break;
    $item = $value['item'];
    if (!$item || !endsWith($item, '.png')) {
        echo "   Skipping $filename\n";
        continue;
    }

    $name = explode('/', $item);
    $name = $name[count($name) - 1];
    $name = explode('.', $name)[0];

    echo " Skull $filename : $name\n";

    $url = getCurrentSkin($name);
    sleep(5);
    if (!$url) {
        echo "   failed :(\n";
        continue;
    }
    $value['item']= 'player_head:' . $url;
    $skull[$key] = $value;

    file_put_contents($folder . "/" . $filename, spyc_dump($skull));
    echo "Updated!\n";
}