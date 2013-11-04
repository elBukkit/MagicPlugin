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

public class ForceSpell extends Spell
{
	int itemMagnitude = 1;
	int entityMagnitude = 3;
	int maxAllDistance = 20;
	Entity targetEntity = null;
	boolean allowAll = true;

	public void forceAll(double mutliplier, boolean push)
	{
		List<Entity> entities = player.getWorld().getEntities();
		for (Entity entity : entities)
		{
			if (entity == player) continue;
			Location playerLocation = player.getLocation();

			if (getDistance(playerLocation, entity.getLocation()) > maxAllDistance) continue;

			forceEntity(entity, mutliplier, playerLocation, !push);
		}
	}

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		boolean push = false;
		boolean pull = false;

		if (targetEntity != null)
		{
			if (targetEntity instanceof LivingEntity)
			{
				LivingEntity le = (LivingEntity)targetEntity;
				if (le.isDead())
				{
					targetEntity = null;
				}
				if (targetEntity != null && getDistance(player.getLocation(), targetEntity.getLocation()) > getMaxRange())
				{
					targetEntity = null;
				}
			}
		}

		String typeString = parameters.getString("type", "");
		push = typeString.equals("push");
		pull = typeString.equals("pull");

		double multiplier = parameters.getDouble("size", 1);

		targetEntity(Entity.class);
		Target target = getTarget();

		if 
		(
				(push || pull)
				&&  allowAll
				&&  (target == null || !target.isEntity() || target.isBlock()) 
				&&  (getYRotation() < -60 || getYRotation() > 60)
				)
		{
			if (push)
			{
				castMessage("Get away!");
				forceAll(multiplier, true);
			}
			else
			{
				castMessage("Gimme!");
				forceAll(multiplier, false);
			}
			return SpellResult.SUCCESS;
		}

		if (target == null || !target.hasTarget())
		{
			targetEntity = null;
			return SpellResult.NO_TARGET;
		}

		if (target.isEntity())
		{
			Entity newEntity = target.getEntity();
			if 
			(
					targetEntity == null 
					||  (newEntity instanceof LivingEntity) 
					||  !(targetEntity instanceof LivingEntity)
					)
			{
				targetEntity = newEntity;
				if (!(push || pull)) return SpellResult.COST_FREE;
			}
		}

		if (targetEntity == null)
		{
			return SpellResult.NO_TARGET;
		}

		Location destination = target.getLocation();
		if (pull) destination = player.getLocation();
		forceEntity(targetEntity, multiplier, destination, push);

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

	protected void forceEntity(Entity target, double multiplier, Location destination, boolean useAim)
	{
		int magnitude = (target instanceof LivingEntity) ? entityMagnitude : itemMagnitude;
		magnitude = (int)((double)magnitude * multiplier);
		Vector targetLoc = new Vector(target.getLocation().getBlockX(), target.getLocation().getBlockY(), target.getLocation().getBlockZ());
		Vector destinationLoc = new Vector(destination.getBlockX(), destination.getBlockY(), destination.getBlockZ());
		Vector forceVector = destinationLoc;
		if (useAim)
		{
			forceVector = getAimVector();
		}
		else
		{
			forceVector.subtract(targetLoc);                   
		}
		forceVector.normalize();
		forceVector.multiply(magnitude);

		target.setVelocity(forceVector);
	}

	@Override
	public boolean onCancel()
	{
		if (targetEntity != null)
		{
            if 
            (
                    (targetEntity instanceof LivingEntity) 
            &&      !targetEntity.isDead() 
            &&      getDistance(player.getLocation(), targetEntity.getLocation()) < getMaxRange()
            )
            {
                castMessage("Released target");
            }

            targetEntity = null;
			return true;
		}
		
		return false;
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
