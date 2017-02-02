<?php

require_once('config.inc.php');

require_once('spyc.php');

function parseConfigFile($name, $loadDefaults, $disableDefaults = false) {
	global $magicDefaultsFolder;
	global $magicRootFolder;

    $baseFile = "$magicDefaultsFolder/$name.defaults.yml";
	$overrideFile = "$magicRootFolder/$name.yml";

    if ($loadDefaults) {
	    $config = spyc_load_file($baseFile);
	    if (file_exists($overrideFile)) {
            $override = spyc_load_file($overrideFile);
            if ($disableDefaults) {
                foreach ($config as $key => &$spell) {
                    $spell['enabled'] = false;
                }
            }
            $config = array_replace_recursive($config, $override);
        }
    } else {
        $config = spyc_load_file($overrideFile);
    }

    if (count($config) == 1 && $config[0] == 0) {
        $config = array();
    }

	return $config;
}

// Load and parse Magic configuration files
$path = 'default';
$texturePath = 'default';
try {
    global $magicRootFolder;
    // Look for path override
    if (isset($_REQUEST['example'])) {
        $path = $_REQUEST['example'];
        $magicRootFolder = "$magicRootFolder/examples/$path";
        
        // special-case hackiness to show painterly icons
        if ($path === 'painterly') $texturePath = 'painterly';
		else if ($path === 'potter') $texturePath = 'potter';
    }

	$general = parseConfigFile('config', true);
	if (!isset($general['load_default_spells'])) $general['load_default_spells'] = true;
	if (!isset($general['disable_default_spells'])) $general['disable_default_spells'] = false;
	if (!isset($general['load_default_wands'])) $general['load_default_wands'] = true;
	if (!isset($general['load_default_crafting'])) $general['load_default_crafting'] = true;
	if (!isset($general['load_default_enchanting'])) $general['load_default_enchanting'] = true;
	$allSpells = parseConfigFile('spells', $general['load_default_spells'], $general['disable_default_spells']);
	$wands = parseConfigFile('wands', $general['load_default_wands']);
	$crafting = parseConfigFile('crafting', $general['load_default_crafting']);
	$enchantingConfig = parseConfigFile('enchanting', $general['load_default_enchanting']);
	$messages = parseConfigFile('messages', true);
	
	// Load resource pack textures
	$spellJson = json_decode(file_get_contents('rp/default/assets/minecraft/models/item/diamond_axe.json'), true);
	$spellJson = $spellJson['overrides'];
	$spellIcons = array();
	$diamondUses = 1562;
	foreach ($spellJson as $spellPredicate) {
		$durability = round($spellPredicate['predicate']['damage'] * $diamondUses);
		$texture = str_replace('item/', '', $spellPredicate['model']);
		array_push($spellIcons,
			array('texture' => $texture, 'durability' => $durability)
		);
	}
	
} catch (Exception $ex) {
	die($ex->getMessage());
}

$upgrades = array();

// Look up localizations
$spells = array();
foreach ($allSpells as $key => $spell) {
    if ($key == 'default' || (isset($spell['hidden']) && $spell['hidden']) || (isset($spell['enabled']) && !$spell['enabled'])) {
        continue;
    }
    $spell['key'] = $key;

    $spell['upgrade_description'] = isset($messages['spells'][$key]['upgrade_description']) ? $messages['spells'][$key]['upgrade_description'] : '';
    if (strpos($key, '|') !== FALSE) {
        $spellPieces = explode('|', $key);
        $baseKey = $spellPieces[0];
        $level = $spellPieces[1];
        $spellLevel = $spells[$key];
        if (isset($allSpells[$baseKey])) {
            $baseSpell = &$allSpells[$baseKey];
            if (!isset($baseSpell['spell_levels'])) {
                $baseSpell['spell_levels'] = array($level => $spellLevel);
            } else {
                $baseSpell['spell_levels'][$level] = $spellLevel;
            }
        }
        continue;
    }

	if (isset($spell['inherit']) && $spell['inherit'])
    {
        $spell = array_merge($spell, $allSpells[$spell['inherit']]);
    }
	if ((isset($spell['hidden']) && $spell['hidden']) || (isset($spell['enabled']) && !$spell['enabled'])) {
		continue;
	}
    if (!isset($spell['name']))
    {
        $spell['name'] = isset($messages['spells'][$key]['name']) ? $messages['spells'][$key]['name'] : $key;
    }

    if (!isset($spell['description']))
    {
        $spell['description'] = isset($messages['spells'][$key]['description']) ? $messages['spells'][$key]['description'] : '';
    }
    $spell['extended_description'] = isset($messages['spells'][$key]['extended_description']) ? $messages['spells'][$key]['extended_description'] : '';
    $spell['usage'] = isset($messages['spells'][$key]['usage']) ? $messages['spells'][$key]['usage'] : '';
	$spells[$key] = $spell;
}

ksort($spells);

// Filter and link enchanting paths
$enchanting = array();
function getPath($key) {
    global $enchanting;
    global $enchantingConfig;

    if (!isset($enchanting[$key])) {
        $config = $enchantingConfig[$key];
        $spellKeys = isset($config['spells']) ? $config['spells'] : array();
        $pathSpells = array();
        if ($spellKeys) {
            foreach ($spellKeys as $spellKey => $spellData) {
                $pathSpells[] = $spellKey;
            }
        }
        $requiredSpells = isset($config['required_spells']) ? $config['required_spells'] : array();
        if (isset($config['inherit'])) {
            $baseConfig = getPath($config['inherit']);
            unset($baseConfig['hidden']);
            $config = array_replace_recursive($baseConfig, $config);
        }
        $config['required_spells'] = $requiredSpells;
        $config['path_spells'] = $pathSpells;
        $enchanting[$key] = $config;
    }

    return $enchanting[$key];
}
foreach ($enchantingConfig as $key => $path) {
    getPath($key);
}

// Two-passes for inheritance
foreach ($enchanting as $key => $path) {
    if ($key == 'default' || (isset($path['hidden']) && $path['hidden'])) {
        unset($enchanting[$key]);
        continue;
    }
    $path['name'] = isset($messages['paths'][$key]['name']) ? $messages['paths'][$key]['name'] : '';
    $path['description'] = isset($messages['paths'][$key]['description']) ? $messages['paths'][$key]['description'] : '';

    $convertedRandomSpells = array();
    if (isset($path['spells'])) {
        $path['spell_probabilities'] = $path['spells'];
        $randomSpells = $path['spells'];
        $path['spells'] = $convertedRandomSpells;

        foreach ($randomSpells as $spellKey => $probability) {
            $convertedRandomSpells[] = $spellKey;
        }
    }

    $path['spells'] = $convertedRandomSpells;
    $enchanting[$key] = $path;
}

ksort($enchanting);

// Process economy data
$worthItems = array();

if (isset($general['currency'])) {
    $tempWorth = array();
    foreach ($general['currency'] as $item => $data) {
      $tempWorth[$data['worth']] = $item;
    }
    krsort($tempWorth);
    foreach ($tempWorth as $amount => $item) {
      $worthItems[] = array('item' => $item, 'amount' => $amount);
    }
}

// Look up category naming info
$categories = isset($messages['categories']) ? $messages['categories'] : array();

$worthBase = isset($general['worth_base']) ? $general['worth_base'] : 1;

// Parse wand properties needed for cost validation
$useModifier = isset($general['worth_use_multiplier']) ? $general['worth_use_multiplier'] : 1;
$worthBrush = isset($general['worth_brush']) ? $general['worth_brush'] : 0;
$worthMana = isset($general['worth_mana']) ? $general['worth_mana'] : 0;
$worthManaMax = isset($general['worth_mana_max']) ? $general['worth_mana_max'] : 0;
$worthManaRegeneration = isset($general['worth_mana_regeneration']) ? $general['worth_mana_regeneration'] : 0;
$worthDamageReduction = isset($general['worth_damage_reduction']) ? $general['worth_damage_reduction'] : 0;
$worthDamageReductionExplosions = isset($general['worth_damage_reduction_explosions']) ? $general['worth_damage_reduction_explosions'] : 0;
$worthDamageReductionFalling = isset($general['worth_damage_reduction_falling']) ? $general['worth_damage_reduction_falling'] : 0;
$worthDamageReductionPhysical = isset($general['worth_damage_reduction_physical']) ? $general['worth_damage_reduction_physical'] : 0;
$worthDamageReductionFire = isset($general['worth_damage_reduction_fire']) ? $general['worth_damage_reduction_fire'] : 0;
$worthDamageReductionProjectiles = isset($general['worth_damage_reduction_projectiles']) ? $general['worth_damage_reduction_projectiles'] : 0;
$worthCostReduction = isset($general['worth_cost_reduction']) ? $general['worth_cost_reduction'] : 0;
$worthCooldownReduction = isset($general['worth_cooldown_reduction']) ? $general['worth_cooldown_reduction'] : 0;
$worthEffectColor = isset($general['worth_effect_color']) ? $general['worth_effect_color'] : 0;
$worthEffectParticle = isset($general['worth_effect_particle']) ? $general['worth_effect_particle'] : 0;
$worthEffectSound = isset($general['worth_effect_sound']) ? $general['worth_effect_sound'] : 0;

// Wand limits for scaled displays
$maxXpRegeneration = isset($general['max_mana_regeneration']) ? $general['max_mana_regeneration'] : 0;
$maxXp = isset($general['max_mana']) ? $general['max_mana'] : 0;

// Process wands
// Look up localizations
// Calculate worth
// Hide hidden wands, organize upgrades
foreach ($wands as $key => $wand) {
	if (isset($wand['hidden']) && $wand['hidden']) {
		unset($wands[$key]);
		continue;
	}

    $wand['name'] = isset($messages['wands'][$key]['name']) ? $messages['wands'][$key]['name'] : '';
    $wand['description'] = isset($messages['wands'][$key]['description']) ? $messages['wands'][$key]['description'] : '';
    $wandsSpells = isset($wand['spells']) ? $wand['spells'] : array();
	if (!is_array($wandsSpells)) {
		$wandsSpells = array();
	}
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

	if (isset($wand['upgrade']) && $wand['upgrade']) {
        unset($wands[$key]);
        $upgrades[$key] = $wand;
    } else {
	    $wands[$key] = $wand;
	}
}
ksort($wands);
ksort($upgrades);

// Look up craftable wands
foreach ($crafting as $key => &$recipe) {
    if (!isset($recipe['output_type']) || $recipe['output_type'] != 'wand')
    {
        $recipe['wand'] = null;
        continue;
    }
    $recipe['wand'] = $wands[$recipe['output']];
}

$enchantingEnabled = isset($general['enable_enchanting']) ? $general['enable_enchanting'] : false;
$combiningEnabled = isset($general['enable_combining']) ? $general['enable_combining'] : false;

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

$textures = array();
$textureConfig = $magicRootFolder . '/../../resource-pack/common/source/image_map.yml';
if (file_exists($textureConfig)) {
    $textures = array_values(spyc_load_file($textureConfig));
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

function printIcon($iconUrl, $title) {
    return $icon = '<span title="' . $title . '" class="url_icon materal_icon" style="background-image: url(' . $iconUrl . ')">&nbsp;</span>';
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
			var paths = <?= json_encode($enchanting); ?>;
			var recipes = <?= json_encode($crafting); ?>;
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
            var categories = <?= json_encode($categories) ?>;
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
				<li><a href="#crafting">Crafting</a></li>
				<li><a href="#enchanting">Enchanting</a></li>
				<li><a href="#wands">Wands and Items</a></li>
				<li><a href="#upgrades">Upgrades</a></li>
				<li id="booksTab"><a href="#books">Books</a></li>
                <li><a href="#textures">Textures</a></li>
				<li><a href="#icons">Icons</a></li>
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
                <div>There are also <?= count($wands); ?> wand templates, which may be purchasable or given my admins.</div>

                <h2>Wand Spells</h2>
                <div>
                    Wands contain one or more spells in their inventory. Wands may generally know any spell, though some wands
                    may only naturally obtain certain spells via enchanting.
                </div>
                <br/>
                <div>
                    There are currently <?= count($spells) ?> spells available.
                </div>


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
					    If your wand has a lot of spells, click outside of the inventory window to move to the next page. Right-click outside of the inventory to move back a page.
					    <br/><br/>
					    <b>Inventory Mode</b><br/>
						Right-click with your wand to toggle the wand inventory. When the wand's inventory is active, your survival items are stored
						and your player's inventory will change to show the spells and materials bound to your active wand:
						<br/><br/>
						<img src="image/WandHotbar.png" alt="Wand hotbar image"/>
						<br/><br/>
						With the wand inventory active, each spell is represented by an icon. You can quickly change spells using the hotbar buttons (1-9).
						<br/><br/>
						You can also open your inventory ('E' by default) to see all of the spells and materials your wand has, with detailed descriptions:
						<br/><br/>
						<img src="image/WandInventory.png" alt="Wand inventory image"/>
						<br/><br/>
						While in this view, you can re-arrange your spells and materials, deciding which ones to put in the hotbar.
						<br/><br/>
						Right-click again to deactivate the wand inventory and restore your items. Any items you
						collected while the wand inventory was active will be in your survival inventory.
						<br/><br/>
						For wands with more than 35 spells, clicking outside of the inventory will cycle to the next "page" of spells.
                        Right-clicking outside of the inventory will go to the previous page.
                        Renaming a wand on an anvil will organize its inventory,
						should it get too cluttered.
						<br/><br/>
						A spell or material can be quick-selected from an open wand inventory using right-click.
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
                        $name = isset($spell['name']) ? $spell['name'] : "($key)";
						
						$iconFile = 'survival/assets/minecraft/textures/items/spells/' . $key . '.png';
						if (file_exists($resourcePackFolder . $iconFile))
						{
							$icon = printIcon('rp/' . $iconFile, $name);
						}
                        else if (isset($spell['icon_url']))
                        {
                            $icon = printIcon($spell['icon_url'], $name);
                        }
                        else
                        {
                            $icon = isset($spell['icon']) ? printMaterial($spell['icon'], true) : '';
                        }
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
			<div id="crafting">
			  <div class="scrollingTab">
			  	<div class="navigation">
				<ol id="craftingList">
				<?php
					foreach ($crafting as $key => $craftingRecipe)
                    {
						$wand = $craftingRecipe['wand'];
						if ($wand) {
							$name = isset($wand['name']) && $wand['name'] ? $wand['name'] : "($key)";
						} else {
							$name = $key;
						}
						$nameSpan = $name;
						if (isset($craftingRecipe['enabled']) && !$craftingRecipe['enabled']) {
							$nameSpan = '<span class="disabled">' . $name . '</span>';
						}
						$icon = $craftingRecipe['output'];
						if ($wand && isset($wand['icon']))
						{
							$icon = $wand['icon'];
							if (strpos($icon, 'skull_item:') !== FALSE) {
								$icon = trim(substr($icon, 11));
								$icon = printIcon($icon, $name);
							} else {
								$icon = printMaterial($icon, true);
							}
						} else {
							$icon = printMaterial($icon, true);
						}
						echo '<li class="ui-widget-content" id="recipe-' . $key . '">' . $icon . '<span class="recipeTitle">' . $nameSpan . '</span></li>';
					}
				?>
				</ol>
			  </div>
			  </div>
			  <div class="details" id="craftingDetails">
			  	Select a recipe for details.
			  </div>
			</div>
			<div id="enchanting">
			  <div class="scrollingTab">
			  	<div class="navigation">
				<ol id="enchantingList">
				<?php
					foreach ($enchanting as $key => $path) {
                        $name = isset($path['name']) ? $path['name'] : "($key)";

						echo '<li class="ui-widget-content" id="path-' . $key . '"><span class="pathTitle">' . $name . '</span></li>';
					}
				?>
				</ol>
			  </div>
			  </div>
			  <div class="details" id="enchantingDetails">
			  	Select an enchanting path for details.
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
						$name = isset($wand['name']) && $wand['name'] ? $wand['name'] : "($key)";
						$wandClass = ($key == 'random') ? 'randomWandTitle' : 'wandTitle';

                        $icon = 'wand';
                        if (isset($wand['icon']))
                        {
                            $icon = $wand['icon'];
                            if (strpos($icon, 'skull_item:') !== FALSE) {
                                $icon = trim(substr($icon, 11));
                                $icon = printIcon($icon, $name);
                            } else {
                                $icon = printMaterial($icon, true);
                            }
                        } else {
                            $icon = printMaterial($icon, true);
                        }

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
                        $name = isset($upgrade['name']) && $upgrade['name'] ? $upgrade['name'] : "($key)";
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
            <div id="icons">
                <div class="scrollingTab">
                    <div>
                        <div class="title">
                            There are <?= count($spellIcons) ?> spell icons available in the Magic RP, each is a variant of the diamond axe item.
                        </div>
                        <ul id="iconList">
                            <?php
                            foreach ($spellIcons as $spellIcon) {
								if ($spellIcon['durability'] == 0) continue;
                                $icon = printIcon($texture, $texture);
                                echo '<li class="ui-widget-content"><img src="rp/' . $texturePath . '/assets/minecraft/textures/items/' . $spellIcon['texture'] . '.png"> <span class="iconItem">diamond_axe:' . $spellIcon['durability'] . '</span><span class="iconName">(' . $spellIcon['texture'] . ')</span></li>';
                            }
                            ?>
                        </ul>
                    </div>
                </div>
            </div>
			<div id="textures">
				<div class="scrollingTab">
					<div>
						<div class="title">
							Legacy configs use player skulls for icons, here are <?= count($textures) ?> that have been made or chosen specifically for Magic.
						</div>
						<ul id="textureList">
							<?php
							foreach ($textures as $texture) {
								$icon = printIcon($texture, $texture);
								echo '<li class="ui-widget-content">' . $icon . '<span class="textureURL">' . $texture . '</span></li>';
							}
							?>
						</ul>
					</div>
				</div>
			</div>
		</div>
	</body>
</html>