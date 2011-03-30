package com.elmakers.mine.bukkit.spells;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;
import com.elmakers.mine.bukkit.plugins.nether.NetherManager;

public class PhaseSpell extends Spell
{
    private final NetherManager nether;

    public PhaseSpell(NetherManager nether)
    {
        this.nether = nether;
    }

    @Override
    public String getDescription()
    {
        return "Phase between worlds";
    }

    @Override
    public String getName()
    {
        return "phase";
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        if (nether == null)
        {
            return false;
        }

        String worldName = parameters.getString("world", null);
        
        return nether.go(player, worldName) != null;
    }
}
