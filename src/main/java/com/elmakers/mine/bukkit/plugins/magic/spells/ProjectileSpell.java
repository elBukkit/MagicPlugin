package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.NMSUtils;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class ProjectileSpell extends Spell 
{
	private int defaultSize = 1;

	@SuppressWarnings("unchecked")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Player player = getPlayer();
		if (player == null) {
			return SpellResult.PLAYER_REQUIRED;
		}

		int count = parameters.getInt("count", 1);
		int size = parameters.getInt("size", defaultSize);
		int radius = parameters.getInt("radius", 0);
		double damage = parameters.getDouble("damage", 0);
		float speed = (float)parameters.getDouble("speed", 0.6f);
		float spread = (float)parameters.getDouble("spread", 12);
		Collection<PotionEffect> effects = null;
		
		if (radius > 0) {
			effects = getPotionEffects(parameters);
			radius = (int)(mage.getRadiusMultiplier() * radius);
		}

		// Modify with wand power
		count *= mage.getRadiusMultiplier();
		size = (int)(mage.getRadiusMultiplier() * size);
		float damageMultiplier = mage.getDamageMultiplier();
		speed *= damageMultiplier;
		damage *= damageMultiplier;
		spread /= damageMultiplier;
		
		boolean useFire = parameters.getBoolean("fire", true);
		int tickIncrease = parameters.getInteger("tick_increase", 1180);
		
		String projectileClass = parameters.getString("projectile", "Fireball");
		final Class<?> arrowClass = NMSUtils.getBukkitClass("net.minecraft.server.EntityArrow");
		final Class<?> craftArrowClass = NMSUtils.getBukkitClass("org.bukkit.craftbukkit.entity.CraftArrow");
		
		// Track projectiles to remove them after some time.
		List<Projectile> projectiles = new ArrayList<Projectile>();
		Class<? extends Projectile> projectileType = null;
		try {
			projectileType = (Class<? extends Projectile>)Class.forName("org.bukkit.entity." + projectileClass);
		} catch (Exception ex) {
			castMessage("Your projectile fizzled");
			controller.getLogger().warning(ex.getMessage());
			return SpellResult.FAIL;
		}
		
		if (projectileType != Arrow.class && !hasBuildPermission(getLocation().getBlock())) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		
		Location location = getEyeLocation();
		Vector direction = getDirection().normalize();
		for (int i = 0; i < count; i++) {
			try {
				Projectile projectile = null;
				
				if (projectileType == Arrow.class) {
					// Move arrow out a bit
					Location arrowLocation = location.clone();
					arrowLocation.setX(arrowLocation.getX() + direction.getX());
					arrowLocation.setY(arrowLocation.getY() + direction.getY());
					arrowLocation.setZ(arrowLocation.getZ() + direction.getZ());
					projectile = player.getWorld().spawnArrow(arrowLocation, direction, speed, spread);
					projectile.setShooter(player);
				} else {
					projectile = player.launchProjectile(projectileType);
				}
				if (projectile == null) {
					throw new Exception("A projectile fizzled");
				}
				projectiles.add(projectile);
				projectile.setShooter(getPlayer());
				
				if (projectile instanceof Fireball) {
					Fireball fireball = (Fireball)projectile;
					fireball.setIsIncendiary(useFire);
					fireball.setYield(size);
					
					try {
						Object handle = NMSUtils.getHandle(fireball);
						Method setPositionMethod = handle.getClass().getMethod("setPositionRotation", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);
						setPositionMethod.invoke(handle, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
					} catch (Throwable ex) {
						ex.printStackTrace();
					}
				}
				if (projectile instanceof Arrow) {
					Arrow arrow = (Arrow)projectile;
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
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}

		if (tickIncrease > 0 && projectiles.size() > 0 && arrowClass != null) {
			scheduleProjectileCheck(projectiles, tickIncrease, effects, radius, arrowClass, craftArrowClass, 5);
		}
		return SpellResult.CAST;
	}
	
	protected void scheduleProjectileCheck(final Collection<Projectile> projectiles, final int tickIncrease, 
			final Collection<PotionEffect> effects, final int radius, final Class<?> arrowClass, final Class<?> craftArrowClass, final int retries) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(controller.getPlugin(), new Runnable() {
			public void run() {
				checkProjectiles(projectiles, tickIncrease, effects, radius, arrowClass, craftArrowClass, retries);
			}
		}, 40);
	}
	
	protected void checkProjectiles(final Collection<Projectile> projectiles, final int tickIncrease, 
			final Collection<PotionEffect> effects, final int radius, final Class<?> arrowClass, final Class<?> craftArrowClass, int retries) {
		try {
			Field lifeField = arrowClass.getDeclaredField("j");
			Method getHandleMethod = craftArrowClass.getMethod("getHandle");
			final Collection<Projectile> remaining = new ArrayList<Projectile>();
			for (Projectile projectile : projectiles) {
				if (projectile.isDead()) {
					// Apply potion effects if configured
					applyPotionEffects(projectile.getLocation(), radius, effects);
				} else if (projectile instanceof Arrow){
					Object handle = getHandleMethod.invoke(projectile);
					lifeField.setAccessible(true);
					int currentLife = (Integer)lifeField.get(handle);
					if (currentLife < tickIncrease) {
						lifeField.set(handle, tickIncrease);
					}
					
					remaining.add(projectile);
				}
			}
			if (remaining.size() > 0 && retries > 0) {
				scheduleProjectileCheck(remaining, tickIncrease, effects, radius, arrowClass, craftArrowClass, retries - 1);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
