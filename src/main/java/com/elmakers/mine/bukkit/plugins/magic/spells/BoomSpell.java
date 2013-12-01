package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
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

		Location l = target.getBlock().getLocation();
		player.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), size, incendiary, breakBlocks);

		return SpellResult.SUCCESS;
	}

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		int size = parameters.getInt("size", defaultSize);
		boolean useFire = parameters.getBoolean("fire", false);
		boolean breakBlocks = parameters.getBoolean("break_blocks", true);
		String targetType = (String)parameters.getString("target", "");
		
		size = (int)(playerSpells.getPowerMultiplier() * size);
		
		if (targetType.equals("here"))
		{
			player.damage(player.getMaxHealth() * 10);
			return createExplosionAt(player.getLocation(), size, useFire, breakBlocks);
		}

		Target target = getTarget();
		if (!target.hasTarget())
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}

		return createExplosionAt(target.getLocation(), size, useFire, breakBlocks);
	}
}
