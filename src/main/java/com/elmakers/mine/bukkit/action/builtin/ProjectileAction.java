package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.TriggeredCompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.spell.BaseSpell;
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
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class ProjectileAction  extends TriggeredCompoundAction
{
	private int defaultSize = 1;
	private static Field lifeField = null;
	private static Method getHandleMethod = null;
	private static boolean reflectionInitialized = false;

    private int count;
    private int undoInterval;
    private int size;
    private double damage;
    private float speed;
    private float spread;
    private boolean useFire;
    private boolean breakBlocks;
    private int tickIncrease;
    private String projectileTypeName;
    private int startDistance;

	private static Class<?> projectileClass;
	private static Class<?> fireballClass;
	private static Class<?> arrowClass;
	private static Class<?> worldClass;
	private static Class<?> entityClass;
	private static Class<?> craftArrowClass;

	private void checkReflection(MageController controller)
	{
		if (!reflectionInitialized)
		{
            try
            {
                projectileClass = NMSUtils.getBukkitClass("net.minecraft.server.EntityProjectile");
                fireballClass = NMSUtils.getBukkitClass("net.minecraft.server.EntityFireball");
                arrowClass = NMSUtils.getBukkitClass("net.minecraft.server.EntityArrow");
                worldClass = NMSUtils.getBukkitClass("net.minecraft.server.World");
                entityClass = NMSUtils.getBukkitClass("net.minecraft.server.Entity");
                craftArrowClass = NMSUtils.getBukkitClass("org.bukkit.craftbukkit.entity.CraftArrow");

                try {
                    // This is kinda hacky, like fer reals :\
                    try {
                        // 1.8.3
                        lifeField = arrowClass.getDeclaredField("ar");
                    } catch (Throwable ignore3) {
                        try {
                            // 1.8
                            lifeField = arrowClass.getDeclaredField("ap");
                        } catch (Throwable ignore2) {
                            try {
                                // 1.7
                                lifeField = arrowClass.getDeclaredField("at");
                            } catch (Throwable ignore) {
                                // Prior
                                lifeField = arrowClass.getDeclaredField("j");
                            }
                        }
                    }
                    getHandleMethod = craftArrowClass.getMethod("getHandle");
                } catch (Throwable ex) {
                    lifeField = null;
                    getHandleMethod = null;
                    controller.getLogger().log(Level.WARNING, "Failed to create short-lived arrow. Set tick_increase to 0 to avoid this message", ex);
                }
                if (lifeField != null)
                {
                    lifeField.setAccessible(true);
                }
            }
            catch (Throwable failure)
            {
                controller.getLogger().log(Level.WARNING, "Failed to bind to NMS projectile objects, ProjectileAction will not work", failure);
            }
            reflectionInitialized = true;
		}
	}

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        count = parameters.getInt("count", 1);
        undoInterval = parameters.getInt("undo_interval", 200);
        size = parameters.getInt("size", defaultSize);
        damage = parameters.getDouble("damage", 0);
        speed = (float)parameters.getDouble("speed", 0.6f);
        spread = (float)parameters.getDouble("spread", 12);
        useFire = parameters.getBoolean("fire", false);
        tickIncrease = parameters.getInt("tick_increase", 1180);
        projectileTypeName = parameters.getString("projectile", "Arrow");
        breakBlocks = parameters.getBoolean("break_blocks", true);
        startDistance = parameters.getInt("start", 0);
    }

	@Override
	public SpellResult perform(CastContext context)
	{
        MageController controller = context.getController();
		checkReflection(controller);

		Mage mage = context.getMage();

		// Modify with wand power
		// Turned some of this off for now
		// int count = this.count * mage.getRadiusMultiplier();
        // int speed = this.speed * damageMultiplier;
		int size = (int)(mage.getRadiusMultiplier() * this.size);
		float damageMultiplier = mage.getDamageMultiplier();
        double damage = damageMultiplier * this.damage;
		float spread = this.spread / damageMultiplier;
        Random random = context.getRandom();
		
		if (projectileClass == null || worldClass == null || fireballClass == null || arrowClass == null || craftArrowClass == null) {
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
        Field projectileSourceField = null;
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
            projectileSourceField = projectileType.getField("projectileSource");
	    } catch (Throwable ex) {
			ex.printStackTrace();
			return SpellResult.FAIL;
		}
		
		// Prepare parameters
		Location location = context.getEyeLocation();
		Vector direction = context.getDirection().normalize();

        if (startDistance > 0) {
            location = location.clone().add(direction.clone().multiply(startDistance));
        }

		// Track projectiles to remove them after some time.
		List<Projectile> projectiles = new ArrayList<Projectile>();
		
		// Spawn projectiles
		Object nmsWorld = NMSUtils.getHandle(location.getWorld());
        LivingEntity shootingEntity = context.getLivingEntity();
        ProjectileSource source = null;
        if (shootingEntity != null && shootingEntity instanceof ProjectileSource)
        {
            source = (ProjectileSource)shootingEntity;
        }
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
				if (shootingEntity != null) {
					projectile.setShooter(shootingEntity);
				}
                if (source != null) {
                    Object projectileEntity = NMSUtils.getHandle(projectile);
                    projectileSourceField.set(projectileEntity, source);
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
                Collection<EffectPlayer> projectileEffects = context.getEffects("projectile");
                for (EffectPlayer effectPlayer : projectileEffects) {
                    effectPlayer.start(projectile.getLocation(), projectile, null, null);
                }
                context.registerForUndo(projectile);
                if (!breakBlocks) {
                    projectile.setMetadata("cancel_explosion", new FixedMetadataValue(controller.getPlugin(), true));
                }
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		if (projectiles.size() > 0) {
			registerProjectiles(projectiles, actions, context, parameters, undoInterval);
		}

		return SpellResult.CAST;
	}

	@Override
	public boolean isUndoable() {
		return true;
	}

	protected void registerProjectiles(final Collection<Projectile> projectiles,
			final ActionHandler actions,
            final CastContext context, final ConfigurationSection parameters,
            final int undoInterval) {

        for (Projectile projectile : projectiles) {
            ActionHandler.setActions(projectile, actions, context, parameters, "indirect_player_message");
            ActionHandler.setEffects(projectile, context, "hit");
        }

        if (undoInterval > 0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(context.getController().getPlugin(), new Runnable() {
                public void run() {
                    checkProjectiles(projectiles);
                }
            }, undoInterval);
        }
    }

	protected void checkProjectiles(final Collection<Projectile> projectiles) {

		for (Projectile projectile : projectiles)
		{
            projectile.remove();

            // Don't run actions here, the spell may have been undone
            // and removed the projectile!
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
        parameters.add("start");
		parameters.add("projectile");
		parameters.add("fire");
		parameters.add("tick_increase");
	}

	@Override
	public void getParameterOptions(Collection<String> examples, String parameterKey) {
		if (parameterKey.equals("undo_interval")) {
			examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_DURATIONS)));
		} else if (parameterKey.equals("count") || parameterKey.equals("size") || parameterKey.equals("speed")
				|| parameterKey.equals("spread") || parameterKey.equals("tick_increase")
                || parameterKey.equals("damage") || parameterKey.equals("start")) {
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
