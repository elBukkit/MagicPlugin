package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class BoomSpell extends Spell {

	protected int defaultSize = 1;

	public SpellResult createExplosionAt(Location target, float size, boolean incendiary)
	{
		if (target == null) 
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}

		player.getWorld().createExplosion(target.getBlock().getLocation(), size, incendiary);

		return SpellResult.SUCCESS;
	}

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		int size = parameters.getInt("size", defaultSize);
		boolean useFire = parameters.getBoolean("fire", false);
		String targetType = (String)parameters.getString("target", "");
		if (targetType.equals("here"))
		{
			player.damage(100);
			return createExplosionAt(player.getLocation(), size, useFire);
		}

		Target target = getTarget();
		if (!target.hasTarget())
		{
			sendMessage(player, "No target");
			return SpellResult.NO_TARGET;
		}

		return createExplosionAt(target.getLocation(), size, useFire);
	}
}
