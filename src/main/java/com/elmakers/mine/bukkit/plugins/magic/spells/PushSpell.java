package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class PushSpell extends Spell
{
	int itemMagnitude = 1;
	int entityMagnitude = 3;
	int maxAllDistance = 20;
	boolean allowAll = true;

	public void forceAll(double mutliplier, boolean pull)
	{
		List<Entity> entities = player.getWorld().getEntities();
		for (Entity target : entities)
		{
			if (target == player) continue;
			Location playerLocation = player.getLocation();
			Location targetLocation = target.getLocation();

			if (playerLocation.distanceSquared(targetLocation) > maxAllDistance * maxAllDistance) continue;

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
		int count = parameters.getInt("count", 0);

		targetEntity(Entity.class);
		List<Target> targets = getAllTargetEntities();
		if 
		(
			allowAll
			&&  (targets.size() == 0) 
			&&  (getYRotation() < -60 || getYRotation() > 60)
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
}
