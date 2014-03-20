package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
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
	private Random random = new Random();

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
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
		
		String projectileTypeName = parameters.getString("projectile", "Fireball");
		final Class<?> projectileClass = NMSUtils.getBukkitClass("net.minecraft.server.EntityProjectile");
		final Class<?> fireballClass = NMSUtils.getBukkitClass("net.minecraft.server.EntityFireball");
		final Class<?> arrowClass = NMSUtils.getBukkitClass("net.minecraft.server.EntityArrow");
		final Class<?> worldClass = NMSUtils.getBukkitClass("net.minecraft.server.World");
		final Class<?> entityClass = NMSUtils.getBukkitClass("net.minecraft.server.Entity");
		final Class<?> craftArrowClass = NMSUtils.getBukkitClass("org.bukkit.craftbukkit.entity.CraftArrow");
		
		if (projectileClass == null || worldClass == null || fireballClass == null || arrowClass == null || craftArrowClass == null) {
			controller.getLogger().warning("Can't find NMS classess");
			return SpellResult.FAIL;
		}
		
		Class<?> projectileType = NMSUtils.getBukkitClass("net.minecraft.server.Entity" + projectileTypeName);
		if (projectileType == null
			|| (!arrowClass.isAssignableFrom(projectileType) 
			&& !projectileClass.isAssignableFrom(projectileType) 
			&& !fireballClass.isAssignableFrom(projectileType))) {
			controller.getLogger().warning("Bad projectile class: " + projectileTypeName);
			return SpellResult.FAIL;
		}
		
		// Prevent explosive projectiles in no-build zones
		if (fireballClass.isAssignableFrom(projectileType) && !hasBuildPermission(getLocation().getBlock())) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		Constructor<? extends Object> constructor = null;
		Method shootMethod = null;
		Method setPositionRotationMethod = null;
		Field dirXField = null;
		Field dirYField = null;
		Field dirZField = null;
		Method addEntityMethod = null;
		try {
			constructor = projectileType.getConstructor(worldClass);
			
			if (fireballClass.isAssignableFrom(projectileType)) {
				dirXField = projectileType.getField("dirX");
				dirYField = projectileType.getField("dirY");
				dirZField = projectileType.getField("dirZ");
			} 

			if (projectileClass.isAssignableFrom(projectileType) || arrowClass.isAssignableFrom(projectileType)) {
				shootMethod = projectileType.getMethod("shoot", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);
			}
			
			setPositionRotationMethod = projectileType.getMethod("setPositionRotation", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);
			addEntityMethod = worldClass.getMethod("addEntity", entityClass);
	    } catch (Throwable ex) {
			ex.printStackTrace();
			return SpellResult.FAIL;
		}
		
		// Prepare parameters
		Location location = getEyeLocation();
		Vector direction = getDirection().normalize();

		// Track projectiles to remove them after some time.
		List<Projectile> projectiles = new ArrayList<Projectile>();
		
		// Spawn projectiles
		Object nmsWorld = NMSUtils.getHandle(location.getWorld());
		Player player = getPlayer();
		for (int i = 0; i < count; i++) {
			try {
				// Spawn a new projectile
				Object nmsProjectile = null;
				nmsProjectile = constructor.newInstance(nmsWorld);

				if (nmsProjectile == null) {
					throw new Exception("Failed to spawn projectile of class " + projectileTypeName);
				}
				
				// Set position and rotation, and potentially velocity (direction)
				// Velocity must be set manually- EntityFireball.setDirection applies a crazy-wide gaussian distribution!
				if (dirXField != null && dirYField != null && dirZField != null) {
					// Taken from EntityArrow
					double spreadWeight = Math.min(0.4f,  spread * 0.007499999832361937D);
					
					double dx = speed * (direction.getX() + (random.nextGaussian() * spreadWeight));
					double dy = speed * (direction.getY() + (random.nextGaussian() * spreadWeight));
					double dz = speed * (direction.getZ() + (random.nextGaussian() * spreadWeight));

			        dirXField.set(nmsProjectile, dx * 0.1D);
			        dirYField.set(nmsProjectile, dy * 0.1D);
			        dirZField.set(nmsProjectile, dz * 0.1D);
				}
				setPositionRotationMethod.invoke(nmsProjectile, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

				if (shootMethod != null) {
					shootMethod.invoke(nmsProjectile, direction.getX(), direction.getY(), direction.getZ(), speed, spread);
				}
				
				Entity entity = NMSUtils.getBukkitEntity(nmsProjectile);
				if (entity == null || !(entity instanceof Projectile)) {
					throw new Exception("Got invalid bukkit entity from projectile of class " + projectileTypeName);
				}
				Projectile projectile = (Projectile)entity;

				if (player != null) {
					projectile.setShooter(player);
				}
				
				projectiles.add(projectile);
				
				addEntityMethod.invoke(nmsWorld, nmsProjectile);
				
				if (projectile instanceof Fireball) {
					Fireball fireball = (Fireball)projectile;
					fireball.setIsIncendiary(useFire);
					fireball.setYield(size);
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
