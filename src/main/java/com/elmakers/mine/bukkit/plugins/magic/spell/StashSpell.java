package com.elmakers.mine.bukkit.plugins.magic.spell;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.elmakers.mine.bukkit.plugins.magic.Mage;
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
		if (!target.hasEntity()) return SpellResult.NO_TARGET;
		Entity targetEntity = target.getEntity();
		if (!(targetEntity instanceof HumanEntity)) return SpellResult.NO_TARGET;
		
		Player showPlayer = mage.getPlayer();
		if (showPlayer == null) return SpellResult.PLAYER_REQUIRED;
		String typeString = parameters.getString("type", "ender");

		// Special case for wands
		if (targetEntity instanceof Player && targetEntity != showPlayer) {
			Player targetPlayer = (Player)targetEntity;
			Mage targetMage = controller.getMage(targetPlayer);
			
			if (!mage.isSuperPowered() && targetMage.isSuperProtected()) {
				return SpellResult.NO_TARGET;
			}
			
			if (targetMage != null && targetMage.getActiveWand() != null && typeString.equalsIgnoreCase("inventory")) {
				targetMage.getActiveWand().closeInventory();
			}
		}
		
		// Make sure to close the player's wand
		Wand activeWand = mage.getActiveWand();
		if (activeWand != null) {
			activeWand.closeInventory();
		}
		
		HumanEntity humanTarget = (HumanEntity)targetEntity;
		
		if (typeString.equalsIgnoreCase("inventory")) {
			Inventory inventory = humanTarget.getInventory();
			showPlayer.openInventory(inventory);
		} else {
			Inventory enderInventory = humanTarget.getEnderChest();
			showPlayer.openInventory(enderInventory);
		}
		
		return SpellResult.CAST;
	}
}
