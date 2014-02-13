package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.blocks.SimpleBlockAction;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class FireSpell extends Spell
{
	private final static int		DEFAULT_RADIUS	= 4;
    
	public class FireAction extends SimpleBlockAction
	{
		public SpellResult perform(Block block)
		{
			if (block.getType() == Material.AIR || block.getType() == Material.FIRE)
			{
				return SpellResult.NO_TARGET;
			}
			Material material = Material.FIRE;

			if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER || block.getType() == Material.ICE || block.getType() == Material.SNOW)
			{
				material = Material.AIR;
			}
			else
			{
				block = block.getRelative(BlockFace.UP);
			}

			super.perform(block);
			block.setType(material);

			return SpellResult.CAST;
		}
	}

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Block target = getTargetBlock();
		if (target == null) 
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}

		if (!hasBuildPermission(target)) 
		{
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		int radius = parameters.getInt("radius", DEFAULT_RADIUS);
		radius = (int)(mage.getRadiusMultiplier() * radius);
		
		FireAction action = new FireAction();

		if (radius <= 1)
		{
			action.perform(target);
		}
		else
		{
			this.coverSurface(target.getLocation(), radius, action);
		}

		mage.registerForUndo(action.getBlocks());
		castMessage("Burned " + action.getBlocks().size() + " blocks");
		controller.updateBlock(target);

		return SpellResult.CAST;
	}

	public int checkPosition(int x, int z, int R)
	{
		return (x * x) +  (z * z) - (R * R);
	}
}
