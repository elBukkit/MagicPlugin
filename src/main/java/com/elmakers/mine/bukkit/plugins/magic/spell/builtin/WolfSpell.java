package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utilities.Target;

public class WolfSpell extends TargetingSpell
{
	private static int DEFAULT_MAX_WOLVES = 5;
	protected List<Wolf> wolves = new ArrayList<Wolf>();

	public Wolf newWolf(Target target)
	{
		Block targetBlock = target.getBlock();
		if (targetBlock == null) {
			return null;
		}
		targetBlock = targetBlock.getRelative(BlockFace.UP);
		if (target.hasEntity())
		{      
			targetBlock = targetBlock.getRelative(BlockFace.SOUTH);
		}

		Wolf entity = (Wolf)getWorld().spawnEntity(targetBlock.getLocation(), EntityType.WOLF);
		if (entity == null)
		{
			return null;
		}
		tameWolf(entity);
		return entity;
	}

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target target = getTarget();
		ArrayList<Wolf> newWolves = new ArrayList<Wolf>();

		for (Wolf wolf : wolves)
		{
			if (!wolf.isDead())
			{
				newWolves.add(wolf);
			}
		}

		wolves = newWolves;
		
		int maxWolves = parameters.getInt("max_wolves", DEFAULT_MAX_WOLVES);
		int scaledMaxWolves = (int)(mage.getRadiusMultiplier() * maxWolves);
		if (wolves.size() >= scaledMaxWolves) 
		{
			Wolf killWolf = wolves.remove(0);
			killWolf.setHealth(0);
		}

		Wolf wolf = newWolf(target);
		if (wolf == null)
		{
			return SpellResult.NO_TARGET;
		}

		wolves.add(wolf);

		Entity e = target.getEntity();
		if (e != null && e instanceof LivingEntity)
		{
			LivingEntity targetEntity = (LivingEntity)e;
			for (Wolf w : wolves)
			{
				w.setTarget(targetEntity);
				w.setAngry(true);
			}
		}

		return SpellResult.CAST;
	}

	protected void tameWolf(Wolf wolfie)
	{
		wolfie.setAngry(false);
		wolfie.setHealth(8);
		wolfie.setTamed(true);
		wolfie.setOwner(getPlayer());
	}
}
