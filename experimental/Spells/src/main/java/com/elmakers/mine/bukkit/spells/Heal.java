package com.elmakers.mine.bukkit.spells;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;

public class Heal extends Spell
{
    @Override
    public String getDescription()
    {
        return "Heal yourself";
    }

    @Override
    public String getName()
    {
        return "heal";
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        castMessage(player, "You heal yourself");
        player.setHealth(20);
        return true;
    }
}
