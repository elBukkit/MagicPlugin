<?php

require_once('../config.inc.php');

function getUser() {
    global $sandboxServer;

    $userId = isset($_COOKIE['user_id']) ? $_COOKIE['user_id'] : '';
    $userCode = isset($_COOKIE['user_code']) ? $_COOKIE['user_code'] : '';

    $user = array(
        'id' => $userId,
        'name' => '',
        'skin' => ''
    );

    if (!$sandboxServer) return $user;

    $registeredFile = "$sandboxServer/plugins/Magic/data/registered.yml";
    if (file_exists($registeredFile) && $userId && $userCode) {
        $registered = $config = yaml_parse_file($registeredFile);

        $registered = isset($registered[$userId]) ? $registered[$userId] : null;
        if ($registered && $registered['code'] === $userCode) {
            $user['name'] = $registered['name'];
            $user['skin'] = $registered['skin_url'];
        }
    }

    return $user;
}
