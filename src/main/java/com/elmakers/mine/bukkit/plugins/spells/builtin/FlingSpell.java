package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.spells.Spell;

public class FlingSpell extends Spell
{
	private final int defaultMagnitude = 5;

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

}
