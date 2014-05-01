package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.elmakers.mine.bukkit.utility.Target;

public class BoomSpell extends BlockSpell {

	protected int defaultSize = 1;

	public SpellResult createExplosionAt(Location target, float size, boolean incendiary, boolean breakBlocks)
	{
		if (target == null) 
		{
			return SpellResult.NO_TARGET;
		}

		Block block = target.getBlock();
		if ((breakBlocks || incendiary) && !hasBuildPermission(block)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		Location l = block.getLocation();
		Player player = getPlayer();
		registerForUndo();
		NMSUtils.createExplosion(player, getWorld(), l.getX(), l.getY(), l.getZ(), size, incendiary, breakBlocks);
		controller.updateBlock(block);
		return SpellResult.CAST;
	}

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		int size = parameters.getInt("size", defaultSize);
		boolean useFire = parameters.getBoolean("fire", false);
		boolean breakBlocks = parameters.getBoolean("break_blocks", true);
		
		size = (int)(mage.getRadiusMultiplier() * size);

		Target target = getTarget();
		if (!target.hasTarget())
		{
			return SpellResult.NO_TARGET;
		}

		return createExplosionAt(target.getLocation(), size, useFire, breakBlocks);
	}
}
