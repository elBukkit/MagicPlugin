package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.effect.EffectRing;
import com.elmakers.mine.bukkit.plugins.magic.spell.SpellEventType;
import com.elmakers.mine.bukkit.plugins.magic.spell.TargetingSpell;

public class LevitateSpell extends TargetingSpell
{
	private long levitateEnded;
	private final long safetyLength = 10000;

    private final static int effectSpeed = 2;
    private final static int effectPeriod = 2;
    private final static int minRingEffectRange = 1;
    private final static int maxRingEffectRange = 8;
    private final static int maxDamageAmount = 150;
    
    private float flySpeedMultiplier = 0;
    private float restoreFlySpeed = 0;
	
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		flySpeedMultiplier = (float)parameters.getDouble("speed", 0);
		if (getPlayer().getAllowFlight()) {
			deactivate();
			return SpellResult.COST_FREE;
		}
		activate();

		return SpellResult.CAST;
	}
	
	@Override
	public void onDeactivate() {
		final Player player = getPlayer();
		if (player == null) return;
		
		if (restoreFlySpeed > 0) {
			player.setFlySpeed(restoreFlySpeed);
		}
		
		player.setFlying(false);
		player.setAllowFlight(false);
		
		// Prevent the player from death by fall
		controller.registerEvent(SpellEventType.PLAYER_DAMAGE, this);
		levitateEnded = System.currentTimeMillis();
	}
	
	@Override
	public void onActivate() {
		final Player player = getPlayer();
		if (player == null) return;
		
		// Store and modify fly speed
		if (flySpeedMultiplier > 0) {
			restoreFlySpeed = player.getFlySpeed();
			player.setFlySpeed(restoreFlySpeed * flySpeedMultiplier);
		}
		
		Vector velocity = getPlayer().getVelocity();
		velocity.setY(velocity.getY() + 2);
		getPlayer().setVelocity(velocity);
		Bukkit.getScheduler().scheduleSyncDelayedTask(controller.getPlugin(), new Runnable() {
			public void run() {
				player.setAllowFlight(true);
				player.setFlying(true);
				
			}
		}, 2);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onPlayerDamage(EntityDamageEvent event)
	{
		if (event.getCause() != DamageCause.FALL) return;

		controller.unregisterEvent(SpellEventType.PLAYER_DAMAGE, this);

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
			effect.setEffect(Effect.STEP_SOUND);
			effect.setEffectData(block.getTypeId());
			effect.setPeriod(effectPeriod);
			effect.start(effectLocation, null);
		}
	}
}
