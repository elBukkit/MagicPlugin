package com.elmakers.mine.bukkit.spell.builtin;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.Target;

public class PushSpell extends TargetingSpell
{
	private int DEFAULT_ITEM_MAGNITUDE = 1;
	private int DEFAULT_ENTITY_MAGNITUDE = 3;
	private int DEFAULT_MAX_ALL_DISTANCE = 20;

	public void forceAll(double mutliplier, boolean pull, int entityMagnitude, int itemMagnitude, int maxAllDistance)
	{
		float maxDistance = (float)maxAllDistance * mage.getRangeMultiplier();
		float maxDistanceSquared = maxDistance * maxDistance;
		
		List<Entity> entities = getWorld().getEntities();
		for (Entity target : entities)
		{
			if (target == getPlayer()) continue;
			if (target.hasMetadata("NPC")) continue;
			Location playerLocation = getLocation();
			Location targetLocation = target.getLocation();

			if (playerLocation.distanceSquared(targetLocation) >maxDistanceSquared) continue;

			Location to = pull ? targetLocation : playerLocation;
			Location from = pull ? playerLocation : targetLocation;

			int magnitude = (target instanceof LivingEntity) ? entityMagnitude : itemMagnitude;
			forceEntity(target, mutliplier, from, to, magnitude);
		}
	}

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
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

		List<Target> targets = getAllTargetEntities();
		if 
		(
			allowAll
			&&  (isLookingDown() || isLookingUp())
		)
		{
			forceAll(multiplier, pull, entityMagnitude, itemMagnitude, maxAllDistance);
			return SpellResult.AREA;
		}

		// Don't deduct costs for not doing anything.
		if (targets.size() == 0)
		{
			return SpellResult.COST_FREE;
		}
		
		int pushed = 0;
		for (Target target : targets) {
			Entity targetEntity = target.getEntity();
			Location to = pull ? target.getLocation() : getLocation();
			Location from = pull ? getLocation() : target.getLocation();
			int magnitude = (target instanceof LivingEntity) ? entityMagnitude : itemMagnitude;

			forceEntity(targetEntity, multiplier, from, to, magnitude);
			pushed++;
			if (count > 0 && pushed >= count) break;
		}
		return SpellResult.CAST;
	}

	protected void forceEntity(Entity target, double multiplier, Location from, Location to, int magnitude)
	{
		// Check for protected Mages
		if (target instanceof Player) {
			Mage targetMage = controller.getMage((Player)target);
			// Check for protected players (admins, generally...)
			if (targetMage.isSuperProtected()) {
				return;
			}
		}
		
		magnitude = (int)((double)magnitude * multiplier);
		Vector toVector = new Vector(to.getBlockX(), to.getBlockY(), to.getBlockZ());
		Vector fromVector = new Vector(from.getBlockX(), from.getBlockY(), from.getBlockZ());
		Vector forceVector = fromVector;
		forceVector.subtract(toVector);
		forceVector.normalize();
		forceVector.multiply(magnitude);
		target.setVelocity(forceVector);
	}
}
