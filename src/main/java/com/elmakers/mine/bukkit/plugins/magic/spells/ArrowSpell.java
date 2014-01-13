package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class ArrowSpell extends Spell
{ 
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		if (!hasBuildPermission(player.getLocation())) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		int arrowCount = 1;
		arrowCount = parameters.getInt("count", arrowCount);
		boolean useFire = parameters.getBoolean("fire", false);

		float speed = 0.6f;
		float spread = 12f;
		double damage = 2.0;
		
		speed = (float)parameters.getDouble("speed", speed);
		spread = (float)parameters.getDouble("spread", spread);
		damage = parameters.getDouble("damage", damage);
		
		// Modify with wand power
		float powerMultiplier = playerSpells.getPowerMultiplier();
		speed *= powerMultiplier;
		damage *= powerMultiplier;
		spread /= powerMultiplier;
		Vector direction = player.getLocation().getDirection();
		for (int ai = 0; ai < arrowCount; ai++)
		{
			Arrow arrow = null;
			Location location = player.getLocation();
			location.setX(location.getX() + direction.getX() * (1 + Math.random() * arrowCount));
			location.setY(location.getY() + 1.5f);
			location.setZ(location.getZ() + direction.getZ() * (1 + Math.random() * arrowCount));
			
			arrow = player.getWorld().spawnArrow(location, direction, speed, spread);

			if (arrow == null)
			{
				sendMessage("One of your arrows fizzled");
				return SpellResult.FAILURE;
			}

			arrow.setShooter(player);

			if (useFire) {
				arrow.setFireTicks(300);
			}

			// Hackily make this an infinite arrow and set damage
			try {
				Method getHandleMethod = arrow.getClass().getMethod("getHandle");
				Object handle = getHandleMethod.invoke(arrow);
				Field fromPlayerField = handle.getClass().getField("fromPlayer");
				fromPlayerField.setInt(handle, 2);
				Method setDamageMethod = handle.getClass().getMethod("b", Double.TYPE);
				setDamageMethod.invoke(handle, damage);
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
			arrow.setTicksLived(300);
		}

		castMessage("You fire some magical arrows");

		return SpellResult.SUCCESS;
	}
}
