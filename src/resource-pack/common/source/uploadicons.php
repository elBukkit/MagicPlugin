<?php

error_reporting(E_ALL);
ini_set('display_errors', 1);

require_once('../../../web/spyc.php');

// Replace this with a file that looks like this:
/*
<?php
define('USER_UUID', '<your player's uuid>');
define('AUTH_TOKEN', '<find this in a hidden form element on profile upload page>');
define('USER_COOKIE', '<grab cookies from network inspector on profile upload page>');
?>
 */
require_once('/Users/nathan/mc_creds.php');

$mapFile = dirname(__FILE__) . '/imgur_map.yml';
$inputFolder = dirname(__FILE__) . '/imgur_skins';

if (!file_exists($mapFile))
{
    die("File not found: $mapFile\n");
}

function getCurrentSkin()
{
    $skinChecker = curl_init();
    curl_setopt($skinChecker, CURLOPT_FRESH_CONNECT, TRUE);
    $skinURL = 'https://sessionserver.mojang.com/session/minecraft/profile/' . str_replace('-', '', USER_UUID);
    curl_setopt($skinChecker, CURLOPT_URL, $skinURL);
    $headers = array(
        "Cache-Control: no-cache",
    );
    curl_setopt($skinChecker, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($skinChecker, CURLOPT_RETURNTRANSFER, true);
    $result = curl_exec($skinChecker);
    if (!$result) {
        die("Failed to retrieve profile $skinURL\n");
    }
    $result = json_decode($result, true);
    if (!isset($result['properties'])) {
        var_dump($result);
        die("Profile has no properties\n");
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
        die("Failed to find profile texture\n");
    }
    return $textureURL;
}

$images = spyc_load_file($mapFile);

$dir = new DirectoryIterator($inputFolder);
foreach ($dir as $fileinfo) {
    $inputImage = $fileinfo->getFilename();
    if ($fileinfo->isDot() || startsWith($inputImage, '.')) continue;
    $url = "http://i.imgur.com/$inputImage";
    if (isset($images[$url])) {
        echo " Skipping $inputImage, already mapped\n";
        continue;
    }

    $lastURL = getCurrentSkin();
    echo "Current skin URL: $lastURL\n";
    echo "Uploading $inputImage\n";

    $inputFile = $inputFolder . '/' . $inputImage;
    $post = array
    (
        //'authenticityToken' => $accessToken,
        'authenticityToken' => AUTH_TOKEN,
        'model' => 'steve',
        'skin' => '@' . $inputFile
    );

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, 'https://minecraft.net/profile/skin');
    curl_setopt($ch, CURLOPT_COOKIE, USER_COOKIE);
    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $post);
    curl_setopt($ch, CURLOPT_COOKIESESSION, true);
    curl_setopt($ch, CURLOPT_HEADER, true);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $result = curl_exec($ch);
    $httpcode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    if ($httpcode != 302) {
        die("Failed to upload skin\n");
    }
    curl_close($ch);

    echo("Uploaded.\n");
    $waitedTime = 0;
    $updated = false;
    while (!$updated) {
        echo "Waiting 5 minutes\n";
        sleep(5 * 60);
        $waitedTime += 5;
        $updatedURL = getCurrentSkin();
        echo " current url: $updatedURL\n";
        $updated = $updatedURL != $lastURL;
    }

    $images[$url] = $updatedURL;
    $yaml = spyc_dump($images);
    file_put_contents($mapFile, $yaml);
    echo "Updated YAML map\n";

    if ($waitedTime < 10) {
        $waitedTime = 10 - $waitedTime;
        echo "Waiting $waitedTime more minutes\n";
        sleep($waitedTime * 60);
    }

    echo "Waiting 2 more minutes\n";
    sleep(2 * 60);
}