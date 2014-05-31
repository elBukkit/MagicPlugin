
function getMaterial(materialKey, iconOnly)
{
	if (materialKey == null || materialKey.length == 0) return "";
	
	iconOnly = (typeof iconOnly === 'undefined') ? false : iconOnly;
	var materialName = materialKey.replace(/_/g, ' ');
	if (materialKey == 'copy') {
		materialKey = copyMaterial;
		materialName = 'Copy';
		iconOnly = false;
	} else if (materialKey == 'erase') {
		materialKey = eraseMaterial;
		materialName = 'Erase';
		iconOnly = false;
	} else if (materialKey == 'replicate') {
		materialKey = replicateMaterial;
		materialName = 'Replicate';
		iconOnly = false;
	} else if (materialKey == 'clone') {
		materialKey = cloneMaterial;
		materialName = 'Clone';
		iconOnly = false;
	}
	var imagePath = 'image/material';
	var materialIcon = materialKey.replace(/[_:]/g, '') + '_icon32.png';
	var enclosingSpan = $('<span/>');
	var icon = $('<span title="' + materialName + '" class="materal_icon" style="background-image: url(' + imagePath + '/' + materialIcon + ')">&nbsp;</span>');
	enclosingSpan.append(icon);
	if (!iconOnly) {
		var text = $('<span class="material"/>').text(materialName);
		enclosingSpan.append(text);
	}
	return enclosingSpan;
}

function getWorth(worth)
{
    worth = worth * worthBase;
    var worthSpan = $('<span title="' + worth + '" class="worth"></span>');
    var remainder = worth;
    var itemIndex = 0;
    var text = "";
    for (var index in worthItems)
    {
        var itemAmount = worthItems[index].amount;
        var itemKey = worthItems[index].item;
        if (itemAmount < remainder) {
            var amount = Math.floor(remainder / itemAmount);
            remainder = remainder % itemAmount;
            var currencySpan = jQuery('<span/>');
            currencySpan.append(getMaterial(itemKey, true));
            currencySpan.append(jQuery('<span>x' + amount + '</span>'));
            currencySpan.append(jQuery('<span>&nbsp;&nbsp;&nbsp;</span>'));
            worthSpan.append(currencySpan);
        }
    }
    if (remainder > 0) {
        worthSpan.append(jQuery('<span>+' + worth + '</span>'));
    }
    return worthSpan;
}

function getSpellDetails(key, showTitle, useMana, costReduction, probabilityString)
{
	showTitle = (typeof showTitle === 'undefined') ? true : showTitle;
	useMana = (typeof useMana === 'undefined') ? false : useMana;
	costReduction = (typeof costReduction === 'undefined') ? 0 : costReduction;
	if (!(key in spells)) {
		return $('<span/>').text("Sorry, something went wrong!");
	}
	var spell = spells[key];
	var detailsDiv = $('<div/>');
	if (showTitle) {
		var title = $('<div class="spellTitleBanner"/>').text(spell.name);
		detailsDiv.append(title);
	}
	var description = $('<div class="spellDescription"/>').text(spell.description);
	var icon = $('<div class="spellIcon"/>');
	icon.append($('<span/>').text('Icon: '));
	icon.append(getMaterial(spell.icon));

	detailsDiv.append(description);
	detailsDiv.append(icon);
	
	// Check for rarity
	if (probabilityString != null && probabilityString.length > 0) {
		var rarityClass = 'spellCommon';
		var rarityDescription = 'Common';
		var overallWeight = 0;
		var pieces = probabilityString.split(',');
		for (var index in pieces) {
			overallWeight += parseInt(pieces[index]);
		}
		
		if (overallWeight < 5) {
			rarityClass = 'spellVeryRare';
			rarityDescription = 'Very Rare';
		} else if (overallWeight < 20) {
			rarityClass = 'spellRare';
			rarityDescription = 'Rare';
		} else if (overallWeight < 100) {
			rarityClass = 'spellUncommon';
			rarityDescription = 'Uncommon';
		}
		
		var probabilityDescription = $('<div class="spellProbability ' + rarityClass + '"/>')
			.text(rarityDescription);
		
		detailsDiv.append(probabilityDescription);
	}

    // List worth, if present
    if ('worth' in spell && spell.worth > 0) {
		detailsDiv.append($('<div class="worthHeading"/>').text('Suggested Price'));
        detailsDiv.append(getWorth(spell.worth));
    }

	var firstCost = true;
	if ('costs' in spell) {
		detailsDiv.append($('<div class="spellHeading"/>').text('Costs'));
		var costList = $('<ul/>');
		for (var costKey in spell.costs) {
			var amount = spell.costs[costKey];
			if (costReduction > 0) {
				if (costReduction > 1) costReduction = 1;
				amount = amount * (1 - costReduction);
			}
			if (costKey == 'xp') {
				if (useMana) {
					costList.append($('<li/>').text("Mana: " + amount));
				} else {
					costList.append($('<li/>').text("XP: " + amount));
				}
			} else {
				costList.append($('<li/>').append(getMaterial(costKey, true)).append($('<span/>').text(': ' + amount)));
			}
		}
		detailsDiv.append(costList);
	}
	if ('active_costs' in spell) {
		detailsDiv.append($('<div class="spellHeading"/>').text('Active Costs (per Second)'));
		var costList = $('<ul/>');
		for (var costKey in spell.active_costs) {
			var amount = spell.active_costs[costKey];
			if (costReduction > 0) {
				if (costReduction > 1) costReduction = 1;
				amount = amount * (1 - costReduction);
			}
			if (costKey == 'xp') {
				if (useMana) {
					costList.append($('<li/>').text("Mana: " + amount));
				} else {
					costList.append($('<li/>').text("XP: " + amount));
				}
			} else {
				costList.append($('<li/>').append(getMaterial(costKey, true)).append($('<span/>').text(': ' + amount)));
			}
		}
		detailsDiv.append(costList);
	}
	
	if (showTitle) {
		var admin = $('<div class="adminuse"/>').text("Admin use: /wand add " + key);
		detailsDiv.append(admin);
	}
	return detailsDiv;
}

function getLevelString(prefix, amount)
{
	var suffix = "I";

	if (amount > 1) {
		suffix = "X";
	} else if (amount > 0.8) {
		suffix = "V";
	} else if (amount > 0.6) {
		suffix = "IV";
	} else if (amount > 0.4) {
		suffix = "III";
	} else if (amount > 0.2) {
		suffix = "II";
	}
	return prefix + " " + suffix;
}

function getBookDetails(key)
{
	if (!(key in books)) {
		return $('<span/>').text("Sorry, something went wrong!");
	}
	var book = books[key];
	var detailsDiv = $('<div/>');
	var title = $('<div class="bookTitleBanner"/>').text(book.title);
	var scrollingContainer = $('<div class="bookContainer"/>');	
	
	detailsDiv.append(title);
	detailsDiv.append(scrollingContainer);

	if ('description' in book && book.description.length > 0) {
		var description = $('<div class="bookDescription"/>');
		for (var descriptionIndex in book.description) {
			var descriptionLine = book.description[descriptionIndex];
			description.append(descriptionLine).append(jQuery('<br/>'));
		}
		scrollingContainer.append(description);
	}
	
	var pages = $('<div class="bookPages"/>');
	for (var pageIndex in book.pages) {
		var page = book.pages[pageIndex];
		var lines = page.split("&x");
		for (var lineIndex in lines) {
			var line = lines[lineIndex];
			var lineStyle = "";
			line = line.replace(/\&(.)/g, function (match, capture) {
				lineStyle += getLineStyle(capture);
				return "";
			});

			var lineSpan = jQuery('<span style="' + lineStyle + '"/>');
			lineSpan.text(line);
			pages.append(lineSpan).append(jQuery('<br/>'));
		}
		
		pages.append(jQuery('<br/>')).append(jQuery('<hr/>')).append(jQuery('<br/>'));
	}
	scrollingContainer.append(pages);

	return detailsDiv;
}

function getLineStyle(chatChar)
{
	if (chatChar == 'l') {
		return 'font-weight: bold;';
	}
	if (chatChar == 'n') {
		return 'text-decoration: underline;';
	}
	if (chatChar == 'o') {
		return 'font-style: italic;';
	}
	var color = chatColorToHex(chatChar.toLowerCase());
	if (color.length > 0) {
		return 'color: #' + color + ';';
	}
	
	return '';
}

function chatColorToHex(chatChar)
{
	switch (chatChar) {
		case '0': return '000000';
		case '1': return '0000AA';
		case '2': return '00AA00';
		case '3': return '00AAAA';
		case '4': return 'AA0000';
		case '5': return 'AA00AA';
		case '6': return 'FFAA00';
		case '7': return 'AAAAAA';
		case '8': return '555555';
		case '9': return '5555FF';
		case 'a': return '55FF55';
		case 'b': return '55FFFF';
		case 'c': return 'FF5555';
		case 'd': return 'FF55FF';
		case 'e': return 'FFFF55';
		case 'f': return 'FFFFFF';
	}
	
	return '';
}

function getPathDetails(key)
{
    if (!(key in paths)) {
        return $('<span/>').text("Sorry, something went wrong!");
    }
    var wand = paths[key];
    return getWandItemDetails(key, wand);
}

function getRecipeDetails(key)
{
    if (!(key in recipes)) {
        return $('<span/>').text("Sorry, something went wrong!");
    }
    var recipe = recipes[key];
    return $('<span/>').text("WIP: " + recipe['output']);
}

function getWandDetails(key)
{
    if (!(key in wands)) {
		return $('<span/>').text("Sorry, something went wrong!");
	}
	var wand = wands[key];
	return getWandItemDetails(key, wand);
}

function getWandUpgradeDetails(key)
{
    if (!(key in upgrades)) {
		return $('<span/>').text("Sorry, something went wrong!");
	}
	var wand = upgrades[key];
	return getWandItemDetails(key, wand);
}

function getWandItemDetails(key, wand)
{
	var detailsDiv = $('<div/>');
	var title = $('<div class="wandTitleBanner"/>').text(wand.name);
	var scrollingContainer = $('<div class="wandContainer"/>');
	var description = $('<div class="wandDescription"/>').text(wand.description);
	var admin = $('<div class="adminuse"/>').text("Admin use: /wand " + key);
	var costReduction = ('cost_reduction' in wand) ? wand['cost_reduction'] : 0;
	var cooldownReduction = ('cooldown_reduction' in wand) ? wand['cooldown_reduction'] : 0;
	var xpRegeneration = ('xp_regeneration' in wand) ? wand['xp_regeneration'] : 0;
	var xpMax = ('xp_max' in wand) ? wand['xp_max'] : 0;
	var hungerRegeneration = ('hunger_regeneration' in wand) ? wand['hunger_regeneration'] : 0;
	var healthRegeneration = ('health_regeneration' in wand) ? wand['health_regeneration'] : 0;
	var uses = ('uses' in wand) ? wand['uses'] : 0;
	var protection = ('protection' in wand) ? wand['protection'] : 0;
	var protectionPhysical = ('protection_physical' in wand) ? wand['protection_physical'] : 0;
	var protectionProjectiles = ('protection_projectiles' in wand) ? wand['protection_projectiles'] : 0;
	var protectionFalling = ('protection_falling' in wand) ? wand['protection_falling'] : 0;
	var protectionFire = ('protection_fire' in wand) ? wand['protection_fire'] : 0;
	var protectionExplosion = ('protection_explosion' in wand) ? wand['protection_explosion'] : 0;
	var power = ('power' in wand) ? wand['power'] : 0;
	var haste = ('haste' in wand) ? wand['haste'] : 0;
	
	detailsDiv.append(title);
	scrollingContainer.append(description);

    // List worth, if present
    if ('worth' in wand && wand.worth > 0) {
		scrollingContainer.append($('<div class="worthHeading"/>').text('Suggested Price'));
        scrollingContainer.append(getWorth(wand.worth));
    }
	
	if (xpRegeneration > 0 && xpMax > 0) {
		scrollingContainer.append($('<div class="mana"/>').text('Mana: ' + xpMax));
		scrollingContainer.append($('<div class="regeneration"/>').text(getLevelString('Mana Regeneration', xpRegeneration / maxXpRegeneration)));
	}
	if (uses > 0) {
		scrollingContainer.append($('<div class="uses"/>').text('Uses: ' + uses));
	}
	if (costReduction > 0) {
		scrollingContainer.append($('<div class="costReduction"/>').text(getLevelString('Cost Reduction', costReduction)));
	}
	if (cooldownReduction > 0) {
		scrollingContainer.append($('<div class="cooldownReduction"/>').text(getLevelString('Cooldown Reduction', cooldownReduction)));
	}
	if (power > 0) {
		scrollingContainer.append($('<div class="power"/>').text(getLevelString('Power', power)));
	}
	if (haste > 0) {
		scrollingContainer.append($('<div class="haste"/>').text(getLevelString('Haste', haste)));
	}
	if (protection > 0) {
		scrollingContainer.append($('<div class="protection"/>').text(getLevelString('Protection', protection)));
	}
	if (protection < 1) {
		if (protectionPhysical > 0) scrollingContainer.append($('<div class="protection"/>').text(getLevelString('Physical Protection', protectionPhysical)));
		if (protectionProjectiles > 0) scrollingContainer.append($('<div class="protection"/>').text(getLevelString('Projectile Protection', protectionProjectiles)));
		if (protectionFalling > 0) scrollingContainer.append($('<div class="protection"/>').text(getLevelString('Falling Protection', protectionFalling)));
		if (protectionFire > 0) scrollingContainer.append($('<div class="protection"/>').text(getLevelString('Fire Protection', protectionFire)));
		if (protectionExplosion > 0) scrollingContainer.append($('<div class="protection"/>').text(getLevelString('Blast Protection', protectionExplosion)));		
	}
	if (healthRegeneration > 0) {
		scrollingContainer.append($('<div class="regeneration"/>').text(getLevelString('Health Regeneration', healthRegeneration)));
	}
	if (hungerRegeneration > 0) {
		scrollingContainer.append($('<div class="regeneration"/>').text(getLevelString('Hunger Regeneration', hungerRegeneration)));
	}
		
	var wandSpells = wand.spells;
	wandSpells.sort();
	var spellHeader = $('<div class="wandHeading">Spells (' + wandSpells.length + ')</div>');
	var spellListContainer = $('<div id="wandSpellList"/>');
	var spellList = $('<div/>');
	var usesMana = xpRegeneration > 0 || key == 'random';
	for (var spellIndex in wandSpells)
	{
		var key = wand.spells[spellIndex];
		var spell = spells[key];
		var probabilityString = "";
		if ('spell_probabilities' in wand && key in wand['spell_probabilities']) {
			probabilityString = wand['spell_probabilities'][key];
		}
		spellList.append($('<h3/>').text(spell.name));
		spellList.append($('<div/>').append(getSpellDetails(key, false, usesMana, costReduction, probabilityString)));
	}
	spellList.accordion({ heightStyle: 'content'} );
	spellListContainer.append(spellList);
	scrollingContainer.append(spellHeader);
	scrollingContainer.append(spellListContainer);
	
	if ('materials' in wand && wand.materials.length > 0) {
		var materialHeader = $('<div class="wandHeading">Materials</div>');
		var materialListContainer = $('<ul/>');
		var wandMaterials = wand.materials;
		wandMaterials.sort();
		for (var materialIndex in wandMaterials)
		{
			var key = wandMaterials[materialIndex];
			materialListContainer.append($('<li/>').append(getMaterial(key)));
		}
		scrollingContainer.append(materialHeader);
		scrollingContainer.append(materialListContainer);
	}
	
	detailsDiv.append(scrollingContainer);
	detailsDiv.append(admin);
	return detailsDiv;
}
 
$(document).ready(function() {
	$("#tabs").tabs();
	if (books.length == 0) {
		$('#booksTab').hide(); 
	} else {
		$("#bookList").selectable({
			selected: function(event, ui) {
				var selected = jQuery(".ui-selected", this);
				var key = selected.prop('id').substr(5);
				$('#bookDetails').empty();
				$('#bookDetails').append(getBookDetails(key));
			}
	    });
	}
    $("#spellList").selectable({
		selected: function(event, ui) {
			var selected = jQuery(".ui-selected", this);
			var key = selected.prop('id').substr(6);
			$('#spellDetails').empty();
			$('#spellDetails').append(getSpellDetails(key));
		}
    });
    $("#craftingList").selectable({
        selected: function(event, ui) {
            var selected = jQuery(".ui-selected", this);
            var key = selected.prop('id').substr(7);
            $('#craftingDetails').empty();
            $('#craftingDetails').append(getRecipeDetails(key));
        }
    });
    $("#enchantingList").selectable({
        selected: function(event, ui) {
            var selected = jQuery(".ui-selected", this);
            var key = selected.prop('id').substr(5);
            $('#enchantingDetails').empty();
            $('#enchantingDetails').append(getPathDetails(key));
        }
    });
    $("#wandList").selectable({
		selected: function(event, ui) {
			var selected = jQuery(".ui-selected", this);
			var key = selected.prop('id').substr(5);
			$('#wandDetails').empty();
			$('#wandDetails').append(getWandDetails(key));
		}
    });
    $("#upgradeList").selectable({
		selected: function(event, ui) {
			var selected = jQuery(".ui-selected", this);
			var key = selected.prop('id').substr(5);
			$('#upgradeDetails').empty();
			$('#upgradeDetails').append(getWandUpgradeDetails(key));
		}
    });

    $("#tabs").show();
});