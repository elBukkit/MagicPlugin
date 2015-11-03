package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class ProjectileAction  extends CompoundAction
{
	private int defaultSize = 1;

    private int count;
    private int size;
	private long lifetime;
    private double damage;
    private float speed;
    private float spread;
    private boolean useFire;
    private boolean breakBlocks;
    private int tickIncrease;
    private String projectileTypeName;
    private int startDistance;
	private boolean setTarget;

	private long expiration;
	private Collection<Projectile> tracking = null;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters) {
        super.initialize(spell, parameters);

        breakBlocks = parameters.getBoolean("break_blocks", false);
        useFire = parameters.getBoolean("fire", false);
    }

    @Override
    public boolean requiresBuildPermission() {
        return useFire;
    }

    @Override
    public boolean requiresBreakPermission() {
        return breakBlocks;
    }

	@Override
	public void reset(CastContext context)
	{
		super.reset(context);
		expiration = System.currentTimeMillis() + lifetime;
		tracking = null;
	}

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        count = parameters.getInt("count", 1);
        size = parameters.getInt("size", defaultSize);
        damage = parameters.getDouble("damage", 0);
        speed = (float)parameters.getDouble("speed", 0.6f);
        spread = (float)parameters.getDouble("spread", 12);
        useFire = parameters.getBoolean("fire", false);
        tickIncrease = parameters.getInt("tick_increase", 1180);
        projectileTypeName = parameters.getString("projectile", "Arrow");
        breakBlocks = parameters.getBoolean("break_blocks", false);
        startDistance = parameters.getInt("start", 0);
		setTarget = parameters.getBoolean("set_target", false);
		lifetime = parameters.getLong("lifetime", 15000);
    }

	@Override
	public SpellResult perform(CastContext context)
	{
        MageController controller = context.getController();
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
		
		Class<?> projectileType = NMSUtils.getBukkitClass("net.minecraft.server.Entity" + projectileTypeName);
		if (!CompatibilityUtils.isValidProjectileClass(projectileType)) {
			controller.getLogger().warning("Bad projectile class: " + projectileTypeName);
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
        LivingEntity shootingEntity = context.getLivingEntity();
        ProjectileSource source = null;
        if (shootingEntity != null && shootingEntity instanceof ProjectileSource)
        {
            source = (ProjectileSource)shootingEntity;
        }
        for (int i = 0; i < count; i++) {
			try {
				// Spawn a new projectile
				Projectile projectile = CompatibilityUtils.spawnProjectile(projectileType, location, direction, source, speed, spread, i > 0 ? spread : 0, random);
				if (projectile == null) {
					return SpellResult.FAIL;
				}
				if (shootingEntity != null) {
					projectile.setShooter(shootingEntity);
				}
				
				projectiles.add(projectile);
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

					CompatibilityUtils.makeInfinite(projectile);
					if (damage > 0) {
						CompatibilityUtils.setDamage(projectile, damage);
					}
					if (tickIncrease > 0) {
						CompatibilityUtils.decreaseLifespan(projectile, tickIncrease);
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
				if (setTarget) {
					context.setTargetEntity(projectile);
					context.setTargetLocation(projectile.getLocation());
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		if (projectiles.size() > 0) {
			// TODO: Fix!
			// registerProjectiles(projectiles, actions, context, parameters, undoInterval);
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
    public void getParameterNames(Spell spell, Collection<String> parameters) {
		super.getParameterNames(spell, parameters);
		parameters.add("count");
		parameters.add("lifetime");
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
	public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
		if (parameterKey.equals("undo_interval")) {
			examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_DURATIONS)));
		} else if (parameterKey.equals("count") || parameterKey.equals("size") || parameterKey.equals("speed")
				|| parameterKey.equals("spread") || parameterKey.equals("tick_increase")
                || parameterKey.equals("damage") || parameterKey.equals("start")|| parameterKey.equals("lifetime")) {
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
			super.getParameterOptions(spell, parameterKey, examples);
		}
	}
}
