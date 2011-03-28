package com.elmakers.mine.bukkit.spells;

import org.bukkit.Material;

import com.elmakers.mine.bukkit.magic.Spell;

public class InvincibleSpell extends Spell
{
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

    @Override
    public String getName()
    {
        return "invincible";
    }

    @Override
    public boolean onCast(List<ParameterData> parameters)
    {
        boolean invincible = !spells.isInvincible(player);
        spells.setInvincible(player, invincible);
        if (invincible)
        {
            castMessage(player, "You feel invincible!");
        }
        else
        {
            castMessage(player, "You feel ... normal.");
        }
        return true;
    }

}
