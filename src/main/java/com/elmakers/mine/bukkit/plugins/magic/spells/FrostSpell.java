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

import com.elmakers.mine.bukkit.blocks.BlockList;
import com.elmakers.mine.bukkit.blocks.SimpleBlockAction;
import com.elmakers.mine.bukkit.effects.EffectTrail;
import com.elmakers.mine.bukkit.effects.ParticleType;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class FrostSpell extends Spell
{
	private static final int			DEFAULT_RADIUS			= 2;
	private static final int            DEFAULT_TIME_TO_LIVE = 60000;
	private static final int            DEFAULT_PLAYER_DAMAGE = 1;
	private static final int            DEFALT_ENTITY_DAMAGE = 10;
	private static final int			DEFAULT_SLOWNESS = 1;
	private static final int			DEFAULT_DURATION = 200;
	
    private final static int 		maxEffectRange = 16;
    private final static int 		effectSpeed = 1;
    private final static float 		particleData = 0.1f;
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
		noTargetThrough(Material.WATER);
		noTargetThrough(Material.STATIONARY_WATER);
		
		int effectRange = Math.min(getMaxRange(), maxEffectRange);
		Location effectLocation = getPlayer().getEyeLocation();
		Vector effectDirection = effectLocation.getDirection();
		EffectTrail effectTrail = new EffectTrail(controller.getPlugin(), effectLocation, effectDirection, effectRange);
		effectTrail.setParticleType(ParticleType.SNOWBALL_POOF);
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
		int entityDamage = parameters.getInteger("entity_damage", DEFALT_ENTITY_DAMAGE);
		int defaultRadius = parameters.getInteger("radius", DEFAULT_RADIUS);
		int timeToLive = parameters.getInt("duration", DEFAULT_TIME_TO_LIVE);
		int slowness = parameters.getInt("slowness", DEFAULT_SLOWNESS);
		int slownessDuration = parameters.getInt("slowness_duration", DEFAULT_DURATION);

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
					li.damage(playerDamage, getPlayer());
				}
				else
				{
					li.damage(entityDamage, getPlayer());
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
		radius = (int)(mage.getRadiusMultiplier() * radius);		
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
		controller.scheduleCleanup(getPlayer().getName(), frozenBlocks);
		castMessage("Frosted " + action.getBlocks().size() + " blocks");
		controller.updateBlock(target.getBlock());

		return SpellResult.SUCCESS;
	}

	public int checkPosition(int x, int z, int R)
	{
		return (x * x) +  (z * z) - (R * R);
	}
}
