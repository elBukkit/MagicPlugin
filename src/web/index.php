<?php

require_once('config.inc.php');

require_once('spyc.php');

function parseConfigFile($name, $loadDefaults) {
	global $magicDefaultsFolder;
	global $magicRootFolder;

    $baseFile = "$magicDefaultsFolder/$name.defaults.yml";
	$overrideFile = "$magicRootFolder/$name.yml";

    if ($loadDefaults) {
	    $config = spyc_load_file($baseFile);
	    if (file_exists($overrideFile)) {
            $override = spyc_load_file($overrideFile);
            $config = array_replace_recursive($config, $override);
        }
    } else {
        $config = spyc_load_file($overrideFile);
    }

	return $config;
}

// Load and parse Magic configuration files
try {
    global $magicRootFolder;
    // Look for path override
    if (isset($_REQUEST['example'])) {
        $path = $_REQUEST['example'];
        $magicRootFolder = "$magicRootFolder/examples/$path";
    }

	$general = parseConfigFile('config', true);
	$spells = parseConfigFile('spells', $general['load_default_spells']);
	$wands = parseConfigFile('wands', $general['load_default_wands']);
	$messages = parseConfigFile('messages', true);
} catch (Exception $ex) {
	die($ex->getMessage());
}

$upgrades = array();

ksort($spells);

// Process economy data
$worthItems = array();

if (isset($general['worth_items'])) {
    $tempWorth = array();
    foreach ($general['worth_items'] as $item => $amount) {
      $tempWorth[$amount] = $item;
    }
    krsort($tempWorth);
    foreach ($tempWorth as $amount => $item) {
      $worthItems[] = array('item' => $item, 'amount' => $amount);
    }
}

$worthBase = isset($general['worth_base']) ? $general['worth_base'] : 1;

// Look up localizations
foreach ($spells as $key => $spell) {
	$spell['name'] = isset($messages['spells'][$key]['name']) ? $messages['spells'][$key]['name'] : '';
	$spell['description'] = isset($messages['spells'][$key]['description']) ? $messages['spells'][$key]['description'] : '';
	$spell['usage'] = isset($messages['spells'][$key]['usage']) ? $messages['spells'][$key]['usage'] : '';
	$spells[$key] = $spell;
}

// Parse wand properties needed for cost validation
$useModifier = isset($general['worth_use_multiplier']) ? $general['worth_use_multiplier'] : 1;
$worthBrush = isset($general['worth_brush']) ? $general['worth_brush'] : 0;
$worthMana = isset($general['worth_mana']) ? $general['worth_mana'] : 0;
$worthManaMax = isset($general['worth_mana_max']) ? $general['worth_mana_max'] : 0;
$worthManaRegeneration = isset($general['worth_mana_regeneration']) ? $general['worth_mana_regeneration'] : 0;
$worthHealthRegeneration = isset($general['worth_health_regeneration']) ? $general['worth_health_regeneration'] : 0;
$worthHungerRegeneration = isset($general['worth_hunger_regeneration']) ? $general['worth_hunger_regeneration'] : 0;
$worthDamageReduction = isset($general['worth_damage_reduction']) ? $general['worth_damage_reduction'] : 0;
$worthDamageReductionExplosions = isset($general['worth_damage_reduction_explosions']) ? $general['worth_damage_reduction_explosions'] : 0;
$worthDamageReductionFalling = isset($general['worth_damage_reduction_falling']) ? $general['worth_damage_reduction_falling'] : 0;
$worthDamageReductionPhysical = isset($general['worth_damage_reduction_physical']) ? $general['worth_damage_reduction_physical'] : 0;
$worthDamageReductionFire = isset($general['worth_damage_reduction_fire']) ? $general['worth_damage_reduction_fire'] : 0;
$worthDamageReductionProjectiles = isset($general['worth_damage_reduction_projectiles']) ? $general['worth_damage_reduction_projectiles'] : 0;
$worthCostReduction = isset($general['worth_cost_reduction']) ? $general['worth_cost_reduction'] : 0;
$worthCooldownReduction = isset($general['worth_cooldown_reduction']) ? $general['worth_cooldown_reduction'] : 0;
$worthHaste = isset($general['worth_haste']) ? $general['worth_haste'] : 0;
$worthEffectColor = isset($general['worth_effect_color']) ? $general['worth_effect_color'] : 0;
$worthEffectParticle = isset($general['worth_effect_particle']) ? $general['worth_effect_particle'] : 0;
$worthEffectSound = isset($general['worth_effect_sound']) ? $general['worth_effect_sound'] : 0;

// Wand limits for scaled displays
$maxXpRegeneration = isset($general['max_mana_regeneration']) ? $general['max_mana_regeneration'] : 0;
$maxXp = isset($general['max_mana']) ? $general['max_mana'] : 0;

// Process wands
// look up localizations
// Calculate worth
// Special-case for randomized wand
foreach ($wands as $key => $wand) {
	if (isset($wand['hidden']) && $wand['hidden']) {
		unset($wands[$key]);
		continue;
	}

	if ($key == 'random') {
		$wand['name'] = 'Randomized Wand';
		$wand['description'] = 'This is a randomized wand template, used for crafting, enchanting, and random wands in chests';
		
		$convertedRandomSpells = array();
		if (isset($wand['spells'])) {
			$wand['spell_probabilities'] = $wand['spells'];
			$randomSpells = $wand['spells'];
			$wand['spells'] = $convertedRandomSpells;
			
			foreach ($randomSpells as $spellKey => $probability) {
				$convertedRandomSpells[] = $spellKey;
			}
		}
		$wand['spells'] = $convertedRandomSpells;
	} else {
		$wand['name'] = isset($messages['wands'][$key]['name']) ? $messages['wands'][$key]['name'] : '';
		$wand['description'] = isset($messages['wands'][$key]['description']) ? $messages['wands'][$key]['description'] : '';
		$wandsSpells = isset($wand['spells']) ? $wand['spells'] : array();
		$worth = 0;
		foreach ($wandsSpells as $wandSpell) {
            if (isset($spells[$wandSpell]) && isset($spells[$wandSpell]['worth'])) {
               $worth += $spells[$wandSpell]['worth'];
            }
		}

		$wandBrushes = isset($wand['materials']) ? $wand['materials'] : array();
        $worth += (count($wandBrushes) * $worthBrush);
        $worth += (isset($wand['xp']) ? $wand['xp'] : 0) * $worthMana;
        $worth += (isset($wand['xp_max']) ? $wand['xp_max'] : 0) * $worthManaMax;
        $worth += (isset($wand['xp_regeneration']) ? $wand['xp_regeneration'] : 0) * $worthManaRegeneration;
        $worth += (isset($wand['hunger_regeneration']) ? $wand['hunger_regeneration'] : 0) * $worthHungerRegeneration;
        $worth += (isset($wand['health_regeneration']) ? $wand['health_regeneration'] : 0) * $worthHealthRegeneration;
        $worth += (isset($wand['damage_reduction']) ? $wand['damage_reduction'] : 0) * $worthDamageReduction;
        $worth += (isset($wand['damage_reduction_physical']) ? $wand['damage_reduction_physical'] : 0) * $worthDamageReductionPhysical;
        $worth += (isset($wand['damage_reduction_falling']) ? $wand['damage_reduction_falling'] : 0) * $worthDamageReductionFalling;
        $worth += (isset($wand['damage_reduction_fire']) ? $wand['damage_reduction_fire'] : 0) * $worthDamageReductionFire;
        $worth += (isset($wand['damage_reduction_projectiles']) ? $wand['damage_reduction_projectiles'] : 0) * $worthDamageReductionProjectiles;
        $worth += (isset($wand['damage_reduction_explosions']) ? $wand['damage_reduction_explosions'] : 0) * $worthDamageReductionExplosions;
        $worth += (isset($wand['cost_reduction']) ? $wand['cost_reduction'] : 0) * $worthCostReduction;
        $worth += (isset($wand['cooldown_reduction']) ? $wand['cooldown_reduction'] : 0) * $worthCooldownReduction;
        $worth += (isset($wand['effect_particle']) && strlen($wand['effect_particle']) > 0 ? $worthEffectParticle : 0);
        $worth += (isset($wand['effect_color']) && strlen($wand['effect_color']) > 0 ? $worthEffectColor : 0);
        $worth += (isset($wand['effect_sound']) && strlen($wand['effect_sound']) > 0 ? $worthEffectSound : 0);

		if (isset($wand['uses']) && $wand['uses'] > 0) {
		    $worth *= $useModifier;
		}

		$wand['worth'] = $worth;
		$wand['spells'] = $wandsSpells;
	}

	if (isset($wand['upgrade']) && $wand['upgrade']) {
        unset($wands[$key]);
        $upgrades[$key] = $wand;
    } else {
	    $wands[$key] = $wand;
	}
}
ksort($wands);
ksort($upgrades);

// Move randomized wand to top
if (isset($wands['random'])) {
	$randomArray = array('random' => $wands['random']);
	unset($wands['random']);
	$wands = $randomArray + $wands;
}

$enchantingEnabled = isset($general['enable_enchanting']) ? $general['enable_enchanting'] : false;
$combiningEnabled = isset($general['enable_combining']) ? $general['enable_combining'] : false;
$blockPopulatorEnabled = isset($general['enable_block_populator']) ? $general['enable_block_populator'] : false;

$wandItem = isset($general['wand_item']) ? $general['wand_item'] : '';
$craftingMaterialUpper = isset($general['crafting_material_upper']) ? $general['crafting_material_upper'] : '';
$craftingMaterialLower = isset($general['crafting_material_lower']) ? $general['crafting_material_lower'] : '';
$craftingEnabled = isset($general['enable_crafting']) ? $general['enable_crafting'] : false;
$rightClickCycles = isset($general['right_click_cycles']) ? $general['right_click_cycles'] : false;

$eraseMaterial = isset($general['erase_item']) ? $general['erase_item'] : 'sulphur';
$copyMaterial = isset($general['copy_item']) ? $general['copy_item'] : 'sugar';
$replicateMaterial = isset($general['replicate_item']) ? $general['replicate_item'] : 'nether_stalk';
$cloneMaterial = isset($general['clone_item']) ? $general['clone_item'] : 'pumpkin_seeds';

$books = array();
if (file_exists($infoBookRootConfig)) {
	$booksConfigKeys = array('version-check', 'onlogin', 'protected');
	$booksConfig = spyc_load_file($infoBookRootConfig);
	foreach ($booksConfig as $key => $book) {
		// Hacky.. InfoBook has a weird config :\
		if (!in_array($booksConfig, $booksConfigKeys)) {
			$books[$key] = $book;
		}
	}
}

function underscoreToReadable($s) {
	if (!$s) return $s;
	$convertFunction = create_function('$c', 'return " " . strtoupper($c[1]);');
	return strtoupper($s[0]) . preg_replace_callback('/_([a-z])/', $convertFunction, substr($s, 1));
}

function printMaterial($materialKey, $iconOnly = null) {
	$materialName = underscoreToReadable($materialKey);
	$imagePath = 'image/material';
	$imageDir = dirname(__FILE__) . '/' . $imagePath;
	$materialIcon = str_replace('_', '', str_replace(':', '', $materialKey)) . '_icon32.png';
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
		<link rel="shortcut icon" type="image/x-icon" href="/favicon.ico">
		<link rel="stylesheet" href="css/smoothness/jquery-ui-1.10.3.custom.min.css" />
		<link rel="stylesheet" href="css/magic.css" />
		<script src="js/jquery-1.10.2.min.js"></script>
		<script src="js/jquery-ui-1.10.3.custom.min.js"></script>
		<script>
			var spells = <?= json_encode($spells); ?>;
			var wands = <?= json_encode($wands); ?>;
			var upgrades = <?= json_encode($upgrades); ?>;
			var eraseMaterial = '<?= $eraseMaterial ?>';
			var copyMaterial = '<?= $copyMaterial ?>';
			var cloneMaterial = '<?= $cloneMaterial ?>';
			var replicateMaterial = '<?= $replicateMaterial ?>';
			var books = <?= json_encode($books); ?>;
			var worthItems = <?= json_encode($worthItems); ?>;
			var worthBase = <?= $worthBase ?>;
			var maxXpRegeneration = <?= $maxXpRegeneration ?>;
			var maxXp = <?= $maxXp ?>;
		</script>
		<script src="js/magic.js"></script>
		<?php if ($analytics) echo $analytics; ?>
	</head>
	<body>
		<div id="heading"><?= $pageOverview ?></div>
		<div id="tabs" style="display:none">
			<ul>
				<li><a href="#overview">Overview</a></li>
				<li><a href="#spells">Spells</a></li>
				<li><a href="#wands">Wands</a></li>
				<li><a href="#upgrades">Upgrades</a></li>
				<li id="booksTab"><a href="#books">Books</a></li>
			</ul>
			<div id="overview">
			  <div class="scrollingTab">
				<h2>Obtaining a Wand</h2>
				<div>
				In order to cast spells, you must obtain a wand. Each wand is unique and knows one or more spells. Wands can also be imbued with
				special properties and materials. Players do not "know" spells- if you lose your wand, you have no magic.<br/><br/>
				You may find a wand in one of the following ways:
				<ul>
					<?php if ($howToGetWands) {
						foreach ($howToGetWands as $item) {
							echo "<li>$item</li>"; 
						}
					}?>
					<?php if ($craftingEnabled) {
						echo '<li>Craft a wand with ' . printMaterial($craftingMaterialUpper) . ' and ' . 
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
					<div>You may combine two wands on an anvil. (Click the empty result slot, it's WIP!)</div>
				<?php 
				} ?>
				<h2>Using a Wand</h2>
				<div>
					A wand is considered "active" when you are holding it. Any special effects a wand gives are only applied while the wand is active.<br.>
					<br/><br/>
					Swing a wand (left-click) to cast its active spell. Some wands may have more than one spell. If a wand has multiple spells, you use the
					interact (right-click) action to select spells.
					<br/><br/>

						For detailed instructions, see this video:<br/><br/>
						<iframe width="640" height="360" src="//www.youtube.com/embed/<?= $youTubeVideo ?>" frameborder="0" allowfullscreen></iframe>
						<br/><br/>
					    Wands may function in one of three modes:<br/>
					    <b>Chest Mode</b><br/>
					    In the default mode, right-clicking with your wand will pop up a chest inventory. Click on a spell icon to activate it.<br/><br/>
					    If your wand has a lot of spells, right-click in the inventory to move to the next page.
					    <br/><br/>
					    <b>Inventory Mode</b><br/>
						Right-click with your wand to toggle the wand inventory. When the wand's inventory is active, your survival items are stored
						and your player's inventory will change to show the spells and materials bound to your active wand:
						<br/><br/>
						<img src="image/WandHotbar.png" alt="Wand hotbar image"></img>
						<br/><br/>
						With the wand inventory active, each spell is represented by an icon. You can quickly change spells using the hotbar buttons (1-9).
						<br/><br/>
						You can also open your inventory ('E' by default) to see all of the spells and materials your wand has, with detailed descriptions:
						<br/><br/>
						<img src="image/WandInventory.png" alt="Wand inventory image"></img>
						<br/><br/>
						While in this view, you can re-arrange your spells and materials, deciding which ones to put in the hotbar.
						<br/><br/>
						Right-click again to deactive the wand inventory and restore your items. Any items you
						collected while the wand inventory was active will be in your surivival inventory.
						<br/><br/>
						For wands with more than 35 spells, right-clicking an additional time will cycle to the next "page" of spells. You may also
						right-click on an item in the inventory to cycle the inventory page. Renaming a wand on an anvil will also organize its inventory,
						should it get too cluttered.
						<br/><br/>
						A spell or material can be quick-selected from an open wand inventory using shift+click.
						<br/><br/>
						<b>Cycle Mode</b><br/>
						This mode only works well with low-powered wands, ones that only have a few spells. In this mode
						you right-click to cycle through available spells- there is no menu, and no icons.
				</div>
				<h2>Costs</h2>
				<div>
					Casting costs vary by spell, wand, and server configuration.<br/><br/>
					The most common setup is the "mana" system. In this mode, each wand has a mana pool that 
					regenerates over time. While a wand is active, your mana is represented by the XP bar. (Your gathered XP will
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
				<ol id="spellList">
				<?php 
					foreach ($spells as $key => $spell) {
						$icon = isset($spell['icon']) ? printMaterial($spell['icon'], true) : '';
						$name = isset($spell['name']) ? $spell['name'] : "($key)";
						echo '<li class="ui-widget-content" id="spell-' . $key . '">' . $icon . '<span class="spellTitle">' . $name . '</span></li>';
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
				<ol id="wandList">
				<?php 
					foreach ($wands as $key => $wand) {
						$extraStyle = '';
						if (isset($wand['effect_color'])) {
							$effectColor = $wand['effect_color'];
							if ($effectColor == 'FFFFFF') {
								$effectColor = 'DDDDDD';
							}
							$extraStyle = 'font-weight: bold; color: #' . $effectColor;
						}
						$name = isset($wand['name']) ? $wand['name'] : "($key)";
						$wandClass = ($key == 'random') ? 'randomWandTitle' : 'wandTitle';
						$icon = isset($wand['icon']) ? $wand['icon'] : 'wand';
						$icon = printMaterial($icon, true);
						echo '<li class="ui-widget-content" style="' . $extraStyle . '" id="wand-' . $key . '">' . $icon . '<span class="' . $wandClass . '">' . $name . '</span></li>';
					}
				?>
				</ol>
			  </div>
			  </div>
			  <div class="details" id="wandDetails">
			  	Select a wand for details.
			  </div>
			</div>
			<div id="upgrades">
              <div class="scrollingTab">
                <div class="navigation">
                <ol id="upgradeList">
                <?php
                    foreach ($upgrades as $key => $upgrade) {
                        $extraStyle = '';
                        if (isset($upgrade['effect_color'])) {
                            $effectColor = $upgrade['effect_color'];
                            if ($effectColor == 'FFFFFF') {
                                $effectColor = 'DDDDDD';
                            }
                            $extraStyle = 'font-weight: bold; color: #' . $effectColor;
                        }
                        $name = isset($upgrade['name']) ? $upgrade['name'] : "($key)";
                        $icon = isset($upgrade['icon']) ? $upgrade['icon'] : 'nether_star';
                        $icon = printMaterial($icon, true);
                        echo '<li class="ui-widget-content" style="' . $extraStyle . '" id="wand-' . $key . '">' . $icon . '<span class="wandTitle">' . $name . '</span></li>';
                    }
                ?>
                </ol>
              </div>
              </div>
              <div class="details" id="upgradeDetails">
                Select an item for details.
              </div>
            </div>
			<div id="books">
			  <div class="scrollingTab">
				<div class="navigation">
				<ol id="bookList">
				<?php 
					foreach ($books as $key => $book) {
						if (!isset($book['title'])) continue;
						echo '<li class="ui-widget-content" id="book-' . $key . '">' .'<span class="bookTitle">' . $book['title'] . '</span></li>';
					}
				?>
				</ol>
			  </div>
			  </div>
			  <div class="details" id="bookDetails">
			  	Select a book to read.
			  </div>
			</div>
		</div>
	</body>
</html>