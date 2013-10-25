package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellEventType;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class LevitateSpell extends Spell
{
	private long levitateEnded;
	private final long safetyLength = 10000;
	
	@Override
	public boolean onCast(ConfigurationNode parameters) 
	{
		if (player.getAllowFlight()) {
			castMessage(player, "You feel heavier");
			player.setFlying(false);
			player.setAllowFlight(false);
			
			// Prevent the player from death by fall
			spells.registerEvent(SpellEventType.PLAYER_DAMAGE, this);
			levitateEnded = System.currentTimeMillis();
			
			return false;
		}
		castMessage(player, "You feel lighter");
		Vector velocity = player.getVelocity();
		velocity.setY(velocity.getY() + 2);
		player.setVelocity(velocity);
		Bukkit.getScheduler().scheduleSyncDelayedTask(spells.getPlugin(), new Runnable() {
			public void run() {
				player.setFlySpeed(0.99f);
				player.setAllowFlight(true);
				player.setFlying(true);
			}
		}, 2);

		return true;
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
