package com.elmakers.mine.bukkit.spells;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.ParameterData;

public class AbsorbSpell extends Spell
{
    @Override
    public String getDescription()
    {
        return "Give yourself some of your target";
    }

    @Override
    public String getName()
    {
        return "absorb";
    }

    @Override
    public boolean onCast(List<ParameterData> parameters)
    {
        if (!isUnderwater())
        {
            targeting.noTargetThrough(Material.STATIONARY_WATER);
            targeting.noTargetThrough(Material.WATER);
        }
        Block target = targeting.getTargetBlock();

        if (target == null)
        {
            castMessage(player, "No target");
            return false;
        }
        int amount = 1;

        castMessage(player, "Absorbing some " + target.getType().name().toLowerCase());

        return giveMaterial(target.getType(), amount, (short) 0, target.getData());
    }
}
