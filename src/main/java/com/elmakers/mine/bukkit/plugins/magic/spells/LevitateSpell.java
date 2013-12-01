package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellEventType;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class LevitateSpell extends Spell
{
	private long levitateEnded;
	private final long safetyLength = 10000;
	private float flightSpeed = 0.6f;
	
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		flightSpeed = (float)parameters.getDouble("speed", flightSpeed);
		if (player.getAllowFlight()) {
			sendMessage("You feel heavier");
			
			deactivate();
			
			return SpellResult.COST_FREE;
		}
		castMessage("You feel lighter");
		activate();

		return SpellResult.SUCCESS;
	}
	
	@Override
	public void onDeactivate() {
		player.setFlying(false);
		player.setAllowFlight(false);
		
		// Prevent the player from death by fall
		spells.registerEvent(SpellEventType.PLAYER_DAMAGE, this);
		levitateEnded = System.currentTimeMillis();
	}
	
	@Override
	public void onActivate() {
		Vector velocity = player.getVelocity();
		velocity.setY(velocity.getY() + 2);
		player.setVelocity(velocity);
		Bukkit.getScheduler().scheduleSyncDelayedTask(spells.getPlugin(), new Runnable() {
			public void run() {
				player.setFlySpeed(flightSpeed);
				player.setAllowFlight(true);
				player.setFlying(true);
			}
		}, 2);
	}

	@Override
	public void onPlayerDamage(EntityDamageEvent event)
	{
		if (event.getCause() != DamageCause.FALL) return;

		spells.unregisterEvent(SpellEventType.PLAYER_DAMAGE, this);

		if (levitateEnded == 0) return;

		if (levitateEnded + safetyLength > System.currentTimeMillis())
		{
			event.setCancelled(true);
			levitateEnded = 0;;
		}
	}

	@Override
	public void onLoad(ConfigurationNode node)
	{
		disableTargeting();
	}
}
