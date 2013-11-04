package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class GrenadeSpell extends Spell
{
	int defaultSize = 6;

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		int size = parameters.getInt("size", defaultSize);
		int fuse = parameters.getInt("fuse", 80);
		boolean useFire = parameters.getBoolean("fire", false);

		Block target = getNextBlock();
		Location loc = target.getLocation();
		TNTPrimed grenade = (TNTPrimed)player.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);

		Vector aim = getAimVector();
		grenade.setVelocity(aim);
		grenade.setYield(size);
		grenade.setFuseTicks(fuse);
		grenade.setIsIncendiary(useFire);

		return SpellResult.SUCCESS;
	}
}
