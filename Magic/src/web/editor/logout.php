<?php
header('Content-Type: application/json');
setcookie('user_id', null);
setcookie('user_code', null);
echo json_encode(array('success' => true));