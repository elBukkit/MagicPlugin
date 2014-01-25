package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.NMSUtils;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class ArrowSpell extends Spell
{ 
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		if (!hasBuildPermission(getPlayer().getLocation().getBlock())) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		int arrowCount = 1;
		arrowCount = parameters.getInt("count", arrowCount);
		// Modify with wand power
		arrowCount *= mage.getRadiusMultiplier();
		
		boolean useFire = parameters.getBoolean("fire", false);

		float speed = 0.6f;
		float spread = 12f;
		double damage = 0;
		int tickIncrease = 1180;
		
		speed = (float)parameters.getDouble("speed", speed);
		spread = (float)parameters.getDouble("spread", spread);
		damage = parameters.getDouble("damage", damage);
		tickIncrease = parameters.getInteger("tick_increase", tickIncrease);
		
		// Modify with wand power
		float damageMultiplier = mage.getDamageMultiplier();
		speed *= damageMultiplier;
		damage *= damageMultiplier;
		spread /= damageMultiplier;
		Vector direction = getPlayer().getLocation().getDirection();
		
		final Class<?> arrowClass = NMSUtils.getBukkitClass("net.minecraft.server.EntityArrow");
		final Class<?> craftArrowClass = NMSUtils.getBukkitClass("org.bukkit.craftbukkit.entity.CraftArrow");
		if (arrowClass == null || craftArrowClass == null) {
			controller.getLogger().warning("Can not access NMS EntityArrow class");
		}
		final List<Arrow> arrows = new ArrayList<Arrow>();
		
		for (int ai = 0; ai < arrowCount; ai++)
		{
			Arrow arrow = null;
			Location location = getPlayer().getLocation();
			location.setX(location.getX() + direction.getX() * (1 + Math.random() * arrowCount));
			location.setY(location.getY() + 1.5f);
			location.setZ(location.getZ() + direction.getZ() * (1 + Math.random() * arrowCount));
			
			arrow = getPlayer().getWorld().spawnArrow(location, direction, speed, spread);

			if (arrow == null)
			{
				sendMessage("One of your arrows fizzled");
				return SpellResult.FAILURE;
			}

			arrow.setShooter(getPlayer());

			if (useFire) {
				arrow.setFireTicks(300);
			}

			// Hackily make this an infinite arrow and set damage
			arrows.add(arrow);
			try {
				if (arrowClass != null) {
					Method getHandleMethod = arrow.getClass().getMethod("getHandle");
					Object handle = getHandleMethod.invoke(arrow);
					
					Field fromPlayerField = arrowClass.getField("fromPlayer");
					fromPlayerField.setInt(handle, 2);
					if (damage > 0) {
						Field damageField = arrowClass.getDeclaredField("damage");
						damageField.setAccessible(true);
						damageField.set(handle, damage);
					}
				}
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
		
		if (tickIncrease > 0 && arrows.size() > 0 && arrowClass != null) {
			scheduleKillArrows(arrows, tickIncrease, arrowClass, craftArrowClass, 5);
		}

		castMessage("You fire some magical arrows");

		return SpellResult.SUCCESS;
	}
	
	protected void scheduleKillArrows(final Collection<Arrow> arrows, final int tickIncrease, final Class<?> arrowClass, final Class<?> craftArrowClass, final int retries) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(controller.getPlugin(), new Runnable() {
			public void run() {
				killArrows(arrows, tickIncrease, arrowClass, craftArrowClass, retries);
			}
		}, 40);
	}
	
	protected void killArrows(final Collection<Arrow> arrows, final int tickIncrease, final Class<?> arrowClass, final Class<?> craftArrowClass, int retries) {
		try {
			Field lifeField = arrowClass.getDeclaredField("j");
			Method getHandleMethod = craftArrowClass.getMethod("getHandle");
			boolean done = true;
			for (Arrow arrow : arrows) {
				if (!arrow.isDead()) {
					Object handle = getHandleMethod.invoke(arrow);
					lifeField.setAccessible(true);
					int currentLife = (Integer)lifeField.get(handle);
					if (currentLife < tickIncrease) {
						lifeField.set(handle, tickIncrease);
						done = false;
					}
				}
			}
			if (!done && retries > 0) {
				scheduleKillArrows(arrows, tickIncrease, arrowClass, craftArrowClass, retries - 1);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
