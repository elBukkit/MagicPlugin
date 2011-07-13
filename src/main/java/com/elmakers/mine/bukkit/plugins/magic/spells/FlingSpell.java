package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellEventType;

public class FlingSpell extends Spell
{
	private final int defaultMagnitude = 5;
	private final long safetyLength = 20000;
	private long lastFling = 0;

	@Override
	public boolean onCast(String[] parameters)
	{
	    int magnitude = defaultMagnitude;
        if (parameters.length > 0)
        {
            try
            {
                magnitude = Integer.parseInt(parameters[0]);
            }
            catch (NumberFormatException ex)
            {
                magnitude = defaultMagnitude;
            }
        }
		Vector velocity = getAimVector();
		velocity.normalize();
		velocity.multiply(magnitude);
		CraftPlayer craftPlayer = (CraftPlayer)player;
		craftPlayer.setVelocity(velocity);
		castMessage(player, "Whee!");
		
        spells.registerEvent(SpellEventType.PLAYER_DAMAGE, this);
        lastFling = System.currentTimeMillis();
		return true;
	}
	
	@Override
    public void onPlayerDamage(EntityDamageEvent event)
    {
        if (event.getCause() != DamageCause.FALL) return;

        spells.unregisterEvent(SpellEventType.PLAYER_DAMAGE, this);
        
        if (lastFling == 0) return;
        
        if (lastFling + safetyLength > System.currentTimeMillis())
        {
            event.setCancelled(true);
            lastFling = 0;
        }
    }
}
