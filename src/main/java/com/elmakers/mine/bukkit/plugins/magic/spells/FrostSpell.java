package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.EffectTrail;
import com.elmakers.mine.bukkit.utilities.ParticleType;
import com.elmakers.mine.bukkit.utilities.SimpleBlockAction;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class FrostSpell extends Spell
{
	private int				defaultRadius			= 2;
	private int				verticalSearchDistance	= 8;
	private int             timeToLive = 60000;
	private int             playerDamage = 1;
	private int             entityDamage = 10;
	private int				slowness = 1;
	private int				slownessDuration = 200;
	
    private final static int 		maxEffectRange = 16;
    private final static int 		effectSpeed = 1;
    private final static float 		particleSpeed = 0.1f;
    private final static int 		effectPeriod = 2;
    private final static int 		particleCount = 8;

	public class FrostAction extends SimpleBlockAction
	{
		public SpellResult perform(Block block)
		{
			if (block.getType() == Material.AIR || block.getType() == Material.SNOW)
			{
				return SpellResult.NO_TARGET;
			}
			Material material = Material.SNOW;
			if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
			{
				material = Material.ICE;
			}
			else if (block.getType() == Material.LAVA)
			{
				material = Material.COBBLESTONE;
			}
			else if (block.getType() == Material.STATIONARY_LAVA)
			{
				material = Material.OBSIDIAN;
			}
			else if (block.getType() == Material.FIRE)
			{
				material = Material.AIR;
			}
			else
			{
				block = block.getRelative(BlockFace.UP);
			}
			super.perform(block);
			block.setType(material);
			return SpellResult.SUCCESS;
		}
	}

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		int effectRange = Math.min(getMaxRange(), maxEffectRange);
		Location effectLocation = player.getEyeLocation();
		Vector effectDirection = effectLocation.getDirection();
		EffectTrail effectTrail = new EffectTrail(spells.getPlugin(), effectLocation, effectDirection, effectRange);
		effectTrail.setParticleType(ParticleType.SNOWBALL_POOF);
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
				if (slowness > 0) {
					PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, slownessDuration, slowness, false);
					li.addPotionEffect(effect);
				}
				if (li instanceof Player)
				{
					li.damage(playerDamage, player);
				}
				else
				{
					li.damage(entityDamage, player);
				}
			}
		}

		if (!target.hasTarget())
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(target.getBlock())) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		int radius = parameters.getInt("radius", defaultRadius);
		radius = (int)(playerSpells.getPowerMultiplier() * radius);		
		FrostAction action = new FrostAction();

		if (radius <= 1)
		{
			action.perform(target.getBlock());
		}
		else
		{
			this.coverSurface(target.getLocation(), radius, action);
		}


		BlockList frozenBlocks = action.getBlocks();
		frozenBlocks.setTimeToLive(timeToLive);
		spells.scheduleCleanup(player.getName(), frozenBlocks);
		castMessage("Frosted " + action.getBlocks().size() + " blocks");
		spells.updateBlock(target.getBlock());

		return SpellResult.SUCCESS;
	}

	public int checkPosition(int x, int z, int R)
	{
		return (x * x) +  (z * z) - (R * R);
	}	

	@Override
	public void onLoadTemplate(ConfigurationNode properties)  
	{
		noTargetThrough(Material.WATER);
		noTargetThrough(Material.STATIONARY_WATER);
		playerDamage = properties.getInteger("player_damage", playerDamage);
		entityDamage = properties.getInteger("entity_damage", entityDamage);
		defaultRadius = properties.getInteger("radius", defaultRadius);
		verticalSearchDistance = properties.getInteger("vertical_search_distance", verticalSearchDistance);
		timeToLive = properties.getInt("duration", timeToLive);
		slowness = properties.getInt("slowness", slowness);
		slownessDuration = properties.getInt("slowness_duration", slownessDuration);
	}
}
