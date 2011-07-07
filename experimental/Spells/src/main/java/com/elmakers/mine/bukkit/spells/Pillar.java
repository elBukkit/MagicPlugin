package com.elmakers.mine.bukkit.spells;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.persistence.dao.MaterialData;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;

public class Pillar extends Spell
{
    int MAX_SEARCH_DISTANCE = 255;

    @Override
    public String getDescription()
    {
        return "Raises a pillar up (or down)";
    }

    @Override
    public String getName()
    {
        return "pillar";
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        Block attachBlock = targeting.getTargetBlock();
        if (attachBlock == null)
        {
            castMessage(player, "No target");
            return false;
        }

        BlockFace direction = parameters.hasFlag("down") ? BlockFace.DOWN : BlockFace.UP;

        Block targetBlock = attachBlock.getFace(direction);
        int distance = 0;

        while (targeting.isTargetable(targetBlock.getType()) && distance <= MAX_SEARCH_DISTANCE)
        {
            distance++;
            attachBlock = targetBlock;
            targetBlock = attachBlock.getFace(direction);
        }
        if (targeting.isTargetable(targetBlock.getType()))
        {
            player.sendMessage("Can't pillar any further");
            return false;
        }

        MaterialData material = getBuildingMaterial(parameters, targetBlock);

        BlockList pillarBlocks = new BlockList();
        Block pillar = targeting.getBlockAt(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
        pillarBlocks.add(pillar);
        pillar.setType(material.getType());
        pillar.setData(material.getData());

        castMessage(player, "Creating a pillar of " + attachBlock.getType().name().toLowerCase());
        magic.addToUndoQueue(player, pillarBlocks);

        return true;
    }
}
