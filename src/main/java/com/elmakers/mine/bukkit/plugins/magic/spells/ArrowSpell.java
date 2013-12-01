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
		int arrowCount = 1;
		arrowCount = parameters.getInt("count", arrowCount);
		boolean useFire = parameters.getBoolean("fire", false);

		float speed = 0.6f;
		float spread = 12f;
		double damage = 2.0;
		
		speed = (float)parameters.getDouble("speed", speed);
		spread = (float)parameters.getDouble("spread", spread);
		damage = parameters.getDouble("damage", damage);
		Vector direction = player.getLocation().getDirection();
		direction.normalize().multiply(speed);
		Vector up = new Vector(0, 1, 0);
		Vector perp = new Vector();
		perp.copy(direction);
		perp.crossProduct(up);
		
		for (int ai = 0; ai < arrowCount; ai++)
		{
			Arrow arrow = null;
			Location location = player.getEyeLocation();
			location.setX(location.getX() + perp.getX() * (Math.random() * arrowCount / 2 - arrowCount / 4));
			location.setY(location.getY());
			location.setZ(location.getZ() + perp.getZ() * (Math.random() * arrowCount / 2 - arrowCount / 4));
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

	@Override
	public void onLoad(ConfigurationNode node)
	{
		disableTargeting();
	}
}
