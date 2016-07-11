<?php

error_reporting(E_ALL);
ini_set('display_errors', 1);

require_once('../../../web/spyc.php');

// You will need to fill out some info here- see mc_creds.php.sample
require_once('mc_creds.php');

$mapFile = dirname(__FILE__) . '/image_map.yml';
$inputFolder = dirname(__FILE__) . '/skin_images';

if (!file_exists($mapFile))
{
    die("File not found: $mapFile\n");
}

function startsWith($haystack, $needle)
{
    // search backwards starting from haystack length characters from the end
    return $needle === "" || strrpos($haystack, $needle, -strlen($haystack)) !== FALSE;
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
        echo "Failed to retrieve skin, retrying in 2 minutes";
        sleep(2 * 60);
        $result = curl_exec($skinChecker);
        if (!$result)
        {
            die("Failed to retrieve profile $skinURL\n");
        }
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

function uploadSkin($inputFile)
{
    $skinFile = curl_file_create($inputFile, 'image/png', 'skin.png');
    $post = array
    (
        //'authenticityToken' => $accessToken,
        'authenticityToken' => AUTH_TOKEN,
        'model' => 'steve',
        'skin' => $skinFile
    );

    $ch = curl_init();

    curl_setopt($ch, CURLINFO_HEADER_OUT, true);

    curl_setopt($ch, CURLOPT_URL, 'https://minecraft.net/profile/skin');
    curl_setopt($ch, CURLOPT_COOKIE, USER_COOKIE);
    curl_setopt($ch, CURLOPT_REFERER, "https://minecraft.net/profile");
    curl_setopt($ch, CURLOPT_HTTPHEADER, array(
        'Host: minecraft.net',
        'Origin: https://minecraft.net',
        'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36')
    );
    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $post);
    curl_setopt($ch, CURLOPT_COOKIESESSION, true);
    curl_setopt($ch, CURLOPT_HEADER, true);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $output = curl_exec($ch);
    $httpcode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

    if (DEBUG)
    {
        $headerSent = curl_getinfo($ch, CURLINFO_HEADER_OUT );
        echo "\n.\n";
        echo($headerSent);
        echo "\n.\n";
        echo($output);
        echo "\n.\n";
    }

    if ($httpcode != 302) {
        echo("Failed to upload skin\n");
        if (!DEBUG) echo($output);
        return false;
    }
    curl_close($ch);
    return true;
}

// Save backup of original skin
$originalSkin = 'original_skin.png';
if (!file_exists($originalSkin))
{
    echo "Saving original skin to $originalSkin\n";
    $skinURL = getCurrentSkin();
    $skinFile = file_get_contents($skinURL);
    file_put_contents($originalSkin, $skinFile);
    echo "Waiting 2 minutes to start\n";
    sleep(2 * 60);
}

$images = spyc_load_file($mapFile);

$dir = new DirectoryIterator($inputFolder);
foreach ($dir as $fileinfo) {
    $inputImage = $fileinfo->getFilename();
    if ($fileinfo->isDot() || startsWith($inputImage, '.')) continue;
    $url = $inputImage;
    if (isset($images[$url])) {
        echo " Skipping $inputImage, already mapped\n";
        continue;
    }

    $lastURL = getCurrentSkin();
    echo "Current skin URL: $lastURL\n";
    echo "Uploading $inputImage\n";

    $inputFile = $inputFolder . '/' . $inputImage;
    if (!uploadSkin($inputFile))
    {
        echo "Waiting 10 minutes to retry upload\n";
        sleep(11 * 60);
        if (!uploadSkin($inputFile))
        {
            die("Failed to upload twice, stopping");
        }
    }

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

if (file_exists($originalSkin))
{
    echo "Re-uploading original skin from $originalSkin\n";
    if (!uploadSkin($originalSkin))
    {
        die("Failed to re-upload original skin!");
    }
    else
    {
        unlink($originalSkin);
    }
}