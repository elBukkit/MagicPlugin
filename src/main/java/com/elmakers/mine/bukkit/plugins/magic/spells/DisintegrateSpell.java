package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.EffectTrail;
import com.elmakers.mine.bukkit.utilities.ParticleType;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class DisintegrateSpell extends Spell
{
	private int             playerDamage = 1;
	private int             entityDamage = 100;

    private final static int 		maxEffectRange = 16;
    private final static int 		effectSpeed = 1;
    private final static int 		effectPeriod = 2;
	private final static float 		particleSpeed = 1f;
	private final static int 		particleCount = 6;

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		int effectRange = Math.min(getMaxRange(), maxEffectRange / effectSpeed);
		Location effectLocation = player.getEyeLocation();
		Vector effectDirection = effectLocation.getDirection();
		EffectTrail effectTrail = new EffectTrail(spells.getPlugin(), effectLocation, effectDirection, effectRange);
		effectTrail.setParticleType(ParticleType.LAVA_DRIPPING);
		effectTrail.setParticleCount(particleCount);
		effectTrail.setEffectSpeed(particleSpeed);
		effectTrail.setParticleOffset(0.2f, 0.2f, 0.2f);
		effectTrail.setSpeed(effectSpeed);
		effectTrail.setPeriod(effectPeriod);
		effectTrail.start();
		
		Target target = getTarget();
		if (target == null)
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}
		if (target.isEntity())
		{
			Entity targetEntity = target.getEntity();
			if (targetEntity instanceof LivingEntity)
			{
				LivingEntity li = (LivingEntity)targetEntity;
				if (li instanceof Player)
				{
					li.damage(playerSpells.getPowerMultiplier() * playerDamage, player);
				}
				else
				{
					li.damage(playerSpells.getPowerMultiplier() * entityDamage, player);
				}
				castMessage("ZOT!");
				return SpellResult.SUCCESS;
			}
		}

		if (!target.hasTarget())
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}

		Block targetBlock = target.getBlock();
		BlockList disintigrated = new BlockList();
		disintigrated.add(targetBlock);

		if (!hasBuildPermission(targetBlock)) {
			castMessage("You don't have permission to build here.");
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		if (isUnderwater())
		{
			targetBlock.setType(Material.STATIONARY_WATER);
		}
		else
		{
			targetBlock.setType(Material.AIR);
		}

		spells.addToUndoQueue(player, disintigrated);
		castMessage("ZAP!");

		return SpellResult.SUCCESS;
	}

	@Override
	public void onLoad(ConfigurationNode properties)  
	{
		playerDamage = properties.getInteger("player_damage", playerDamage);
		entityDamage = properties.getInteger("entity_damage", entityDamage);
	}
}
