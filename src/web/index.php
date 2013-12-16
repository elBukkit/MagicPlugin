<?php

// Read in the configuration file. You will need to change this path.
//$magicRootFolder = '/Users/nathan/Server/plugins/Magic';
$magicRootFolder = '/Users/nathan/Documents/Code/Bukkit/Testing/plugins/Magic';

// Page title
$title = "elMakers Magic Development Site";

// Don't edit after this part...

// Set up autoloader for external classes

function autoload($className)
{
	$className = ltrim($className, '\\');
	$fileName  = '';
	$namespace = '';
	if ($lastNsPos = strrpos($className, '\\')) {
		$namespace = substr($className, 0, $lastNsPos);
		$className = substr($className, $lastNsPos + 1);
		$fileName  = str_replace('\\', DIRECTORY_SEPARATOR, $namespace) . DIRECTORY_SEPARATOR;
	}
	$fileName .= str_replace('_', DIRECTORY_SEPARATOR, $className) . '.php';

	require $fileName;
}

spl_autoload_register(autoload);

use Symfony\Component\Yaml\Yaml;

$spellsFile = $magicRootFolder . '/spells.yml';
if (!file_exists($spellsFile)) {
	$spellsFile =  $magicRootFolder . '/spells_default.yml';
}

$spellsConfiguration = Yaml::parse($spellsFile);
$spells = [];
if (isset( $spellsConfiguration['spells'])) {
	$spells = $spellsConfiguration['spells'];
}

?>
<html>
	<head>
		<title><?= $title ?></title>
	</head>
	<body>
		Found <?= count($spells); ?> Spells.
	</body>
</html>