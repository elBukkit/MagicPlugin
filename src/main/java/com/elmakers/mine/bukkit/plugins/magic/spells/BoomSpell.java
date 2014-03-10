package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class BoomSpell extends Spell {

	protected int defaultSize = 1;

	public SpellResult createExplosionAt(Location target, float size, boolean incendiary, boolean breakBlocks)
	{
		if (target == null) 
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}

		Block block = target.getBlock();
		if (!hasBuildPermission(block)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		Location l = block.getLocation();
		getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), size, incendiary, breakBlocks);
		controller.updateBlock(block);
		return SpellResult.CAST;
	}

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		int size = parameters.getInt("size", defaultSize);
		boolean useFire = parameters.getBoolean("fire", false);
		boolean breakBlocks = parameters.getBoolean("break_blocks", true);
		
		size = (int)(mage.getRadiusMultiplier() * size);

		Target target = getTarget();
		if (!target.hasTarget())
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}

		return createExplosionAt(target.getLocation(), size, useFire, breakBlocks);
	}
}
