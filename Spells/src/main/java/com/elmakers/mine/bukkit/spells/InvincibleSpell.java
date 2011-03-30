package com.elmakers.mine.bukkit.spells;

import org.bukkit.event.entity.EntityDamageEvent;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.magic.SpellEventType;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;

public class InvincibleSpell extends Spell
{
    protected boolean isInvicible = false;
    
    @Override
    public String getDescription()
    {
        return "Makes you impervious to damage";
    }

    @Override
    public String getName()
    {
        return "invincible";
    }
    
    protected void checkListener()
    {
        if (isInvicible)
        {
            magic.unregisterEvent(SpellEventType.PLAYER_DAMAGE, this);
        }
        else
        {
            magic.registerEvent(SpellEventType.PLAYER_DAMAGE, this);
        }
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event)
    {
        if (isInvicible)
        {
            event.setCancelled(true);
        }
    }
 
    @Override
    public boolean onCast(ParameterMap parameters)
    {
        isInvicible = !isInvicible;
        if (isInvicible)
        {
            castMessage(player, "You feel invincible!");
        }
        else
        {
            castMessage(player, "You feel ... normal.");
        }
        checkListener();
        return true;
    }
}
