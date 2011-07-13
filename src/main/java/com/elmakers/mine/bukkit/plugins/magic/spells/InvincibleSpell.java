package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.event.entity.EntityDamageEvent;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellEventType;

public class InvincibleSpell extends Spell 
{
    protected float protectAmount = 0;
    
 	@Override
	public boolean onCast(String[] parameters) 
	{
	    int amount = 100;
        if (parameters.length > 0)
        {
            try
            {
                amount = Integer.parseInt(parameters[0]);
            }
            catch (NumberFormatException ex)
            {
                amount = 100;
            }
        }
        
        if (protectAmount != 0)
        {
            sendMessage(player, "You feel ... normal.");
            spells.unregisterEvent(SpellEventType.PLAYER_DAMAGE, this); 
            protectAmount = 0;
        }
        else
        {
            spells.registerEvent(SpellEventType.PLAYER_DAMAGE, this);
            
            if (amount >= 100)
            {
                sendMessage(player, "You feel invincible!");
            }
            else
            {
                sendMessage(player, "You feel strong!");
            }
            
            protectAmount = (float)amount / 100;
        }
       
		return true;
	}

 	@Override
    public void onPlayerDamage(EntityDamageEvent event)
    {
        if (protectAmount > 0)
        {
            if (protectAmount >= 1)
            {
                event.setCancelled(true);
            }
            else
            {
                int newDamage = (int)Math.floor((1.0f - protectAmount) * event.getDamage());
                if (newDamage == 0) newDamage = 1;
                event.setDamage(newDamage);
            }
        }
    }
}
