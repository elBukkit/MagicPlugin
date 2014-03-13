package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class StashSpell extends Spell
{
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Target target = getTarget();
		if (!target.isEntity()) return SpellResult.NO_TARGET;
		Entity targetEntity = target.getEntity();
		if (!(targetEntity instanceof HumanEntity)) return SpellResult.NO_TARGET;
		
		Player showPlayer = mage.getPlayer();
		if (showPlayer == null) return SpellResult.PLAYER_REQUIRED;
		if (target instanceof Player) {
			Player playerTarget = (Player)target;
			if (playerTarget == mage.getPlayer()) {
				castMessage("You peek into your ender chest");
			} else {
				castMessage("You peek into " + playerTarget.getName() + "'s ender chest");
			}
		}

		// Make sure to close the player's wand
		Wand activeWand = mage.getActiveWand();
		if (activeWand != null) {
			activeWand.closeInventory();
		}
		
		HumanEntity humanTarget = (HumanEntity)targetEntity;
		Inventory enderInventory = humanTarget.getEnderChest();
		showPlayer.openInventory(enderInventory);
		
		return SpellResult.CAST;
	}
}
