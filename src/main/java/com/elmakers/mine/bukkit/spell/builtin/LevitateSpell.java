package com.elmakers.mine.bukkit.spell.builtin;

import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.spell.SpellEventType;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;

import java.util.Collection;

public class LevitateSpell extends TargetingSpell implements Listener
{
	private static final float defaultFlySpeed = 0.1f;
	
	private long levitateEnded;
	private final long safetyLength = 10000;

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
    private ThrustAction thrust;
    private double crashDistance = 0;
    private double slowMultiplier = 1;

    private Collection<PotionEffect> crashEffects;

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
                spell.deactivate();
                return;
            }
            if (entity instanceof Player && !((Player)entity).isOnline()) {
                spell.deactivate();
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

        boolean checkHeight = autoDeactivateHeight > 0;
        if (checkHeight && mage.isPlayer()) {
            checkHeight = mage.getPlayer().isSneaking();
        }
        if (checkHeight) {
            int height = 0;
            Block block = entity.getLocation().getBlock();
            while (height < autoDeactivateHeight && block.getType() == Material.AIR)
            {
                block = block.getRelative(BlockFace.DOWN);
                height++;
            }

            if (height < autoDeactivateHeight)
            {
                deactivate();
                return;
            }
        }
        Vector direction = entity.getLocation().getDirection();
        direction.normalize();

        if (crashDistance > 0)
        {
            Vector threshold = direction.clone().multiply(crashDistance);
            if (checkForCrash(mage.getEyeLocation(), threshold)) return;
            if (checkForCrash(mage.getLocation(), threshold)) return;
        }

        double boost = thrustSpeed;
        if (mage.getPlayer().isSneaking()) {
            boost *= slowMultiplier;
        }
        else if (boostTicksRemaining > 0) {
            boost += castBoost;
            --boostTicksRemaining;
        }
        direction.multiply(boost);
        entity.setVelocity(direction);
    }

    protected boolean checkForCrash(Location source, Vector threshold)
    {
        Block facingBlock = source.getBlock();
        Block targetBlock = source.add(threshold).getBlock();
        if (!targetBlock.equals(facingBlock) && targetBlock.getType() != Material.AIR) {
            deactivate(true);
            sendMessage(getMessage("crash"));
            mage.deactivateAllSpells();
            playEffects("crash");
            LivingEntity livingEntity = mage.getLivingEntity();
            if (crashEffects != null && livingEntity != null && crashEffects.size() > 0) {
                CompatibilityUtils.applyPotionEffects(livingEntity, crashEffects);
            }
            return true;
        }

        return false;
    }

    protected boolean checkActive()
    {
        if (!isActive()) return false;

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
        slowMultiplier = parameters.getDouble("slow", 1);
        castBoost = parameters.getDouble("boost", 0);
        yBoost = parameters.getDouble("y_boost", 2);
		flySpeed = (float)parameters.getDouble("speed", 0);
        thrustSpeed = (float)parameters.getDouble("thrust", 0);
        thrustFrequency = parameters.getInt("thrust_interval", thrustFrequency);
        autoDeactivateHeight = parameters.getInt("auto_deactivate", 0);
        boostTicks = parameters.getInt("boost_ticks", 1);
        crashDistance = parameters.getDouble("crash_distance", 0);

        crashEffects = getPotionEffects(parameters);

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
			levitateEnded = 0;
			
			// Visual effect
            int ringEffectRange = (int)Math.ceil(((double)maxRingEffectRange - minRingEffectRange) * event.getDamage() / maxDamageAmount + minRingEffectRange);
            ringEffectRange = Math.min(maxRingEffectRange, ringEffectRange);
            playEffects("land", ringEffectRange);
		}
	}

    @Override
    public com.elmakers.mine.bukkit.api.block.MaterialAndData getEffectMaterial()
    {
        Block block = mage.getEntity().getLocation().getBlock();
        block = block.getRelative(BlockFace.DOWN);
        return new MaterialAndData(block);
    }
}
