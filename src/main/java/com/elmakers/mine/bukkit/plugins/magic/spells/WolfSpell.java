package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class WolfSpell extends Spell
{
	private static int DEFAULT_MAX_WOLVES = 5;
	protected List<Wolf> wolves = new ArrayList<Wolf>();

	public Wolf newWolf(Target target)
	{
		if (target == null)
		{
			castMessage("No target");
			return null;
		}

		Block targetBlock = target.getBlock();
		targetBlock = targetBlock.getRelative(BlockFace.UP);
		if (target.isEntity())
		{      
			targetBlock = targetBlock.getRelative(BlockFace.SOUTH);
		}

		Wolf entity = (Wolf)player.getWorld().spawnEntity(targetBlock.getLocation(), EntityType.WOLF);
		if (entity == null)
		{
			sendMessage("Your wolfie is DOA");
			return null;
		}
		tameWolf(entity);
		castMessage("You summon a wolfie!");
		return entity;
	}

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		this.targetEntity(LivingEntity.class);
		Target target = getTarget();
		if (target == null)
		{
			return SpellResult.NO_TARGET;
		}

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

		return SpellResult.SUCCESS;
	}

	protected void tameWolf(Wolf wolfie)
	{
		wolfie.setAngry(false);
		wolfie.setHealth(8);
		wolfie.setTamed(true);
		wolfie.setOwner(player);
	}
}
