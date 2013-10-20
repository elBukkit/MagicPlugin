package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.elmakers.mine.bukkit.plugins.magic.PlayerSpells;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.Wand;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class AbsorbSpell extends Spell 
{
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCast(ConfigurationNode parameters) 
	{
		Material material = Material.AIR;
		List<Material> buildingMaterials = spells.getBuildingMaterials();

		material = parameters.getMaterial("material", material);
		byte data = 0;
		boolean success = false;
		if (material == Material.AIR || !buildingMaterials.contains(material))
		{
			if (!isUnderwater())
			{
				noTargetThrough(Material.STATIONARY_WATER);
				noTargetThrough(Material.WATER);
			}
			Block target = getTargetBlock();
	
			if (target == null) 
			{
				castMessage(player, "No target");
				return false;
			}
	
			material = target.getType();
			data = target.getData();
		}
		
		// Do some special hacky stuff for the wand.
		Wand wand = Wand.getActiveWand(player);
		PlayerSpells playerSpells = spells.getPlayerSpells(player);
		if (wand != null) {
			// Make sure there's room in the inventory.
			int targetSlot = 8;
			PlayerInventory inventory = player.getInventory();
			if (inventory.getHeldItemSlot() == 8) {
				targetSlot = 7;
			}
			
			// Clear this slot directly and save the inventory
			ItemStack currentItem = inventory.getItem(targetSlot);
			if (currentItem != null && currentItem.getType() != Material.AIR) {
				// Make sure there's room in the inventory if we need to save this item.
				if (Wand.isSpell(currentItem) || currentItem.getType() == Wand.EraseMaterial) {
					// Not a great way to do this.
					ItemStack[] contents = inventory.getContents();
					boolean full = true;
					for (ItemStack stack : contents) {
						if (stack == null || stack.getType() == Material.AIR) {
							full = false; 
							break;
						}
					}
					if (full) {
						castMessage(player, "Your wand is full!");
						return false;
					}
				}
				inventory.setItem(targetSlot, new ItemStack(Material.AIR, 1));
				wand.saveInventory(playerSpells);
				addMaterialToWand(material, data);
				wand.updateInventory(playerSpells);
				if (Wand.isSpell(currentItem) || currentItem.getType() == Wand.EraseMaterial) {
					inventory.addItem(currentItem);
				}
			}
		} else {
			
		}
		
		if (!success) {
			success = addMaterialToWand(material, data);
		}
		
		if (success) {
			castMessage(player, "Absorbing some " + material.name().toLowerCase());
		} else {
			castMessage(player, "Failed to absorb");
		}
		return success;
	}
}
