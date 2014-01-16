package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.List;

import org.bukkit.Color;
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
	private int DEFAULT_ITEM_MAGNITUDE = 1;
	private int DEFAULT_ENTITY_MAGNITUDE = 3;
	private int DEFAULT_MAX_ALL_DISTANCE = 20;
	
	// Maybe make these configurable for custom effects?
    private final static int effectSpeed = 2;
    private final static int effectPeriod = 2;
    private final static int ringEffectAmount = 8;
    private final static int maxEffectRange = 16;
    private final static int maxRingEffectRange = 6;

	public void forceAll(double mutliplier, boolean pull, int entityMagnitude, int itemMagnitude, int maxAllDistance)
	{
		float maxDistance = (float)maxAllDistance * mage.getRangeMultiplier();
		float maxDistanceSquared = maxDistance * maxDistance;
		
		// Visual effect
		int effectRange = Math.min((int)maxRingEffectRange, maxEffectRange / effectSpeed);
		Location effectLocation = player.getLocation();
		EffectRing effectRing = new EffectRing(controller.getPlugin(), effectLocation, effectRange, ringEffectAmount);
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

			int magnitude = (target instanceof LivingEntity) ? entityMagnitude : itemMagnitude;
			forceEntity(target, mutliplier, from, to, magnitude);
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
		if (push) {
			multiplier *= mage.getDamageMultiplier();
		}
		int count = parameters.getInt("count", 0);
		
		boolean allowAll = parameters.getBoolean("allow_area", true);
		int itemMagnitude = parameters.getInt("item_force", DEFAULT_ITEM_MAGNITUDE);
		int entityMagnitude = parameters.getInt("entity_force", DEFAULT_ENTITY_MAGNITUDE);
		int maxAllDistance = parameters.getInt("area_range", DEFAULT_MAX_ALL_DISTANCE);

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
			forceAll(multiplier, pull, entityMagnitude, itemMagnitude, maxAllDistance);
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
		EffectTrail effectTrail = new EffectTrail(controller.getPlugin(), effectLocation, effectDirection, effectRange);
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
			int magnitude = (target instanceof LivingEntity) ? entityMagnitude : itemMagnitude;

			forceEntity(targetEntity, multiplier, from, to, magnitude);
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

	protected void forceEntity(Entity target, double multiplier, Location from, Location to, int magnitude)
	{
		magnitude = (int)((double)magnitude * multiplier);
		Vector toVector = new Vector(to.getBlockX(), to.getBlockY(), to.getBlockZ());
		Vector fromVector = new Vector(from.getBlockX(), from.getBlockY(), from.getBlockZ());
		Vector forceVector = fromVector;
		forceVector.subtract(toVector);
		forceVector.normalize();
		forceVector.multiply(magnitude);
		target.setVelocity(forceVector);
	}
	
	protected void startEffect(EffectPlayer effect, int effectRange) {
		effect.setParticleType(ParticleType.SPELL);
		effect.setParticleCount(3);
		Color effectColor = mage.getEffectColor();
		effect.setEffectData(effectColor != null ? effectColor.asRGB() : 2);
		effect.setParticleOffset(0.2f, 0.2f, 0.2f);
		effect.setSpeed(effectSpeed);
		effect.setPeriod(effectPeriod);
		effect.start();
	}
}
