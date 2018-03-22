package com.elmakers.mine.bukkit.spell.builtin;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.Target;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;

public class OcelotSpell extends TargetingSpell
{
	private static int DEFAULT_MAX_OCELOTS = 30;

	protected List<Ocelot> ocelots = new ArrayList<>();

	@Nullable public Ocelot newOcelot(Target target)
	{
		Block targetBlock = target.getBlock();
		if (targetBlock == null)
		{
			return null;
		}
		targetBlock = targetBlock.getRelative(BlockFace.UP);
		if (target.hasEntity())
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
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target target = getTarget();
		ArrayList<Ocelot> newocelots = new ArrayList<>();

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
        Player player = mage.getPlayer();
        if (player != null) {
            Ocelot.setOwner(player);
        }
	}
}
