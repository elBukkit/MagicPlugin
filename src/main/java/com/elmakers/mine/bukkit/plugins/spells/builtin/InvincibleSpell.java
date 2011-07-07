package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.Material;

import com.elmakers.mine.bukkit.plugins.spells.Spell;

public class InvincibleSpell extends Spell 
{
    public InvincibleSpell()
    {
        addVariant("ironskin", Material.IRON_CHESTPLATE, getCategory(), "Protect you from damage", "99");
        addVariant("leatherskin", Material.LEATHER_CHESTPLATE, getCategory(), "Protect you from some damage", "50");
    }
    
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
        
        Float currentAmount = spells.invincibleAmount(player);
        if (currentAmount != null)
        {
            sendMessage(player, "You feel ... normal.");
            spells.setInvincible(player, 0);
        }
        else
        {
            spells.setInvincible(player, (float)amount / 100);
            
            if (amount >= 100)
            {
                sendMessage(player, "You feel invincible!");
            }
            else
            {
                sendMessage(player, "You feel strong!");
            }
        }
       
		return true;
	}

	@Override
	public String getName() 
	{
		return "invincible";
	}

	@Override
	public String getCategory() 
	{
		return "help";
	}

	@Override
	public String getDescription() 
	{
		return "Makes you impervious to damage";
	}

	@Override
	public Material getMaterial()
	{
		return Material.GOLDEN_APPLE;
	}

}
