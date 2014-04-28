package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.SimpleBlockAction;
import com.elmakers.mine.bukkit.spell.BlockSpell;

public class FireSpell extends BlockSpell
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
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Block target = getTargetBlock();
		if (target == null) 
		{
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

		registerForUndo(action.getBlocks());
		controller.updateBlock(target);

		return SpellResult.CAST;
	}

	public int checkPosition(int x, int z, int R)
	{
		return (x * x) +  (z * z) - (R * R);
	}
}
