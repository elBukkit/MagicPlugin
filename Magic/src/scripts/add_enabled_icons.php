#!/usr/bin/php
<?php

// Don't allow from Apache
if (PHP_SAPI !== 'cli')
{
    die('Nope.');
}

if (count($argv) < 2) {
    die("Usage: add_enabled_icons.php <folder>\n");
}

$folder = $argv[1];
if (!file_exists($folder)) {
    die("Folder does not exist: $folder\n");
}

function startsWith ($string, $startString){
    $len = strlen($startString);
    return (substr($string, 0, $len) === $startString);
}

$dir = new DirectoryIterator($folder);
foreach ($dir as $fileinfo)  {
    $filename = $fileinfo->getFilename();
    if ($fileinfo->isDot() || $fileinfo->isDir()) continue;
    $filePath = $fileinfo->getPathname();
    $iconContents = file_get_contents($filePath);
    $iconLines = explode("\n", $iconContents);
    $newLines = array();
    foreach ($iconLines as $line) {
        $newLine = '';
        if (startsWith(trim($line), 'vanilla_item:')) {
            $newLine = preg_replace_callback(
                '/(.*)\{(.*)\}/',
                function ($matches) {
                    return str_replace('vanilla_item', 'vanilla_item_enabled', $matches[1]) . '{' . (intval($matches[2]) + 1000) . '}';
                },
                $line
            );
        }
        $newLines[] = $line;
        if ($newLine) {
            $newLines[] = $newLine;
        }
    }
    file_put_contents($filePath, implode("\n", $newLines));
    echo "updated $filePath\n";
}
echo "Done!\n";