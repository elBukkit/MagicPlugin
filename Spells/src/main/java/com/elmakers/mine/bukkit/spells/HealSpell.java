package com.elmakers.mine.bukkit.spells;

import org.bukkit.Material;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.ParameterData;

public class HealSpell extends Spell
{
    @Override
    public String getCategory()
    {
        return "help";
    }

    @Override
    public String getDescription()
    {
        return "Heal yourself";
    }

    @Override
    public Material getMaterial()
    {
        return Material.BREAD;
    }

    @Override
    public String getName()
    {
        return "heal";
    }

    @Override
    public boolean onCast(List<ParameterData> parameters)
    {
        castMessage(player, "You heal yourself");
        player.setHealth(20);
        return true;
    }
}
