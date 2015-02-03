package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.BlockAction;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

public class BurnAction extends BaseSpellAction implements BlockAction
{
	public SpellResult perform(ConfigurationSection parameters, Block block)
	{
		if (block.getType() == Material.AIR || block.getType() == Material.FIRE || block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
		{
			return SpellResult.NO_TARGET;
		}
		Material material = Material.FIRE;
		if (block.getType() == Material.ICE || block.getType() == Material.SNOW)
		{
			material = Material.AIR;
		}
		else
		{
			block = block.getRelative(BlockFace.UP);
		}
		if (block.getType() == Material.FIRE || block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
		{
			return SpellResult.NO_TARGET;
		}
		updateBlock(block);
		registerForUndo(block);
		MaterialAndData applyMaterial = new MaterialAndData(material);
		applyMaterial.modify(block);

		return SpellResult.CAST;
	}
}
