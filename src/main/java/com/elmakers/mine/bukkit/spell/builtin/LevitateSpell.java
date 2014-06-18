package com.elmakers.mine.bukkit.spell.builtin;

import com.elmakers.mine.bukkit.api.effect.ParticleType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.spell.SpellEventType;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.effect.builtin.EffectRing;
import com.elmakers.mine.bukkit.spell.TargetingSpell;

public class LevitateSpell extends TargetingSpell implements Listener
{
	private static final float defaultFlySpeed = 0.1f;
	
	private long levitateEnded;
	private final long safetyLength = 10000;

    private final static int effectSpeed = 2;
    private final static int effectPeriod = 2;
    private final static int minRingEffectRange = 1;
    private final static int maxRingEffectRange = 8;
    private final static int maxDamageAmount = 150;
    
    private float flySpeed = 0;
    private int flyDelay = 2;
    private int startDelay = 0;

    private int autoDeactivateHeight = 0;
    private int boostTicksRemaining = 0;

    private double castBoost = 0;
    private int boostTicks = 0;
    private double yBoost = 2;
    private float thrustSpeed = 0;
    private int thrustFrequency = 1;
    protected ThrustAction thrust;

    private boolean cancelled = false;

    public class ThrustAction implements Runnable
    {
        private final LevitateSpell spell;
        private final int taskId;

        public ThrustAction(LevitateSpell spell, int delay, int interval)
        {
            Plugin plugin = spell.getMage().getController().getPlugin();
            this.spell = spell;
            BukkitScheduler scheduler = Bukkit.getScheduler();
            taskId = scheduler.scheduleSyncRepeatingTask(plugin, this, delay, interval);
        }

        public void stop()
        {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        public void run()
        {
            if (!spell.checkActive())
            {
                return;
            }
            Entity entity = spell.getMage().getEntity();
            if (entity == null || entity.isDead())
            {
                spell.cancel();
                return;
            }
            if (entity instanceof Player && !((Player)entity).isOnline()) {
                cancel();
                return;
            }

            spell.thrust();
        }
    }

    protected void thrust()
    {
        if (thrustSpeed == 0) return;
        Entity entity = mage.getEntity();
        if (entity == null) return;

        if (autoDeactivateHeight > 0) {
            int height = 0;
            Block block = entity.getLocation().getBlock();
            while (height < autoDeactivateHeight && block.getType() == Material.AIR)
            {
                block = block.getRelative(BlockFace.DOWN);
                height++;
            }

            if (height < autoDeactivateHeight)
            {
                cancel();
                return;
            }
        }

        Vector thrustVector = entity.getLocation().getDirection();
        thrustVector.normalize();
        double boost = thrustSpeed;
        if (boostTicksRemaining > 0) {
            boost += castBoost;
            --boostTicksRemaining;
        }
        thrustVector.multiply(boost);
        entity.setVelocity(thrustVector);
    }

    protected boolean checkActive()
    {
        if (cancelled) return false;

        Entity entity = mage.getEntity();
        if (entity == null || entity.isDead()) return false;
        if (entity instanceof Player && !((Player)entity).isOnline()) return false;

        return true;
    }

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
        Player player = mage.getPlayer();
        if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }

        startDelay = parameters.getInt("start_delay", 0);
        castBoost = parameters.getDouble("boost", 0);
        yBoost = parameters.getDouble("y_boost", 2);
		flySpeed = (float)parameters.getDouble("speed", 0);
        thrustSpeed = (float)parameters.getDouble("thrust", 0);
        thrustFrequency = parameters.getInt("thrust_interval", thrustFrequency);
        autoDeactivateHeight = parameters.getInt("auto_deactivate", 0);
        boostTicks = parameters.getInt("boost_ticks", 1);

        thrustSpeed *= mage.getRadiusMultiplier();
        castBoost *= mage.getRadiusMultiplier();

		if (isActive()) {
            if (castBoost != 0) {
                boostTicksRemaining += boostTicks;
                return SpellResult.AREA;
            }
			deactivate();
			return SpellResult.COST_FREE;
		}
		activate();

		return SpellResult.CAST;
	}

    @Override
    public boolean onCancel() {
        boolean active = !cancelled && isActive();
        if (active) {
            cancelled = true;
            deactivate();
        }
        return active;
    }
	
	@Override
	public void onDeactivate() {
        if (thrust != null) {
            thrust.stop();
            thrust = null;
        }
		final Player player = mage.getPlayer();
		if (player == null) return;
		
		if (flySpeed > 0) {
			player.setFlySpeed(defaultFlySpeed);
		}
		
		player.setFlying(false);
		player.setAllowFlight(false);
		
		// Prevent the player from death by fall
		mage.registerEvent(SpellEventType.PLAYER_DAMAGE, this);
		levitateEnded = System.currentTimeMillis();
	}
	
	@Override
	public void onActivate() {
		final Player player = mage.getPlayer();
		if (player == null) return;

        cancelled = false;
		
		if (flySpeed > 0) {
			player.setFlySpeed(flySpeed * defaultFlySpeed);
		}

        if (thrustSpeed > 0) {
            if (thrust != null) {
                thrust.stop();
            }
            thrust = new ThrustAction(this, thrustFrequency + flyDelay + startDelay, thrustFrequency);
        }
		
		Vector velocity = player.getVelocity();
		velocity.setY(velocity.getY() + yBoost);
        player.setVelocity(velocity);
		Bukkit.getScheduler().scheduleSyncDelayedTask(controller.getPlugin(), new Runnable() {
			public void run() {
				player.setAllowFlight(true);
				player.setFlying(true);
			}
		}, flyDelay);
	}

	@SuppressWarnings("deprecation")
	@Override
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event)
	{
		if (event.getCause() != DamageCause.FALL) return;

		mage.unregisterEvent(SpellEventType.PLAYER_DAMAGE, this);

		if (levitateEnded == 0) return;

		if (levitateEnded + safetyLength > System.currentTimeMillis())
		{
			event.setCancelled(true);
			levitateEnded = 0;;
			
			// Visual effect
			// TODO: Data-drive?
			Location effectLocation = event.getEntity().getLocation();
			Block block = event.getEntity().getLocation().getBlock();
			block = block.getRelative(BlockFace.DOWN);
			int ringEffectRange = (int)Math.ceil(((double)maxRingEffectRange - minRingEffectRange) * event.getDamage() / maxDamageAmount + minRingEffectRange);
			int effectRange = Math.min(maxRingEffectRange, ringEffectRange);
			effectRange = Math.min(getMaxRange(), effectRange / effectSpeed);
			
			EffectRing effect = new EffectRing(controller.getPlugin());
			effect.setRadius(effectRange);
            effect.setParticleType(ParticleType.BLOCK_BREAKING);
            effect.setMaterial(block);
			effect.setPeriod(effectPeriod);
			effect.start(effectLocation, null);
		}
	}
}
