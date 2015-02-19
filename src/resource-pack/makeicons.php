<?php

function startsWith($haystack, $needle)
{
    // search backwards starting from haystack length characters from the end
    return $needle === "" || strrpos($haystack, $needle, -strlen($haystack)) !== FALSE;
}

$inputFolder = dirname(__FILE__) . '/icons_scaled';
$outputFolder = dirname(__FILE__) . '/icons_skulls';
$dir = new DirectoryIterator($inputFolder);

foreach ($dir as $fileinfo)
{
    $filename = $fileinfo->getFilename();
    if (!$fileinfo->isDot() && !startsWith($filename, '.'))
    {
        echo "Converting $filename\n";
        $source = imagecreatefrompng($inputFolder . '/' . $filename);
        $skull = imagecreatetruecolor(64, 16);
        imagealphablending($skull, false);

        $transparent = imagecolorallocatealpha($skull, 255, 255, 255, 127);
        imagefilledrectangle($skull, 0, 0, 64, 16, $transparent);
        imagealphablending($skull, true);

        imagecopyresampled($skull, $source, 8,  0, 0, 0, 8, 8, 8, 8);
        imagealphablending($skull, true);
        imagecopyresampled($skull, $source, 16, 0, 0, 0, 8, 8, 8, 8);
        imagealphablending($skull, true);
        imagecopyresampled($skull, $source, 0,  8, 0, 0, 8, 8, 8, 8);
        imagealphablending($skull, true);
        imagecopyresampled($skull, $source, 8,  8, 0, 0, 8, 8, 8, 8);
        imagealphablending($skull, true);
        imagecopyresampled($skull, $source, 16, 8, 0, 0, 8, 8, 8, 8);
        imagealphablending($skull, true);
        imagecopyresampled($skull, $source, 24, 8, 0, 0, 8, 8, 8, 8);
        imagealphablending($skull, true);
        imagesavealpha($skull, true);
        imagepng($skull, $outputFolder . '/' . $filename);
        imagedestroy($source);
        imagedestroy($skull);
    }
}
