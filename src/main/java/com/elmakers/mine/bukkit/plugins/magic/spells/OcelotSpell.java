package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class OcelotSpell extends Spell
{
	private static int DEFAULT_MAX_OCELOTS = 30;

	protected List<Ocelot> ocelots = new ArrayList<Ocelot>();

	public Ocelot newOcelot(Target target)
	{
		if (target == null)
		{
			return null;
		}

		Block targetBlock = target.getBlock();
		if (targetBlock == null)
		{
			return null;
		}
		targetBlock = targetBlock.getRelative(BlockFace.UP);
		if (target.isEntity())
		{      
			targetBlock = targetBlock.getRelative(BlockFace.SOUTH);
		}

		Ocelot entity = (Ocelot)getWorld().spawnEntity(targetBlock.getLocation(), EntityType.OCELOT);
		if (entity == null)
		{
			return null;
		}
		tameOcelot(entity);
		return entity;
	}

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Target target = getTarget();
		if (target == null)
		{
			return SpellResult.NO_TARGET;
		}

		ArrayList<Ocelot> newocelots = new ArrayList<Ocelot>();

		for (Ocelot Ocelot : ocelots)
		{
			if (!Ocelot.isDead())
			{
				newocelots.add(Ocelot);
			}
		}

		ocelots = newocelots;
		
		int maxOcelots = parameters.getInt("max_ocelots", DEFAULT_MAX_OCELOTS);
		int scaledMaxOcelots = (int)(mage.getRadiusMultiplier() * maxOcelots);
		if (ocelots.size() >= scaledMaxOcelots) 
		{
			Ocelot killOcelot = ocelots.remove(0);
			killOcelot.setHealth(0);
		}

		Ocelot Ocelot = newOcelot(target);
		if (Ocelot == null)
		{
			return SpellResult.FAIL;
		}

		ocelots.add(Ocelot);

		Entity e = target.getEntity();
		if (e != null && e instanceof LivingEntity)
		{
			LivingEntity targetEntity = (LivingEntity)e;
			for (Ocelot w : ocelots)
			{
				w.setTarget(targetEntity);
			}
		}

		return SpellResult.CAST;
	}

	protected void tameOcelot(Ocelot Ocelot)
	{
		Ocelot.setHealth(8);
		Ocelot.setTamed(true);
		Ocelot.setOwner(getPlayer());
	}
}
