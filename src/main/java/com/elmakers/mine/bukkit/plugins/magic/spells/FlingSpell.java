package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellEventType;

public class FlingSpell extends Spell
{
	private final int defaultMagnitude = 5;
	private final long safetyLength = 20000;
	private HashMap<String, Long> lastFling = new HashMap<String, Long>();

   public FlingSpell()
    {
        addVariant("leap", Material.LEATHER_BOOTS, getCategory(), "Take a big leap", "2");
    }
    
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
		lastFling.put(player.getName(), System.currentTimeMillis());
		return true;
	}

	@Override
	public String getName()
	{
		return "fling";
	}

	@Override
	public String getCategory()
	{
		return "wip";
	}

	@Override
	public String getDescription()
	{
		return "Sends you flying in the target direction";
	}

	@Override
	public Material getMaterial()
	{
		return Material.IRON_BOOTS;
	}
	
    public void onPlayerDamage(Player player, EntityDamageEvent event)
    {
        if (event.getCause() != DamageCause.FALL) return;
        
        Long lastTime = lastFling.get(player.getName());
        if (lastTime == null) return;
        
        if (lastTime + safetyLength > System.currentTimeMillis())
        {
            event.setCancelled(true);
            lastFling.remove(player.getName());
            if (lastFling.size() == 0)
            {
                spells.unregisterEvent(SpellEventType.PLAYER_DAMAGE, this);
            }
        }
    }
}
