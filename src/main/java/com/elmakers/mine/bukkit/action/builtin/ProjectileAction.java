package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.GeneralAction;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.ActionHandler;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class ProjectileAction  extends BaseSpellAction implements GeneralAction
{
	private int defaultSize = 1;
	private Random random = new Random();
	private static Field lifeField = null;
	private static Method getHandleMethod = null;
	private static boolean reflectionInitialized = false;

	private static Class<?> projectileClass;
	private static Class<?> fireballClass;
	private static Class<?> arrowClass;
	private static Class<?> worldClass;
	private static Class<?> entityClass;
	private static Class<?> craftArrowClass;

	private void checkReflection()
	{
		if (!reflectionInitialized)
		{
			projectileClass = NMSUtils.getBukkitClass("net.minecraft.server.EntityProjectile");
			fireballClass = NMSUtils.getBukkitClass("net.minecraft.server.EntityFireball");
			arrowClass = NMSUtils.getBukkitClass("net.minecraft.server.EntityArrow");
			worldClass = NMSUtils.getBukkitClass("net.minecraft.server.World");
			entityClass = NMSUtils.getBukkitClass("net.minecraft.server.Entity");
			craftArrowClass = NMSUtils.getBukkitClass("org.bukkit.craftbukkit.entity.CraftArrow");

			reflectionInitialized = true;
			try {
				// This is kinda hacky, like fer reals :\
				try {
					// 1.8
					lifeField = arrowClass.getDeclaredField("ap");
				}
				catch (Throwable ignore2)
				{
					try {
						// 1.7
						lifeField = arrowClass.getDeclaredField("at");
					} catch (Throwable ignore) {
						// Prior
						lifeField = arrowClass.getDeclaredField("j");
					}
				}
				getHandleMethod = craftArrowClass.getMethod("getHandle");
			} catch (Throwable ex) {
				lifeField = null;
				getHandleMethod = null;
				getController().getLogger().warning("Failed to create short-lived arrow. Set tick_increase to 0 to avoid this message");
			}
			if (lifeField != null)
			{
				lifeField.setAccessible(true);
			}
		}
	}

	@Override
	public SpellResult perform(ConfigurationSection parameters)
	{
		checkReflection();
		int count = parameters.getInt("count", 1);
		int checkFrequency = parameters.getInt("check_frequency", 40);
		int size = parameters.getInt("size", defaultSize);
		double damage = parameters.getDouble("damage", 0);
		float speed = (float)parameters.getDouble("speed", 0.6f);
		float spread = (float)parameters.getDouble("spread", 12);
		ActionHandler actions = null;

		Mage mage = getMage();
		MageController controller = getController();
		actions = getActions("projectile");

		if (actions != null) {
			actions.setParameters(parameters);
		}

		// Modify with wand power
		// Turned some of this off for now
		// count *= mage.getRadiusMultiplier();
		size = (int)(mage.getRadiusMultiplier() * size);
		float damageMultiplier = mage.getDamageMultiplier();
		// speed *= damageMultiplier;
		damage *= damageMultiplier;
		spread /= damageMultiplier;
		
		boolean useFire = parameters.getBoolean("fire", true);
		int tickIncrease = parameters.getInt("tick_increase", 1180);
		
		String projectileTypeName = parameters.getString("projectile", "Arrow");
		
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
        LivingEntity player = mage.getLivingEntity();
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
				Vector modifiedLocation = location.toVector().clone();
				if (i > 0 && fireballClass.isAssignableFrom(projectileType) && spread > 0) {
					modifiedLocation.setX(modifiedLocation.getX() + direction.getX() + (random.nextGaussian() * spread / 5));
					modifiedLocation.setY(modifiedLocation.getY() + direction.getY() + (random.nextGaussian() * spread / 5));
					modifiedLocation.setZ(modifiedLocation.getZ() + direction.getZ() + (random.nextGaussian() * spread / 5));
				}
				setPositionRotationMethod.invoke(nmsProjectile, modifiedLocation.getX(), modifiedLocation.getY(), modifiedLocation.getZ(), location.getYaw(), location.getPitch());

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

							if (lifeField != null) {
								try {
									int currentLife = (Integer) lifeField.get(handle);
									if (currentLife < tickIncrease) {
										lifeField.set(handle, tickIncrease);
									}
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
						}
					} catch (Throwable ex) {
						ex.printStackTrace();
					}
				}
                Collection<EffectPlayer> projectileEffects = getEffects("projectile");
                for (EffectPlayer effectPlayer : projectileEffects) {
                    effectPlayer.start(projectile.getLocation(), projectile, null, null);
                }
				registerForUndo(projectile);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		if (projectiles.size() > 0) {
			registerProjectiles(projectiles, actions, checkFrequency, 4);
		}

		return SpellResult.CAST;
	}

	@Override
	public boolean isUndoable() {
		return true;
	}

	protected void registerProjectiles(final Collection<Projectile> projectiles,
			final ActionHandler actions, final int checkFrequency, final int retries) {

		for (Projectile projectile : projectiles)
		{
			if (actions != null)
			{
				projectile.setMetadata("actions", new FixedMetadataValue(getController().getPlugin(), actions));
			}
			projectile.setMetadata("spell", new FixedMetadataValue(getController().getPlugin(), getSpell()));

		}
		scheduleProjectileCheck(projectiles, checkFrequency, retries);
	}

	protected void scheduleProjectileCheck(final Collection<Projectile> projectiles,
										   final int checkFrequency, final int retries) {

		Bukkit.getScheduler().scheduleSyncDelayedTask(getController().getPlugin(), new Runnable() {
			public void run() {
				checkProjectiles(projectiles, checkFrequency, retries);
			}
		}, checkFrequency);
	}

	protected void checkProjectiles(final Collection<Projectile> projectiles,
			int checkFrequency, int retries) {

		final Collection<Projectile> remaining = new ArrayList<Projectile>();
		for (Projectile projectile : projectiles)
		{
			if (retries == 0)
			{
				projectile.remove();
			}
			if (projectile.isDead() && projectile.hasMetadata("actions"))
			{
				ActionHandler actions = null;
				for (MetadataValue metadata : projectile.getMetadata("actions"))
				{
					Object meta = metadata.value();
					if (meta instanceof ActionHandler)
					{
						actions = (ActionHandler)meta;
						break;
					}
				}
				if (actions != null)
				{
					actions.perform(projectile.getLocation(), projectile, projectile.getLocation(), null);
				}
				projectile.removeMetadata("actions", getController().getPlugin());
			} else  {
				remaining.add(projectile);
			}
		}
		if (remaining.size() > 0 && retries > 0)
		{
			scheduleProjectileCheck(remaining, checkFrequency, retries - 1);
		}
	}

	@Override
	public void getParameterNames(Collection<String> parameters) {
		super.getParameterNames(parameters);
		parameters.add("count");
		parameters.add("check_frequency");
		parameters.add("size");
		parameters.add("damage");
		parameters.add("speed");
		parameters.add("spread");
		parameters.add("projectile");
		parameters.add("fire");
		parameters.add("tick_increase");
	}

	@Override
	public void getParameterOptions(Collection<String> examples, String parameterKey) {
		if (parameterKey.equals("check_frequency")) {
			examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_DURATIONS)));
		} else if (parameterKey.equals("count") || parameterKey.equals("size") || parameterKey.equals("speed")
				|| parameterKey.equals("spread") || parameterKey.equals("tick_increase") || parameterKey.equals("damage")) {
			examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
		} else if (parameterKey.equals("fire")) {
			examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
		} else if (parameterKey.equals("projectile")) {
			examples.add("LargeFireball");
			examples.add("SmallFireball");
			examples.add("WitherSkull");
			examples.add("Arrow");
			examples.add("Snowball");
		} else {
			super.getParameterOptions(examples, parameterKey);
		}
	}
}
