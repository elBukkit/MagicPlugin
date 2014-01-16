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
	
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		if (getPlayer().getAllowFlight()) {
			deactivate();
			return SpellResult.COST_FREE;
		}
		activate();

		return SpellResult.SUCCESS;
	}
	
	@Override
	public void onDeactivate() {
		getPlayer().setFlying(false);
		getPlayer().setAllowFlight(false);
		
		// Prevent the player from death by fall
		controller.registerEvent(SpellEventType.PLAYER_DAMAGE, this);
		levitateEnded = System.currentTimeMillis();
		
		castMessage("You feel heavier");
	}
	
	@Override
	public void onActivate() {
		Vector velocity = getPlayer().getVelocity();
		velocity.setY(velocity.getY() + 2);
		getPlayer().setVelocity(velocity);
		Bukkit.getScheduler().scheduleSyncDelayedTask(controller.getPlugin(), new Runnable() {
			public void run() {
				getPlayer().setAllowFlight(true);
				getPlayer().setFlying(true);
			}
		}, 2);

		castMessage("You feel lighter");
	}

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
		}
	}
}
