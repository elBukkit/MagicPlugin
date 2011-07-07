package com.elmakers.mine.bukkit.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.BlockData;
import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.persistence.dao.MaterialData;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;

public class Transmute extends Spell
{
    @Override
    public String getDescription()
    {
        return "Modify your last construction";
    }

    @Override
    public String getName()
    {
        return "transmute";
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        BlockList transmuteAction = null;

        /*
         * Use target if targeting
         */
        boolean usedTarget = false;
        targeting.targetThrough(Material.GLASS);
        Block target = targeting.getTargetBlock();

        if (target != null)
        {
            transmuteAction = magic.getLastBlockList(player.getName(), target);
            usedTarget = transmuteAction != null;
        }

        if (transmuteAction == null)
        {
            transmuteAction = magic.getLastBlockList(player.getName());
        }

        if (transmuteAction == null)
        {
            sendMessage(player, "Nothing to transmute");
            return false;
        }

        MaterialData material = getBuildingMaterial(parameters, null);
        if (material == null)
        {
            sendMessage(player, "Nothing to transmute with");
            return false;
        }

        for (BlockData undoBlock : transmuteAction)
        {
            Block block = undoBlock.getBlock();
            block.setType(material.getType());
            block.setData(material.getData());
        }

        if (usedTarget)
        {
            castMessage(player, "You transmute your target structure to " + material.getName());
        }
        else
        {
            castMessage(player, "You transmute your last structure to " + material.getName());
        }

        return true;
    }

}
