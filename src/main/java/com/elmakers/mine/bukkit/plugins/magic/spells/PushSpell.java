package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.EffectPlayer;
import com.elmakers.mine.bukkit.utilities.EffectRing;
import com.elmakers.mine.bukkit.utilities.EffectTrail;
import com.elmakers.mine.bukkit.utilities.ParticleType;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class PushSpell extends Spell
{
	private int itemMagnitude = 1;
	private int entityMagnitude = 3;
	private int maxAllDistance = 20;
	private boolean allowAll = true;
	
	// Maybe make these configurable for custom effects?
    private final static int effectSpeed = 2;
    private final static int effectPeriod = 2;
    private final static int ringEffectAmount = 8;
    private final static int maxEffectRange = 16;
    private final static int maxRingEffectRange = 6;

	public void forceAll(double mutliplier, boolean pull)
	{
		float maxDistance = (float)maxAllDistance * playerSpells.getPowerMultiplier();
		float maxDistanceSquared = maxDistance * maxDistance;
		
		// Visual effect
		int effectRange = Math.min((int)maxRingEffectRange, maxEffectRange / effectSpeed);
		Location effectLocation = player.getLocation();
		EffectRing effectRing = new EffectRing(spells.getPlugin(), effectLocation, effectRange, ringEffectAmount);
		if (pull) effectRing.setInvert(true);
		startEffect(effectRing, effectRange);
		
		List<Entity> entities = player.getWorld().getEntities();
		for (Entity target : entities)
		{
			if (target == player) continue;
			Location playerLocation = player.getLocation();
			Location targetLocation = target.getLocation();

			if (playerLocation.distanceSquared(targetLocation) >maxDistanceSquared) continue;

			Location to = pull ? targetLocation : playerLocation;
			Location from = pull ? playerLocation : targetLocation;
			
			forceEntity(target, mutliplier, from, to);
		}
	}

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		boolean push = false;
		boolean pull = false;

		String typeString = parameters.getString("type", "");
		push = typeString.equals("push");
		pull = typeString.equals("pull");

		double multiplier = parameters.getDouble("size", 1);
		multiplier *= playerSpells.getPowerMultiplier();
		int count = parameters.getInt("count", 0);

		targetEntity(Entity.class);
		List<Target> targets = getAllTargetEntities();
		if 
		(
			allowAll
			&&  (getYRotation() < -80 || getYRotation() > 80)
		)
		{
			if (push)
			{
				castMessage("Get away!");
			}
			else
			{
				castMessage("Gimme!");
			}
			forceAll(multiplier, pull);
			return SpellResult.SUCCESS;
		}

		// Visual effect
		int effectRange = Math.min(getMaxRange(), maxEffectRange / effectSpeed);
		Location effectLocation = player.getEyeLocation();
		Vector effectDirection = effectLocation.getDirection();
		if (pull) {
			effectDirection.normalize();
			effectDirection.multiply(effectSpeed * effectRange);
			effectLocation.add(effectDirection);
			effectDirection.multiply(-1);
		}
		EffectTrail effectTrail = new EffectTrail(spells.getPlugin(), effectLocation, effectDirection, effectRange);
		startEffect(effectTrail, effectRange);

		// Don't deduct costs for not doing anything but still show the effect.
		if (targets.size() == 0)
		{
			return SpellResult.NO_TARGET;
		}
		
		int pushed = 0;
		for (Target target : targets) {
			Entity targetEntity = target.getEntity();
			Location to = pull ? target.getLocation() : player.getLocation();
			Location from = pull ? player.getLocation() : target.getLocation();
			forceEntity(targetEntity, multiplier, from, to);
			pushed++;
			if (count > 0 && pushed >= count) break;
		}

		if (pull)
		{
			castMessage("Yoink!");
		}
		else
		{
			castMessage("Shove!");
		}
		return SpellResult.SUCCESS;
	}

	protected void forceEntity(Entity target, double multiplier, Location from, Location to)
	{
		int magnitude = (target instanceof LivingEntity) ? entityMagnitude : itemMagnitude;
		magnitude = (int)((double)magnitude * multiplier);
		Vector toVector = new Vector(to.getBlockX(), to.getBlockY(), to.getBlockZ());
		Vector fromVector = new Vector(from.getBlockX(), from.getBlockY(), from.getBlockZ());
		Vector forceVector = fromVector;
		forceVector.subtract(toVector);
		forceVector.normalize();
		forceVector.multiply(magnitude);
		target.setVelocity(forceVector);
	}

	@Override
	public void onLoad(ConfigurationNode properties)  
	{
		itemMagnitude = properties.getInt("item_force", itemMagnitude);
		entityMagnitude = properties.getInt("entity_force", entityMagnitude);
		allowAll = properties.getBoolean("allow_area", allowAll);
		maxAllDistance = properties.getInt("area_range", maxAllDistance);
	}
	
	protected void startEffect(EffectPlayer effect, int effectRange) {
		effect.setParticleType(ParticleType.SPELL);
		effect.setParticleCount(3);
		effect.setEffectSpeed(2);
		effect.setParticleOffset(0.2f, 0.2f, 0.2f);
		effect.setSpeed(effectSpeed);
		effect.setPeriod(effectPeriod);
		effect.start();
	}
}
