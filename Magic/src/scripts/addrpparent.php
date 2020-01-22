#!/usr/bin/php
<?php

// Don't allow from Apache
if (PHP_SAPI !== 'cli')
{
    die('Nope.');
}

function fixFiles($folder) {
    $dir = new DirectoryIterator($folder);
    foreach ($dir as $fileinfo) {
        if ($fileinfo->isDot()) continue;
        if ($fileinfo->isDir()) {
            fixFiles($fileinfo->getPathname());
        } else if (strpos($fileinfo->getFilename(), '.json') !== FALSE
            && strpos($fileinfo->getPathname(), '/models/') !== FALSE
            && strpos($fileinfo->getPathname(), '/target/') === FALSE
            && $fileinfo->getFilename() != 'lit.json') {
            $handle = fopen($fileinfo->getPathname(), "r");
            if ($handle) {
                $lines = array();
                $skip = false;
                $hasComments = false;
                while (($line = fgets($handle)) !== false) {
                    $line = preg_replace( "/\r|\n/", "", $line );
                    array_push($lines, $line);
                    $trimmedLine = trim($line);
                    if (strpos($trimmedLine, '"parent"') === 0) {
                        // echo "Skipping: " . $fileinfo->getPathname() . ", has parent already\n";
                        $skip = true;
                        break;
                    }
                    if (strpos($trimmedLine, '"__comment"') === 0) {
                        $hasComments = true;
                    }
                }
                fclose($handle);
                $cnt = count($lines);
                if (!$skip && $cnt < 3) {
                    $skip = true;
                    echo "Skipping: " . $fileinfo->getPathname() . ", only has $cnt lines\n";
                }
                if (!$skip) {
                    $insert = $hasComments ? 2 : 1;
                    $secondLine = $lines[1];
                    $indentSize = strlen($secondLine) - strlen(ltrim($secondLine));
                    $indent = '';
                    if ($indentSize > 0) {
                        $indentChar = substr($secondLine, 0, 1);
                        $indent = $indentSize == 0 ? '' : str_repeat($indentChar, $indentSize);
                    }
                    array_splice($lines, $insert, 0, $indent . '"parent" : "item/custom/lit",');
                    $newFile = implode("\n", $lines);
                    file_put_contents($fileinfo->getPathname(), $newFile);
                    echo "Modified: " . $fileinfo->getPathname() . "\n";
                }
            } else {
                echo "Could not open: " . $fileinfo->getPathname() . "\n";
            }
        }
    }
}

fixFiles(dirname(getcwd()));