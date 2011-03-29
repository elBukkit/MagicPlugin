package com.elmakers.mine.bukkit.spells;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.persistence.dao.ParameterData;

public class DisintegrateSpell extends Spell
{
    private int defaultSearchDistance = 32;

    @Override
    public String getDescription()
    {
        return "Destroy the target block";
    }

    @Override
    public String getName()
    {
        return "disintegrate";
    }

    @Override
    public boolean onCast(List<ParameterData> parameters)
    {
        Block target = targeting.getTargetBlock();
        if (target == null)
        {
            castMessage(player, "No target");
            return false;
        }
        if (defaultSearchDistance > 0 && targeting.getDistance(player, target) > defaultSearchDistance)
        {
            castMessage(player, "Can't blast that far away");
            return false;
        }

        BlockList disintigrated = new BlockList();
        disintigrated.add(target);

        if (isUnderwater())
        {
            target.setType(Material.STATIONARY_WATER);
        }
        else
        {
            target.setType(Material.AIR);
        }

        magic.addToUndoQueue(player, disintigrated);
        castMessage(player, "ZAP!");

        return true;
    }

    @Override
    public void onLoad()
    {
        //defaultSearchDistance = properties.getInteger("spells-disintegrate-search-distance", defaultSearchDistance);
    }
}
