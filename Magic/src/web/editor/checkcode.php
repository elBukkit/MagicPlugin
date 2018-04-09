<?php
header('Content-Type: application/json');
if (!isset($_REQUEST['user'])) {
    die(json_encode(array('success' => false, 'message' => 'Missing user parameter')));
}
if (!isset($_REQUEST['code'])) {
    die(json_encode(array('success' => false, 'message' => 'Missing code parameter')));
}
require_once('../config.inc.php');

$userId = $_REQUEST['user'];
$userCode = $_REQUEST['code'];

$registeredFile = "$sandboxServer/plugins/Magic/data/registered.yml";
if (!file_exists($registeredFile)) {
    die(json_encode(array('success' => false, 'message' => 'Missing registration file')));
}

$registered = yaml_parse_file($registeredFile);
$registered = isset($registered[$userId]) ? $registered[$userId] : null;
if (is_null($registered) || $registered['code'] !== $userCode) {
    die(json_encode(array('success' => false, 'message' => 'Incorrect code')));
}

setcookie('user_id', $userId);
setcookie('user_code', $userCode);

$user = array(
    'id' => $userId,
    'code' => $userCode,
    'name' => $registered['name'],
    'skin' => $registered['skin_url'],
);

echo json_encode(array('success' => true, 'user' => $user));