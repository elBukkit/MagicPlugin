package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.blocks.BlockData;
import com.elmakers.mine.bukkit.blocks.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.BrushSpell;
import com.elmakers.mine.bukkit.plugins.magic.MaterialBrush;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class TransmuteSpell extends BrushSpell
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
			transmuteAction = controller.getLastBlockList(getPlayer().getName(), target);
			usedTarget = transmuteAction != null;
		}

		if (transmuteAction == null)
		{
			transmuteAction = controller.getLastBlockList(getPlayer().getName());
		}

		if (transmuteAction == null)
		{
			sendMessage("Nothing to transmute");
			return SpellResult.NO_TARGET;
		}

		MaterialBrush buildWith = getMaterialBrush();
		Material material = buildWith.getMaterial();
		byte data = buildWith.getData();
		
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
