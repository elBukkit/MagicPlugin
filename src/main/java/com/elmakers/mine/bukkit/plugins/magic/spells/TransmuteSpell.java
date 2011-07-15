package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.dao.BlockData;
import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;

public class TransmuteSpell extends Spell
{
	@Override
	public boolean onCast(Map<String, Object> parameters)
	{	
		BlockList transmuteAction = null;

		/*
		 * Use target if targeting
		 */
		boolean usedTarget = false;
		targetThrough(Material.GLASS);
		Block target = getTargetBlock();
		
		if (target != null)
		{
			transmuteAction = spells.getLastBlockList(player.getName(), target);
			usedTarget = transmuteAction != null;
		}

		if (transmuteAction == null)
		{
			transmuteAction = spells.getLastBlockList(player.getName());
		}
		
		if (transmuteAction == null)
		{
			sendMessage(player, "Nothing to transmute");
			return false;
		}
		
		ItemStack targetItem = getBuildingMaterial();
		if (targetItem == null)
		{
			sendMessage(player, "Nothing to transmute with");
			return false;
		}
		
		Material material = targetItem.getType();
		byte data = getItemData(targetItem);
				
		for (BlockData undoBlock : transmuteAction)
		{
			Block block = undoBlock.getBlock();
			block.setType(material);
			block.setData(data);
		}
		
		if (usedTarget)
		{
			castMessage(player, "You transmute your target structure to " + material.name().toLowerCase());
		}
		else
		{
			castMessage(player, "You transmute your last structure to " + material.name().toLowerCase());
		}
		
		return true;
	}
}
