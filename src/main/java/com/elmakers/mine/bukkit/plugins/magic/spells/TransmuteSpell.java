package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.dao.BlockData;
import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class TransmuteSpell extends Spell
{
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
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
			sendMessage("Nothing to transmute");
			return SpellResult.NO_TARGET;
		}

		ItemStack targetItem = getBuildingMaterial();
		if (targetItem == null)
		{
			sendMessage("Nothing to transmute with");
			return SpellResult.NO_TARGET;
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
			castMessage("You transmute your target structure to " + material.name().toLowerCase());
		}
		else
		{
			castMessage("You transmute your last structure to " + material.name().toLowerCase());
		}

		return SpellResult.SUCCESS;
	}
}
