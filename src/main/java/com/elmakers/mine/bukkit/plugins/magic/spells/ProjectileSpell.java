package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.WitherSkull;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.NMSUtils;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class ProjectileSpell extends Spell 
{
	int defaultSize = 1;

	@SuppressWarnings("unchecked")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		if (!mage.hasBuildPermission(getPlayer().getLocation().getBlock())) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		int size = parameters.getInt("size", defaultSize);
		size = (int)(mage.getRadiusMultiplier() * size);
		boolean useFire = parameters.getBoolean("fire", true);
		int tickIncrease = parameters.getInteger("tick_increase", 1180);
		double damage = parameters.getDouble("damage", 0);
		float speed = (float)parameters.getDouble("speed", 0.6f);
		float spread = (float)parameters.getDouble("spread", 12);
		
		String projectileClass = parameters.getString("projectile", "Fireball");
		final Class<?> arrowClass = NMSUtils.getBukkitClass("net.minecraft.server.EntityArrow");
		final Class<?> craftArrowClass = NMSUtils.getBukkitClass("org.bukkit.craftbukkit.entity.CraftArrow");
		
		// Track projectiles to remove them after some time.
		List<Arrow> arrows = new ArrayList<Arrow>();
		
		try {
			Location playerLocation = getPlayer().getLocation();
			Class<? extends Projectile> projectileType = (Class<? extends Projectile>)Class.forName("org.bukkit.entity." + projectileClass);
			Projectile projectile = getPlayer().launchProjectile(projectileType);
			if (projectile == null) {
				throw new Exception("A projectile fizzled");
			}
			projectile.setShooter(getPlayer());
			if (projectile instanceof WitherSkull) {
				playerLocation.getWorld().playSound(playerLocation, Sound.WITHER_SHOOT, 1.0f, 1.5f);		
			}
			if (projectile instanceof Fireball) {
				Fireball fireball = (Fireball)projectile;
				fireball.setIsIncendiary(useFire);
				fireball.setYield(size);
				if (!(projectile instanceof WitherSkull)) {
					playerLocation.getWorld().playSound(playerLocation, Sound.GHAST_FIREBALL, 1.0f, 1.5f);
				}
			}
			if (projectile instanceof Arrow) {
				Arrow arrow = (Arrow)projectile;
				arrows.add(arrow);
				if (useFire) {
					arrow.setFireTicks(300);
				}
				// Hackily make this an infinite arrow and set damage
				try {
					if (arrowClass == null || craftArrowClass == null) {
						controller.getLogger().warning("Can not access NMS EntityArrow class");
					} else {
						Method getHandleMethod = arrow.getClass().getMethod("getHandle");
						Object handle = getHandleMethod.invoke(arrow);
						
						Method shootMethod = arrowClass.getMethod("shoot", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);
						Vector velocity = getPlayer().getLocation().getDirection();
						shootMethod.invoke(handle, velocity.getX(), velocity.getY(), velocity.getZ(), speed, spread);
						
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
				
				playerLocation.getWorld().playSound(playerLocation, Sound.SHOOT_ARROW, 1.0f, 1.5f);
			}
		} catch(Exception ex) {
			sendMessage("Failed to fire projectile class " + projectileClass);
			controller.getLogger().warning(ex.getMessage());
			return SpellResult.FAILURE;
		}

		if (tickIncrease > 0 && arrows.size() > 0 && arrowClass != null) {
			scheduleKillArrows(arrows, tickIncrease, arrowClass, craftArrowClass, 5);
		}
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
