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
		size = (int)(mage.getRadiusMultiplier() * size);		
		int fuse = parameters.getInt("fuse", 80);
		boolean useFire = parameters.getBoolean("fire", false);

		Block target = getNextBlock();
		if (target == null) {
			return SpellResult.NO_TARGET;
		}
		if (!mage.hasBuildPermission(target)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		Location loc = target.getLocation();
		TNTPrimed grenade = (TNTPrimed)getPlayer().getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
		if (grenade == null) {
			return SpellResult.FAILURE;
		}
		Vector aim = getAimVector();
		grenade.setVelocity(aim);
		grenade.setYield(size);
		grenade.setFuseTicks(fuse);
		grenade.setIsIncendiary(useFire);

		return SpellResult.SUCCESS;
	}
}
