<?php


foreach ($_POST as $key => $value) {
    echo $key . " :\n";
    echo " $value\n\n";
}

echo "\nCOOKIES\n:";

foreach ($_COOKIE as $key => $value) {
    echo $key . " :\n";
    echo " $value\n\n";
}

?>