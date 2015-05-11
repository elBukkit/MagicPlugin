<?php

function startsWith($haystack, $needle)
{
    // search backwards starting from haystack length characters from the end
    return $needle === "" || strrpos($haystack, $needle, -strlen($haystack)) !== FALSE;
}

$inputFolder = dirname(__FILE__) . '/source_images';
$outputFolder = dirname(__FILE__) . '/skin_images';
$dir = new DirectoryIterator($inputFolder);

foreach ($dir as $fileinfo)
{
    $filename = $fileinfo->getFilename();
    if (!$fileinfo->isDot() && !startsWith($filename, '.'))
    {
        $outputFile = $outputFolder . '/' . $filename;
        if (file_exists($outputFile))
        {
            echo " Skipping $filename\n";
            continue;
        }
        echo "Converting $filename\n";
        $source = imagecreatefrompng($inputFolder . '/' . $filename);
        $skull = imagecreatetruecolor(64, 32);
        imagealphablending($skull, false);

        $transparent = imagecolorallocatealpha($skull, 255, 255, 255, 127);
        imagefilledrectangle($skull, 0, 0, 64, 32, $transparent);
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
        imagecopyresampled($skull, $source, 20, 22, 0, 0, 8, 8, 8, 8);
        imagealphablending($skull, true);
        imagecopyresampled($skull, $source, 32, 22, 0, 0, 8, 8, 8, 8);
        imagealphablending($skull, true);
        imagesavealpha($skull, true);
        imagepng($skull, $outputFile);
        imagedestroy($source);
        imagedestroy($skull);
    }
}
