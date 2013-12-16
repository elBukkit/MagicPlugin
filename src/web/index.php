<?php

require_once('config.inc.php');

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

spl_autoload_register('autoload');

use Symfony\Component\Yaml\Yaml;

function getConfigFile($name) {
	global $magicRootFolder;
	$configFile = "$magicRootFolder/$name.yml";
	if (!file_exists($configFile)) {
		$configFile =  "$magicRootFolder/$name_default.yml";
	}
	return $configFile;
}

// Load and parse Magic configuration files
$spellsConfiguration = Yaml::parse(getConfigFile('spells'));
$magicConfiguratiom = Yaml::parse(getConfigFile('magic'));
$wandConfiguratiom = Yaml::parse(getConfigFile('wands'));

$spells = [];
if (isset($spellsConfiguration['spells'])) {
	$spells = $spellsConfiguration['spells'];
}
ksort($spells);

$wands = [];
if (isset($wandConfiguratiom['wands'])) {
	$wands = $wandConfiguratiom['wands'];
}
ksort($wands);

// Look for important config options
$general = [];

if (isset($magicConfiguratiom['general'])) {
	$general = $magicConfiguratiom['general'];
}
$enchantingEnabled = isset($general['enable_enchanting']) ? $general['enable_enchanting'] : false;
$combiningEnabled = isset($general['enable_combining']) ? $general['enable_combining'] : false;
$blockPopulatorEnabled = isset($general['enable_block_populator']) ? $general['enable_block_populator'] : false;

$craftingMaterialUpper = isset($general['crafting_material_upper']) ? $general['crafting_material_upper'] : '';
$craftingMaterialLower = isset($general['crafting_material_lower']) ? $general['crafting_material_lower'] : '';
$craftingEnabled = isset($general['enable_crafting']) ? $general['enable_crafting'] : false;
$rightClickCycles = isset($general['right_click_cycles']) ? $general['right_click_cycles'] : false;

function underscoreToReadable($s) {
	if (!$s) return $s;
	$convertFunction = create_function('$c', 'return " " . strtoupper($c[1]);');
	return strtoupper($s[0]).  preg_replace_callback('/_([a-z])/', $convertFunction, substr($s, 1));
}

function printMaterial($materialKey, $iconOnly = null) {
	$materialName = underscoreToReadable($materialKey);
	$imagePath = 'image/material';
	$imageDir = dirname(__FILE__) . '/' . $imagePath;
	$materialIcon = str_replace('_', '', $materialKey) . '_icon32.png';
	$materialFilename = $imageDir . '/' . $materialIcon;
	if (file_exists($materialFilename)) {
		return $icon = '<span title="' . $materialName . '" class="materal_icon" style="background-image: url(' . $imagePath . '/' . $materialIcon . ')">&nbsp;</span>';
	} else {
		if ($iconOnly) {
			return '<span title="' . $materialName . '" class="materal_icon">&nbsp;</span>';
		}
	}
	return '<span class="material">' . $materialName . '</span>';
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
			var spells = <?= json_encode($spells); ?>;
			var wands = <?= json_encode($wands); ?>;

			function getMaterial(materialKey)
			{
				var materialName = materialKey.replace(/_/g, ' ');
				var imagePath = 'image/material';
				var materialIcon = materialKey.replace(/_/g, '') + '_icon32.png';
				var enclosingSpan = $('<span/>');
				var icon = $('<span title="' + materialName + '" class="materal_icon" style="background-image: url(' + imagePath + '/' + materialIcon + ')">&nbsp;</span>');
				var text = $('<span class="material"/>').text(materialName);
				enclosingSpan.append(icon);
				enclosingSpan.append(text);
				return enclosingSpan;
			}
			
			function getSpellDetails(key)
			{
				if (!(key in spells)) {
					return $('<span/>').text("Sorry, something went wrong!");
				}
				var spell = spells[key];
				var detailsDiv = $('<div/>');
	  			var title = $('<div class="spellTitleBanner"/>').text(spell.name);
	  			var description = $('<div class="spellDescription"/>').text(spell.description);
	  			var icon = $('<div class="spellIcon"/>');
	  			icon.append($('<span/>').text('Icon: '));
	  			icon.append(getMaterial(spell.icon));

	  			detailsDiv.append(title);
	  			detailsDiv.append(description);
	  			detailsDiv.append(icon);
	  			return detailsDiv;
			}
			
			function getWandDetails(key)
			{
				if (!(key in wands)) {
					return $('<span/>').text("Sorry, something went wrong!");
				}
				var wand = wands[key];
				var detailsDiv = $('<div/>');
	  			var title = $('<div class="wandTitleBanner"/>').text(wand.name);
	  			var description = $('<div class="wandDescription"/>').text(wand.description);

	  			detailsDiv.append(title);
	  			detailsDiv.append(description);
	  			return detailsDiv;
			}
  		 
			$(document).ready(function() {
			    $("#tabs").tabs();
			    $("#spellList").selectable({
					selected: function(event, ui) {
						$key = $(ui.selected).prop('title');
						$('#spellDetails').empty();
						$('#spellDetails').append(getSpellDetails($key));
					}
			    });
			    $("#wandList").selectable({
					selected: function(event, ui) {
						$key = $(ui.selected).prop('title');
						$('#wandDetails').empty();
						$('#wandDetails').append(getWandDetails($key));
					}
			    });
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
			  <div class="scrollingTab">
				<h2>Obtaining a Wand</h2>
				<div>
				In order to cast spells, you must find a wand. Each wand is unique and knows one or more spells. Wands can also be imbued with
				special properties and materials. Players do not "know" spells- if you lose your wand, you have no magic.<br/><br/>
				You may find a wand in one of the following ways:
				<ul>
					<?php if ($howToGetWands) echo "<li>$howToGetWands</li>"; ?>
					<?php if ($craftingEnabled) {
						echo '<li>Craft a wand with a ' . printMaterial($craftingMaterialUpper) . ' and a ' . 
								printMaterial($craftingMaterialLower);
						echo '</li>'; 
					}?>
					<?php if ($blockPopulatorEnabled) echo "<li>Find in random chests (dungeons, fortresses, etc)</li>"; ?>
				</ul>
				</div>
				<?php 
				if ($enchantingEnabled) {
					?>
					<div>You may upgrade your wands on an enchanting table.</div>
				<?php
				} ?>
				<?php 
				if ($combiningEnabled) {
					?>
					<div>You may combine two wands on an anvil (WIP).</div>
				<?php 
				} ?>
				<h2>Using a Wand</h2>
				<div>
					A wand is considered "active" when you are holding it. Any special effects a wand gives are only applied while the wand is active.<br.>
					<br/><br/>
					Swing a wand (left-click) to cast its active spell. Some wands may have more than one spell.<br/>
					<?php if ($rightClickCycles)  { ?>
						Right-click with your wand to cycle to the next spell.
					<?php } else {?>
						Right-click with your wand to toggle the wand inventory. When the wand's inventory is active, your survival items are stores
						and your player's inventory will change to show the spells and materials bound to your active wand:
						<br/><br/>
						<img src="image/WandHotbar.png" alt="Wand hotbar image"></img>
						<br/><br/>
						With the wand inventory active, each spell is represented by a material icon. You can quickly change spells using the hotbar buttons (1-9).
						<br/>You can also open your inventory ('E' by default) to see all of the spells and materials your wand has, with detailed descriptions:
						<br/><br/>
						<img src="image/WandInventory.png" alt="Wand inventory image"></img>
						<br/><br/>
						While in this view, you can re-arrange your spells and materials, deciding which ones to put in the hotbar.
						<br/><br/>
						For detailed instructions, see this video: (TODO: Updated Video!)<br/><br/>
						<iframe width="640" height="360" src="//www.youtube.com/embed/<?= $youTubeVideo ?>" frameborder="0" allowfullscreen></iframe>
					<?php } ?>
				</div>
				<h2>Costs</h2>
				<div>
					Casting costs vary by spell, wand, and server configuration.<br/><br/>
					The most common setup is the "mana" system. In this mode, each wand has a mana pool that 
					regenerates over time. While a wand is active, your mana is represented by the xp bar. (Your gathered xp will
					be saved and restored when the wand deactivates).<br/><br/>
					Other configurations could range from consuming actual XP, reagent items, or just being free.
					<br/><br/>
					Some wands may also have a limited number of uses, after which time they will self-destruct.
				</div>
			  </div>
			</div>
			<div id="spells">
			  <div class="scrollingTab">
			  	<div class="navigation">
				<ol id="spellList" class="selectable">
				<?php 
					foreach ($spells as $key => $spell) {
						echo '<li class="ui-widget-content" title="' . $key . '">' . printMaterial($spell['icon'], true) . '<span class="spellTitle">' . $spell['name'] . '</span></li>';
					}
				?>
				</ol>
			  </div>
			  </div>
			  <div class="details" id="spellDetails">
			  	Select a spell for details.
			  </div>
			</div>
			<div id="wands">
			  <div class="scrollingTab">
				<div class="navigation">
				<ol id="wandList" class="selectable">
				<?php 
					foreach ($wands as $key => $wand) {
						echo '<li class="ui-widget-content" title="' . $key . '">' .'<span class="wandTitle">' . $wand['name'] . '</span></li>';
					}
				?>
				</ol>
			  </div>
			  </div>
			  <div class="details" id="wandDetails">
			  	Select a wand for details.
			  </div>
			</div>
		</div>
	</body>
</html>