<?php
header('Content-Type: application/json');
if (!isset($_REQUEST['user'])) {
    die(json_encode(array('success' => false, 'message' => 'Missing user parameter')));
}

$userName = $_REQUEST['user'];

$profileFetch = curl_init();
curl_setopt($profileFetch, CURLOPT_FRESH_CONNECT, TRUE);
$profileUrl = 'https://api.mojang.com/users/profiles/minecraft/' . $userName;
curl_setopt($profileFetch, CURLOPT_URL, $profileUrl);
$headers = array(
    "Cache-Control: no-cache",
);
curl_setopt($profileFetch, CURLOPT_HTTPHEADER, $headers);
curl_setopt($profileFetch, CURLOPT_RETURNTRANSFER, true);
$result = curl_exec($profileFetch);
if (!$result) {
    die(json_encode(array('success' => false, 'message' => 'Failed to look up Minecraft profile')));
}

$result = json_decode($result, true);
$id = $result['id'];
$id = substr($id, 0, 8) . '-' . substr($id, 8, 4) . '-' . substr($id, 12, 4) . '-' . substr($id, 16, 4)  . '-' . substr($id, 20);

$code = mt_rand(100000, 999999);
echo json_encode(array('success' => true, 'code' => $code, 'id' => $id));