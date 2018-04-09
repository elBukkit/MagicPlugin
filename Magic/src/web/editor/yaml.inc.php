<?php
function yaml_emit_clean($object) {
    $object = yaml_emit($object, YAML_UTF8_ENCODING);

    // yaml_emit puts a strange first and last line, I could not figure out why.
    function stripFirstLine($text)
    {
        return substr($text, strpos($text, "\n") + 1);
    }
    function stripLastLine($text)
    {
        return substr($text, 0, strrpos($text, "\n"));
    }
    $object = stripFirstLine($object);
    $object = stripLastLine($object);
    $object = stripLastLine($object);
    $object .= "\n";
    return $object;
}