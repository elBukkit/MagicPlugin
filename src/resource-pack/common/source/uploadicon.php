<?php

error_reporting(E_ALL);
ini_set('display_errors', 1);

require_once('../../../web/spyc.php');

function startsWith($haystack, $needle)
{
    // search backwards starting from haystack length characters from the end
    return $needle === "" || strrpos($haystack, $needle, -strlen($haystack)) !== FALSE;
}

require_once('/Users/nathan/mc_creds.php');

$mapFile = dirname(__FILE__) . '/imgur_map.yml';
$inputFolder = dirname(__FILE__) . '/imgur_skins';
$cookieJar = '/Users/nathan/cookie.wolf';

if (!file_exists($mapFile))
{
    die("File not found: $mapFile\n");
}

// Login
/*
$post = array
(
    'agent' => array("name" => 'Minecraft', 'version' => 1),
    'username' => USER_LOGIN,
    'password' => USER_PASSWORD,
    'clientToken' => CLIENT_TOKEN
);
$postString = json_encode($post);

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://authserver.mojang.com/authenticate');
curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "POST");
curl_setopt($ch, CURLOPT_HTTPHEADER, array(
        'Content-Type: application/json',
        'Content-Length: ' . strlen($postString))
);
curl_setopt($ch, CURLOPT_POSTFIELDS, $postString);
curl_setopt($ch, CURLOPT_COOKIESESSION, true);
curl_setopt($ch, CURLOPT_COOKIEJAR, $cookieJar);
curl_setopt($ch, CURLOPT_COOKIEFILE, $cookieJar);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$result = curl_exec($ch);
$httpcode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
if ($httpcode != 200) {
    die("Failed to authenticate\n");
}
curl_close($ch);
$result = json_decode($result, true);
if (!isset($result['accessToken']))
{
    die("Failed to look up accessToken\n");
}
$accessToken = $result['accessToken'];
echo "Authenticated as " . USER_LOGIN .  ": $accessToken\n";
*/

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
    //return curl_getinfo($skinChecker, CURLINFO_REDIRECT_URL);
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
    // curl_setopt($ch, CURLOPT_HTTPHEADER, array('Content-type: application/x-www-form-urlencoded'));
    curl_setopt($ch, CURLOPT_URL, 'https://minecraft.net/profile/skin');
    //curl_setopt($ch, CURLOPT_URL, 'http://localhost/debug.php');
    curl_setopt($ch, CURLOPT_COOKIE, USER_COOKIE);
    curl_setopt($ch, CURLOPT_POST, 1);
    // curl_setopt($ch, CURLOPT_VERBOSE, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $post);
    curl_setopt($ch, CURLOPT_COOKIESESSION, true);
    //curl_setopt($ch, CURLOPT_COOKIEJAR, $cookieJar);
    //curl_setopt($ch, CURLOPT_COOKIEFILE, $cookieJar);
    curl_setopt($ch, CURLOPT_HEADER, true);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $result = curl_exec($ch);
    $httpcode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    if ($httpcode != 302) {
        die("Failed to upload skin\n");
    }
    curl_close($ch);
    // echo $result;

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