package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class ArrowSpell extends Spell
{ 
	@Override
	public boolean onCast(ConfigurationNode parameters) 
	{
		int arrowCount = 1;
		arrowCount = parameters.getInt("count", arrowCount);
		boolean useFire = parameters.getBoolean("fire", true);

		Arrow firstArrow = null;
		float speed = 3;
		float spread = 0.1f;
		for (int ai = 0; ai < arrowCount; ai++)
		{
			Arrow arrow = null;
			if (firstArrow == null) {
				arrow = player.launchProjectile(Arrow.class);
				firstArrow = arrow;
			} else {
				Location location = firstArrow.getLocation();
				Vector velocity = firstArrow.getVelocity();
				location.setX(location.getX() + velocity.getX() * (1 + Math.random() * arrowCount));
				location.setY(location.getY() + velocity.getY() * (1 + Math.random() * arrowCount));
				location.setZ(location.getZ() + velocity.getZ() * (1 + Math.random() * arrowCount));
				arrow = player.getWorld().spawnArrow(location, velocity, speed, spread);
			}

			if (arrow == null)
			{
				sendMessage(player, "One of your arrows fizzled");
				return false;
			}

			if (useFire) {
				arrow.setFireTicks(300);
			}

			// Hackily make this an infinite arrow
			try {
				Method getHandleMethod = arrow.getClass().getMethod("getHandle");
				Object handle = getHandleMethod.invoke(arrow);
				Field fromPlayerField = handle.getClass().getField("fromPlayer");
				fromPlayerField.setInt(handle, 2);
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
			arrow.setTicksLived(300);
		}

		castMessage(player, "You fire some magical arrows");

		return true;
	}

	@Override
	public void onLoad(ConfigurationNode node)
	{
		disableTargeting();
	}
}
