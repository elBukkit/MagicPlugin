package com.elmakers.mine.bukkit.spells;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;

public class FlingSpell extends Spell
{
    private final int magnitude = 20;

    @Override
    public String getDescription()
    {
        return "Sends you flying in the target direction";
    }

    @Override
    public String getName()
    {
        return "fling";
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        Vector velocity = targeting.getAimVector();
        velocity.multiply(magnitude);
        CraftPlayer craftPlayer = (CraftPlayer) player;
        craftPlayer.setVelocity(velocity);
        castMessage(player, "Whee!");
        return true;
    }

}
