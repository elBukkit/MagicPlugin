<?php

// Read in the configuration file. You will need to change this path.
//$magicRootFolder = '/Users/nathan/Server/plugins/Magic';
$magicRootFolder = '/Users/nathan/Documents/Code/Bukkit/Testing/plugins/Magic';

// Page title
$title = "elMakers Magic Development Site";

// Page overview - this will get put in a Header at the top of the page.
$pageOverview = <<<EOT
	<div style="float:left; margin: 5px;">
		<img src="image/logo.png" alt="elMakers Logo" style="width:128px; height:128px"></img>
	</div>
	<div style="float:left; margin-left: 8px;">
		Welcome to the development server for the Magic plugin by elMakers!<br/><br/>
		This is a plugin for the <a href="http://www.bukkit.org" target="_new">Bukkit</a> minecraft server. 
		For more information, <a href="http://dev.bukkit.org/bukkit-plugins/magic/" target="_new">click here.</a>
		<br/><br/>
		While this is just a development server, you are free to log in and play at 
		<span class="minecraftServer">mine.elmakers.com</span>. You may also view our <a href="http://mine.elmakers.com:8080"/>dynamp here</a>, the world is a bit of a mess.
		<br/><br/>
		Thanks for looking!
	</div>
	<div style="clear:both"></div>
EOT;

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
		<link rel="stylesheet" href="css/smoothness/jquery-ui-1.10.3.custom.min.css" />
		<link rel="stylesheet" href="css/magic.css" />
		<script src="js/jquery-1.10.2.min.js"></script>
		<script src="js/jquery-ui-1.10.3.custom.min.js"></script>
		 <script>
		  $(function() {
		    $( "#tabs" ).tabs();
		  });
		  </script>
	</head>
	<body>
		<div id="heading"><?= $pageOverview ?></div>
		<div id="tabs">
			<ul>
				<li><a href="#overview">Overview</a></li>
				<li><a href="#spells">Spells</a></li>
				<li><a href="#wands">Wands</a></li>
			</ul>
			<div id="overview">
				TODO: Overview
			</div>
			<div id="spells">
				Found <?= count($spells); ?> Spells.
			</div>
			<div id="wands">
				TODO: Wands
			</div>
		</div>
	</body>
</html>