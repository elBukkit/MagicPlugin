package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.blocks.BlockList;
import com.elmakers.mine.bukkit.effects.EffectTrail;
import com.elmakers.mine.bukkit.effects.ParticleType;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class DisintegrateSpell extends Spell
{
	private final static int             DEFAULT_PLAYER_DAMAGE = 1;
	private final static int             DEFAULT_ENTITY_DAMAGE = 100;

    private final static int 		maxEffectRange = 16;
    private final static int 		effectSpeed = 1;
    private final static int 		effectPeriod = 2;
	private final static float 		particleData = 1f;
	private final static int 		particleCount = 6;

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		int effectRange = Math.min(getMaxRange(), maxEffectRange / effectSpeed);
		Location effectLocation = getPlayer().getEyeLocation();
		Vector effectDirection = effectLocation.getDirection();
		EffectTrail effectTrail = new EffectTrail(controller.getPlugin(), effectLocation, effectDirection, effectRange);
		effectTrail.setParticleType(ParticleType.LAVA_DRIPPING);
		effectTrail.setParticleCount(particleCount);
		effectTrail.setEffectData(particleData);
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
		
		int playerDamage = parameters.getInteger("player_damage", DEFAULT_PLAYER_DAMAGE);
		int entityDamage = parameters.getInteger("entity_damage", DEFAULT_ENTITY_DAMAGE);

		if (target.isEntity())
		{
			Entity targetEntity = target.getEntity();
			if (targetEntity instanceof LivingEntity)
			{
				LivingEntity li = (LivingEntity)targetEntity;
				if (li instanceof Player)
				{
					li.damage(mage.getDamageMultiplier() * playerDamage, getPlayer());
				}
				else
				{
					li.damage(mage.getDamageMultiplier() * entityDamage, getPlayer());
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
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		if (isIndestructible(targetBlock)) {
			return SpellResult.NO_TARGET;
		}
		if (isUnderwater())
		{
			targetBlock.setType(Material.STATIONARY_WATER);
		}
		else
		{
			targetBlock.setType(Material.AIR);
		}

		controller.addToUndoQueue(getPlayer(), disintigrated);
		castMessage("ZAP!");

		return SpellResult.SUCCESS;
	}
}
