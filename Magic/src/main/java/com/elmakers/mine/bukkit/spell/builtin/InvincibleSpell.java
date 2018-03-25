package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import com.elmakers.mine.bukkit.api.spell.SpellEventType;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;

public class InvincibleSpell extends TargetingSpell implements Listener
{
    protected float protectAmount = 0;
    protected int amount = 100;

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        amount = parameters.getInt("amount", amount);

        if (protectAmount != 0)
        {
            deactivate();
            return SpellResult.DEACTIVATE;
        }
        else
        {
            activate();
        }

        return SpellResult.CAST;
    }

    @Override
    public void onDeactivate()
    {
        mage.unregisterEvent(SpellEventType.PLAYER_DAMAGE, this);
        protectAmount = 0;
    }

    @Override
    public void onActivate()
    {
        mage.registerEvent(SpellEventType.PLAYER_DAMAGE, this);
        protectAmount = (float)amount / 100;
    }

    @Override
    @EventHandler
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
