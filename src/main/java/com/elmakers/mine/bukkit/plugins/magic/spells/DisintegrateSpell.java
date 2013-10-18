package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class DisintegrateSpell extends Spell
{
	private int             playerDamage = 1;
	private int             entityDamage = 100;

	@Override
	public boolean onCast(ConfigurationNode parameters) 
	{
		Target target = getTarget();
		if (target == null)
		{
			castMessage(player, "No target");
			return false;
		}
		if (target.isEntity())
		{
			Entity targetEntity = target.getEntity();
			if (targetEntity instanceof LivingEntity)
			{
				LivingEntity li = (LivingEntity)targetEntity;
				if (li instanceof Player)
				{
					li.damage(playerDamage);
				}
				else
				{
					li.damage(entityDamage);
				}
				castMessage(player, "ZOT!");
				return true;
			}
		}

		if (!target.hasTarget())
		{
			castMessage(player, "No target");
			return false;
		}

		Block targetBlock = target.getBlock();
		BlockList disintigrated = new BlockList();
		disintigrated.add(targetBlock);

		if (isUnderwater())
		{
			targetBlock.setType(Material.STATIONARY_WATER);
		}
		else
		{
			targetBlock.setType(Material.AIR);
		}

		spells.addToUndoQueue(player, disintigrated);
		castMessage(player, "ZAP!");

		return true;
	}

	@Override
	public void onLoad(ConfigurationNode properties)  
	{
		playerDamage = properties.getInteger("player_damage", playerDamage);
		entityDamage = properties.getInteger("entity_damage", entityDamage);
	}
}
